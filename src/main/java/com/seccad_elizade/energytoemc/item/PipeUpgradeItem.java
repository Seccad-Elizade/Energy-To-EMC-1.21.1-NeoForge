package com.seccad_elizade.energytoemc.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

public class PipeUpgradeItem extends Item {
    private final int tier;
    private final long transferRatePerSecond;

    public PipeUpgradeItem(Properties properties, int tier, long transferRatePerSecond) {
        super(properties.stacksTo(1));
        this.tier = tier;
        this.transferRatePerSecond = transferRatePerSecond;
    }

    public long getTransferPerTick() {
        return Math.max(1, this.transferRatePerSecond / 20);
    }

    public int getTier() {
        return tier;
    }

    public int getParticleMultiplier() {
        return (this.tier + 1) * 2;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ChatFormatting tierColor = switch (tier) {
            case 0 -> ChatFormatting.WHITE;
            case 1 -> ChatFormatting.GREEN;
            case 2 -> ChatFormatting.AQUA;
            default -> ChatFormatting.LIGHT_PURPLE;
        };

        tooltip.add(Component.literal("Pipe Tier: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(tier + 1)).withStyle(tierColor, ChatFormatting.BOLD)));

        tooltip.add(Component.empty());

        tooltip.add(Component.literal("Max Transfer Rate:").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal(" " + String.format("%,d", transferRatePerSecond) + " EMC/sec")
                .withStyle(ChatFormatting.WHITE));

        tooltip.add(Component.literal(" (" + String.format("%,d", getTransferPerTick()) + " EMC/tick)")
                .withStyle(ChatFormatting.DARK_GRAY));

        tooltip.add(Component.empty());

        tooltip.add(Component.literal("⚠ Only works in EXTRACT mode")
                .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));

        super.appendHoverText(stack, context, tooltip, flag);
    }
}