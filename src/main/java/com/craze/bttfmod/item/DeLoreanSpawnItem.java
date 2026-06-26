package com.craze.bttfmod.item;

import com.craze.bttfmod.init.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class DeLoreanSpawnItem extends Item {
    public DeLoreanSpawnItem(Properties props) {
        super(props);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (!level.isClientSide) {
            BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
            var entity = ModEntityTypes.DELOREAN.get().create(level);
            if (entity != null) {
                entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                entity.setYRot(ctx.getPlayer() != null ? ctx.getPlayer().getYRot() : 0f);
                level.addFreshEntity(entity);
                if (!ctx.getPlayer().isCreative()) {
                    ctx.getItemInHand().shrink(1);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
