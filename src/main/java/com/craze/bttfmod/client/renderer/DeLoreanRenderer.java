package com.craze.bttfmod.client.renderer;

import com.craze.bttfmod.client.DeLoreanModel;
import com.craze.bttfmod.entity.DeLoreanEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DeLoreanRenderer extends GeoEntityRenderer<DeLoreanEntity> {

    public DeLoreanRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new DeLoreanModel());
        // Push the shadow below the car
        this.shadowRadius = 1.8f;
    }

    @Override
    protected void applyRotations(DeLoreanEntity entity, PoseStack poseStack,
                                   float ageInTicks, float rotationYaw, float partialTick) {
        super.applyRotations(entity, poseStack, ageInTicks, rotationYaw, partialTick);
    }

    @Override
    public void render(DeLoreanEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        // Scale from GeckoLib pixel units to Minecraft blocks (1/16)
        poseStack.scale(0.0625f, 0.0625f, 0.0625f);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
