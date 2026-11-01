package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableCraftButtonClickPayload;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class UncraftingTableScreen extends AbstractUncraftingScreen<UncraftingTableBlockEntity, UncraftingTableMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(UncraftEverything.MODID, "textures/gui/uncrafting_table_gui.png");

    public UncraftingTableScreen(UncraftingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        this.imageHeight = 184;
        this.inventoryLabelY = this.imageHeight - 94;

        super.init();

        this.leftPos = Math.max((width - imageWidth) / 2, (16 * 9) + SCROLLBAR_PADDING + SCROLLBAR_WIDTH);

        int buttonX = leftPos + (imageWidth - 64) - 20;
        int buttonY = topPos + 72;

        this.addRenderableWidget(Button.builder(Component.translatable("screen.uncrafteverything.uncraft"), this::onPressed).pos(buttonX, buttonY).size(64, 16).build());
    }

    private void onPressed(Button button) {
        UncraftingTableCraftButtonClickPayload payload = new UncraftingTableCraftButtonClickPayload(this.menu.blockEntity.getBlockPos(), hasShiftDown());
        UncraftingTableCraftButtonClickPayload.INSTANCE.sendToServer(payload);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        pGuiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        super.renderBg(pGuiGraphics, partialTick, mouseX, mouseY);
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        this.clearWidgets();
        this.init();

        this.renderExpRequired(pGuiGraphics, pMouseX, pMouseY);
        this.renderRecipeSelection(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderInputSlotOverlay(pGuiGraphics);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderExpRequired(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ResourceLocation icon = new ResourceLocation(UncraftEverything.MODID, "textures/gui/sprites/exp.png");
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.5f, 0.5f, 0.5f);
        guiGraphics.pose().translate((this.leftPos + imageWidth - 17) * 2, (this.topPos + 72) * 2, 0f);
        guiGraphics.blit(icon, 0, 0, 0, 0, 16, 16, 16, 16);
        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.75f, 0.75f, 0.75f);
        guiGraphics.pose().translate((this.leftPos + imageWidth - 12) * 1.3334f, (this.topPos + 82) * 1.3334f, 0f);
        this.drawCenteredWordWrapWithoutShadow(guiGraphics, this.font, Component.literal(this.menu.getExpAmount() + ""), 0, 0, 0xFF00AA00);
        guiGraphics.pose().popPose();

        if (mouseX >= (this.leftPos + imageWidth - 19) && mouseX <= (this.leftPos + imageWidth - 7) && mouseY >= this.topPos + 72 && mouseY <= this.topPos + 87){
            Component exp = Component.translatable("screen.uncrafteverything.exp_" + this.menu.getExpType().toLowerCase() + "_required",this.menu.getExpAmount());
            guiGraphics.renderTooltip(this.font, exp, mouseX, mouseY);
        }
    }
}
