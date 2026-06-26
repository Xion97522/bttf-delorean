package com.craze.bttfmod.client;

import com.craze.bttfmod.entity.DeLoreanEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.craze.bttfmod.BTTFMod;
import com.craze.bttfmod.init.ModItems;

@Mod.EventBusSubscriber(modid = BTTFMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SpeedometerOverlay {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        if (!(player.getVehicle() instanceof DeLoreanEntity car)) return;

        GuiGraphics g = event.getGuiGraphics();
        Font font = mc.font;
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        // Speed in "mph" (mapped from 0–MAX_SPEED blocks/tick → 0–88 mph)
        float speed = (float) car.getDeltaMovement().horizontalDistance();
        int mph = Math.round(speed / 0.75f * 88f);

        // Background panel
        int px = w - 120;
        int py = h - 50;
        g.fill(px - 4, py - 4, px + 112, py + 30, 0xBB000000);
        g.fill(px - 4, py - 4, px + 112, py - 3, 0xFF00AAFF);

        // Speed display
        String speedText = mph + " MPH";
        g.drawString(font,
                Component.literal("⚡ " + speedText)
                        .withStyle(mph >= 80 ? ChatFormatting.AQUA : ChatFormatting.WHITE),
                px, py, 0xFFFFFFFF);

        // 88 mph progress bar
        int barW = 108;
        int barH = 6;
        g.fill(px, py + 12, px + barW, py + 12 + barH, 0xFF333333);
        int fill = (int)(barW * (float)mph / 88f);
        fill = Math.min(fill, barW);
        int barColor = (mph >= 80) ? 0xFF00FFFF : (mph >= 50) ? 0xFFFFAA00 : 0xFF00FF44;
        g.fill(px, py + 12, px + fill, py + 12 + barH, barColor);
        g.fill(px, py + 12, px + 1, py + 12 + barH, 0xFFFFFFFF);

        // Time travel prompt
        if (mph >= 80) {
            g.drawString(font,
                    Component.literal("TIME TRAVEL READY!")
                            .withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA),
                    px, py + 20, 0xFF00FFFF);
        } else {
            g.drawString(font,
                    Component.literal("88 mph to time travel")
                            .withStyle(ChatFormatting.DARK_GRAY),
                    px, py + 20, 0xFF888888);
        }
    }
}
