package com.seccad_elizade.energytoemc.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seccad_elizade.energytoemc.network.PayloadPipeMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class EmcPipeScreen extends AbstractContainerScreen<EmcPipeMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("energytoemc", "textures/gui/emc_pipe_gui.png");

    public EmcPipeScreen(EmcPipeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = -1000;

        this.addRenderableWidget(Button.builder(Component.literal("Switch Mode"), (button) -> {
            PacketDistributor.sendToServer(new PayloadPipeMode(this.menu.getBlockEntity().getBlockPos()));
        }).bounds(leftPos + 38, topPos + 50, 100, 20).build());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

        boolean isExtract = menu.isExtractMode();
        String modeText = isExtract ? "MODE: EXTRACT" : "MODE: INSERT";
        int color = isExtract ? 0xFF5555 : 0x55FF55;

        int textX = (imageWidth / 2) - (font.width(modeText) / 2);
        graphics.drawString(this.font, modeText, leftPos + textX, topPos + 10, color, false);

        if (!isExtract) {
            int slotX = leftPos + 80;
            int slotY = topPos + 25;

            graphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xAA000000);
            graphics.drawCenteredString(this.font, "X", slotX + 8, slotY + 4, 0xFF0000);
            graphics.renderOutline(slotX, slotY, 16, 16, 0xFFFF0000);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY);
    }
}