package com.seccad_elizade.energytoemc.screen;

import com.seccad_elizade.energytoemc.EnergyToEmc;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class EmcConverterScreen extends AbstractContainerScreen<EmcConverterMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(EnergyToEmc.MODID, "textures/gui/emc_converter_gui.png");

    public EmcConverterScreen(EmcConverterMenu menu, Inventory inventory, Component title) {
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
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 176, 166);

        float energyPercent = Mth.clamp((float) menu.getEnergy() / 1000000.0f, 0, 1);
        int eH = (int) (energyPercent * 50);
        guiGraphics.fill(x + 11, y + 70 - eH, x + 22, y + 70, 0xFF00AAFF);

        float emcPercent = Mth.clamp((float) menu.getEmc() / 100000.0f, 0, 1);
        int emcH = (int) (emcPercent * 50);
        guiGraphics.fill(x + 153, y + 70 - emcH, x + 166, y + 70, 0xFFAA00FF);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        long fePerSecond = (long) menu.getEnergyUsage() * 20;
        long emcPerSecond = (long) menu.getEmcProduction() * 20;

        if (isHoveringArea(mouseX, mouseY, x + 11, y + 20, 11, 50)) {
            guiGraphics.renderComponentTooltip(font, List.of(
                    Component.literal("Energy Storage").withStyle(ChatFormatting.BOLD),
                    Component.literal(String.format("%,d", menu.getEnergy()) + " / 1,000,000 FE").withStyle(ChatFormatting.BLUE),
                    Component.empty(),
                    Component.literal("Avg. Consumption: ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(String.format("%,d", fePerSecond) + " FE/s").withStyle(ChatFormatting.RED))
            ), mouseX, mouseY);
        }

        if (isHoveringArea(mouseX, mouseY, x + 153, y + 20, 13, 50)) {
            guiGraphics.renderComponentTooltip(font, List.of(
                    Component.literal("EMC Storage").withStyle(ChatFormatting.BOLD),
                    Component.literal(String.format("%,d", menu.getEmc()) + " / 100,000 EMC").withStyle(ChatFormatting.LIGHT_PURPLE),
                    Component.empty(),
                    Component.literal("Avg. Production: ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(String.format("%,d", emcPerSecond) + " EMC/s").withStyle(ChatFormatting.GREEN))
            ), mouseX, mouseY);
        }
    }

    private boolean isHoveringArea(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}