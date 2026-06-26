package com.craze.bttfmod.client;

import com.craze.bttfmod.BTTFMod;
import com.craze.bttfmod.entity.DeLoreanEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DeLoreanModel extends GeoModel<DeLoreanEntity> {

    @Override
    public ResourceLocation getModelResource(DeLoreanEntity entity) {
        return new ResourceLocation(BTTFMod.MODID, "geo/delorean.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DeLoreanEntity entity) {
        return new ResourceLocation(BTTFMod.MODID, "textures/entity/delorean.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DeLoreanEntity entity) {
        return new ResourceLocation(BTTFMod.MODID, "animations/delorean.animation.json");
    }
}
