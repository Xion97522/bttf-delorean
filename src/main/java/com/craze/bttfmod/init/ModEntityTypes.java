package com.craze.bttfmod.init;

import com.craze.bttfmod.BTTFMod;
import com.craze.bttfmod.entity.DeLoreanEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, BTTFMod.MODID);

    public static final RegistryObject<EntityType<DeLoreanEntity>> DELOREAN =
            ENTITY_TYPES.register("delorean",
                    () -> EntityType.Builder.<DeLoreanEntity>of(DeLoreanEntity::new, MobCategory.MISC)
                            .sized(3.5f, 1.8f)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build(new ResourceLocation(BTTFMod.MODID, "delorean").toString()));
}
