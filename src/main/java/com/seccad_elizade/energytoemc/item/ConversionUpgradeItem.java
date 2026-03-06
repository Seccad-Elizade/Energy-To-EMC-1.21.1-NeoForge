package com.seccad_elizade.energytoemc.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ConversionUpgradeItem extends Item {
    private final int profitMultiplier;

    public ConversionUpgradeItem(Properties properties, int profitMultiplier) {
        super(properties.stacksTo(1));
        this.profitMultiplier = profitMultiplier;
    }

    public int getProfitMultiplier() {
        return profitMultiplier;
    }

    public int getTier() {
        return switch (this.profitMultiplier) {
            case 3 -> 1;
            case 6 -> 2;
            case 9 -> 3;
            case 12 -> 4;
            case 15 -> 5;
            default -> 0;
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int tier = getTier();

        tooltip.add(Component.translatable("tooltip.energytoemc.upgrade_tier_" + tier)
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.literal("Profit: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(profitMultiplier + "x EMC").withStyle(ChatFormatting.YELLOW)));

        tooltip.add(Component.empty());
        tooltip.add(Component.literal("Efficiency: Highest").withStyle(ChatFormatting.DARK_GRAY));
    }
}