package com.seccad_elizade.energytoemc.screen;

import com.seccad_elizade.energytoemc.EnergyToEmc;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class EmcCapacitorScreen extends AbstractContainerScreen<EmcCapacitorMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(EnergyToEmc.MODID, "textures/gui/capacitor_gui.png");

    public EmcCapacitorScreen(EmcCapacitorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = -1000;
        this.inventoryLabelX = -1000;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 176, 166);

        int barX = x + 38;
        int barY = y + 45;
        int barWidth = 100;
        int barHeight = 12;

        long stored = menu.getStoredEmc();
        long max = menu.getMaxEmc();

        boolean isCreative = max >= Long.MAX_VALUE || max < 0;
        double fillPct = isCreative ? 1.0 : (max > 0 ? (double) stored / max : 0);
        int scaledWidth = (int) (fillPct * barWidth);

        graphics.fill(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, 0xFF000000);
        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF222222);

        if (scaledWidth > 0) {
            int color1 = isCreative ? 0xFFFFD700 : 0xFFAA00FF;
            int color2 = isCreative ? 0xFFFFAA00 : 0xFF7700AA;

            graphics.fillGradient(barX, barY, barX + scaledWidth, barY + barHeight, color1, color2);
            graphics.fill(barX, barY, barX + scaledWidth, barY + 1, 0x44FFFFFF);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        long stored = menu.getStoredEmc();
        long max = menu.getMaxEmc();

        String emcText;
        if (max >= Long.MAX_VALUE || max < 0) {
            emcText = "Infinite EMC";
        } else {
            emcText = formatValue(stored) + " / " + formatValue(max) + " EMC";
        }

        int centerX = (imageWidth / 2) - (font.width(emcText) / 2);
        graphics.drawString(font, emcText, centerX, 65, 0xCCCCCC, true);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        if (isHovering(38, 45, 100, 12, mouseX, mouseY)) {
            long stored = menu.getStoredEmc();
            long max = menu.getMaxEmc();
            boolean isCreative = max >= Long.MAX_VALUE || max < 0;

            String detailStored = String.format("%,d EMC", stored);
            String detailMax = isCreative ? "Infinite" : String.format("%,d EMC", max);

            graphics.renderComponentTooltip(font, List.of(
                    Component.literal("EMC Storage").withStyle(s -> s.withBold(true).withColor(0xAA00FF)),
                    Component.literal("Current: " + detailStored).withStyle(s -> s.withColor(0xFFFFFF)),
                    Component.literal("Capacity: " + detailMax).withStyle(s -> s.withColor(0xAAAAAA))
            ), mouseX, mouseY);
        }

        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private String formatValue(long value) {
        if (value < 1000) return String.valueOf(value);
        int exp = (int) (Math.log(value) / Math.log(1000));
        char unit = "kMGTPE".charAt(exp - 1);
        return String.format("%.1f%s", value / Math.pow(1000, exp), unit);
    }
}