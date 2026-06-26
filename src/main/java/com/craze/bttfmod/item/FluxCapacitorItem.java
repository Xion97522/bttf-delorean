package com.craze.bttfmod.item;

import com.craze.bttfmod.entity.DeLoreanEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FluxCapacitorItem extends Item {

    public FluxCapacitorItem(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.literal("Roads? Where we're going, we don't need roads.")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        tooltip.add(Component.literal("Right-click on a DeLorean to prime time travel.")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Hold while riding to see your speed.")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, net.minecraft.world.entity.Entity entity) {
        // Using the flux capacitor on a DeLorean while holding it shows time travel readiness
        if (entity instanceof DeLoreanEntity && player.level().isClientSide) {
            player.displayClientMessage(
                    Component.literal("⚡ FLUX CAPACITOR: PRIMED — reach 88 mph to travel through time!")
                            .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), true);
        }
        return false;
    }

    // HUD speed display is handled in the client event handler
}
