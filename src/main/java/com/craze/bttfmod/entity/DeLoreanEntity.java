package com.craze.bttfmod.entity;

import com.craze.bttfmod.BTTFMod;
import com.craze.bttfmod.init.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DeLoreanEntity extends Entity implements GeoEntity {

    // ── Synced data ──────────────────────────────────────────────────────────
    private static final EntityDataAccessor<Boolean> DRIVING =
            SynchedEntityData.defineId(DeLoreanEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DOORS_OPEN =
            SynchedEntityData.defineId(DeLoreanEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> SPEED =
            SynchedEntityData.defineId(DeLoreanEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> FLUX_TIMER =
            SynchedEntityData.defineId(DeLoreanEntity.class, EntityDataSerializers.INT);

    // ── Constants ────────────────────────────────────────────────────────────
    private static final float MAX_SPEED       = 0.75f;  // blocks/tick
    private static final float TIME_TRAVEL_SPD = 88.0f;  // mph threshold (display only – 88 = 1.1 bl/tk conceptual)
    private static final float ACCEL           = 0.04f;
    private static final float BRAKE           = 0.08f;
    private static final float TURN_SPEED      = 3.5f;
    private static final int   TIME_TRAVEL_TICKS = 60;   // 3s at 20 tps

    // ── State ────────────────────────────────────────────────────────────────
    private float currentSpeed = 0f;
    private int   timeTravelCooldown = 0;
    private boolean wasDoorsOpen = false;

    // ── GeckoLib ─────────────────────────────────────────────────────────────
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE_ANIM   = RawAnimation.begin().thenLoop("animation.delorean.idle");
    private static final RawAnimation DRIVE_ANIM  = RawAnimation.begin().thenLoop("animation.delorean.drive");
    private static final RawAnimation DOOR_OPEN   = RawAnimation.begin().thenPlay("animation.delorean.door_open");
    private static final RawAnimation DOOR_CLOSE  = RawAnimation.begin().thenPlay("animation.delorean.door_close");

    // ─────────────────────────────────────────────────────────────────────────
    //  Constructor
    // ─────────────────────────────────────────────────────────────────────────
    public DeLoreanEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.setNoGravity(false);
        this.noCulling = true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Entity setup
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void defineSynchedData() {
        entityData.define(DRIVING,    false);
        entityData.define(DOORS_OPEN, false);
        entityData.define(SPEED,      0f);
        entityData.define(FLUX_TIMER, 0);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        currentSpeed = tag.getFloat("Speed");
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putFloat("Speed", currentSpeed);
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Interaction – mount/dismount
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        if (!level().isClientSide) {
            if (this.getPassengers().isEmpty()) {
                // Open doors, let player in
                entityData.set(DOORS_OPEN, true);
                player.startRiding(this);
                entityData.set(DRIVING, true);
                level().playSound(null, blockPosition(),
                        ModSounds.DELOREAN_ENGINE.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
            } else if (player.isPassenger()) {
                // Player pressing interact while riding = toggle doors (visual)
                boolean open = entityData.get(DOORS_OPEN);
                entityData.set(DOORS_OPEN, !open);
            }
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Dismount
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void removePassenger(@NotNull Entity passenger) {
        super.removePassenger(passenger);
        if (!level().isClientSide) {
            entityData.set(DRIVING, false);
            entityData.set(DOORS_OPEN, false);
            currentSpeed = 0f;
            entityData.set(SPEED, 0f);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Tick – main driving logic
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void tick() {
        super.tick();

        if (timeTravelCooldown > 0) timeTravelCooldown--;

        if (!level().isClientSide) {
            serverTick();
        }
    }

    private void serverTick() {
        // Sync speed
        entityData.set(SPEED, currentSpeed);

        // Apply gravity
        if (!this.onGround() && !this.isInWater()) {
            this.setDeltaMovement(getDeltaMovement().add(0, -0.08, 0));
        }

        Entity driver = getFirstPassenger();
        if (driver instanceof Player player) {
            handleDriving(player);
        } else {
            // No driver — decelerate
            currentSpeed = Mth.lerp(0.1f, currentSpeed, 0f);
            if (Math.abs(currentSpeed) < 0.001f) currentSpeed = 0f;
        }

        // Move
        if (currentSpeed != 0f) {
            float yaw = Mth.DEG_TO_RAD * this.getYRot();
            double dx = -Math.sin(yaw) * currentSpeed;
            double dz =  Math.cos(yaw) * currentSpeed;
            this.setDeltaMovement(dx, getDeltaMovement().y, dz);
        } else {
            this.setDeltaMovement(getDeltaMovement().x * 0.5, getDeltaMovement().y, getDeltaMovement().z * 0.5);
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(getDeltaMovement().scale(0.98));
    }

    private void handleDriving(Player player) {
        // Read input from player's movement intent (via xxa/zza on server via packet)
        float forward = player.zza;   // 1 = W, -1 = S
        float strafe  = player.xxa;   // 1 = A, -1 = D (turn)

        // Acceleration / braking
        if (forward > 0f) {
            currentSpeed = Math.min(currentSpeed + ACCEL, MAX_SPEED);
        } else if (forward < 0f) {
            if (currentSpeed > 0.01f) {
                currentSpeed = Math.max(currentSpeed - BRAKE, 0f);
            } else {
                currentSpeed = Math.max(currentSpeed - ACCEL * 0.5f, -MAX_SPEED * 0.4f);
            }
        } else {
            currentSpeed = Mth.lerp(0.05f, currentSpeed, 0f);
        }

        // Turning (only while moving)
        if (Math.abs(currentSpeed) > 0.01f) {
            float dir = (currentSpeed > 0) ? 1f : -1f;
            this.setYRot(this.getYRot() - strafe * TURN_SPEED * dir);
        }

        // Engine sound loop
        if (level().getGameTime() % 20 == 0 && Math.abs(currentSpeed) > 0.05f) {
            level().playSound(null, blockPosition(),
                    ModSounds.DELOREAN_ENGINE.get(), SoundSource.NEUTRAL,
                    0.5f + Math.abs(currentSpeed) / MAX_SPEED,
                    0.8f + Math.abs(currentSpeed) / MAX_SPEED * 0.4f);
        }

        // ── TIME TRAVEL at 88mph (MAX_SPEED threshold) ───────────────────────
        if (currentSpeed >= MAX_SPEED * 0.95f && timeTravelCooldown == 0) {
            entityData.set(FLUX_TIMER, TIME_TRAVEL_TICKS);
            timeTravelCooldown = 200; // 10s cooldown
            initiateTimeTravel(player);
        }
    }

    private void initiateTimeTravel(Player player) {
        if (level() instanceof ServerLevel serverLevel) {
            // Visual effects
            for (int i = 0; i < 20; i++) {
                double ox = (random.nextDouble() - 0.5) * 4;
                double oy = random.nextDouble() * 2;
                double oz = (random.nextDouble() - 0.5) * 4;
                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.END_ROD,
                        getX() + ox, getY() + oy, getZ() + oz,
                        3, 0.1, 0.1, 0.1, 0.5);
                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.FLASH,
                        getX(), getY() + 1, getZ(),
                        1, 0, 0, 0, 0);
            }
            // Sound
            level().playSound(null, blockPosition(),
                    ModSounds.TIME_TRAVEL.get(), SoundSource.NEUTRAL, 3.0f, 1.0f);

            // Teleport 100–300 blocks in the direction of travel
            float yaw = Mth.DEG_TO_RAD * this.getYRot();
            double dist = 150 + random.nextInt(150);
            double nx = getX() - Math.sin(yaw) * dist;
            double nz = getZ() + Math.cos(yaw) * dist;

            // Find safe Y
            BlockPos target = serverLevel.getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos((int)nx, 64, (int)nz));

            this.teleportTo(nx, target.getY() + 1, nz);
            player.teleportTo(nx, target.getY() + 1, nz);

            // Lightning bolt at origin (where we left from)
            net.minecraft.world.entity.LightningBolt bolt = new net.minecraft.world.entity.LightningBolt(
                    net.minecraft.world.entity.EntityType.LIGHTNING_BOLT, level());
            bolt.moveTo(getX(), getY(), getZ());
            serverLevel.addFreshEntity(bolt);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Passenger positioning
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void positionRider(@NotNull Entity passenger, @NotNull MoveFunction callback) {
        if (hasPassenger(passenger)) {
            float yaw = Mth.DEG_TO_RAD * this.getYRot();
            double px = getX() - Math.sin(yaw) * 0.2;
            double py = getY() + getPassengersRidingOffset() + passenger.getMyRidingOffset();
            double pz = getZ() + Math.cos(yaw) * 0.2;
            callback.accept(passenger, px, py, pz);
            passenger.setYRot(passenger.getYRot() + (this.getYRot() - this.yRotO));
            passenger.setYBodyRot(this.getYRot());
        }
    }

    @Override
    public double getPassengersRidingOffset() {
        return 1.0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Physics / damage
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (!level().isClientSide && !this.isRemoved()) {
            if (source.getEntity() instanceof Player player && player.isCreative()) {
                this.discard();
                return true;
            }
            // DeLorean is indestructible (it's a time machine)
            return false;
        }
        return false;
    }

    @Override
    public boolean canCollideWith(@NotNull Entity entity) {
        return !(entity instanceof Player p && p.isPassenger());
    }

    @Override
    public boolean isPushable() { return false; }

    // ─────────────────────────────────────────────────────────────────────────
    //  GeckoLib
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        // Main controller: idle vs driving wheel spin
        registrar.add(new AnimationController<>(this, "movement", 5, state -> {
            boolean driving = entityData.get(DRIVING);
            float spd = entityData.get(SPEED);
            if (driving && Math.abs(spd) > 0.02f) {
                return state.setAndContinue(DRIVE_ANIM);
            }
            return state.setAndContinue(IDLE_ANIM);
        }));

        // Doors controller
        registrar.add(new AnimationController<>(this, "doors", 2, state -> {
            boolean open = entityData.get(DOORS_OPEN);
            if (open && !wasDoorsOpen) {
                wasDoorsOpen = true;
                return state.setAndContinue(DOOR_OPEN);
            } else if (!open && wasDoorsOpen) {
                wasDoorsOpen = false;
                return state.setAndContinue(DOOR_CLOSE);
            }
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
