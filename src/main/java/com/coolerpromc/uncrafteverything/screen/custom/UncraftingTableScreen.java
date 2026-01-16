package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableCraftButtonClickPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.NotNull;

public class UncraftingTableScreen extends AbstractUncraftingScreen<UncraftingTableBlockEntity, UncraftingTableMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(UncraftEverything.MODID, "textures/gui/uncrafting_table_gui.png");

    public UncraftingTableScreen(UncraftingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 184);
    }

    @Override
    protected void init() {
        this.inventoryLabelY = this.imageHeight - 94;

        super.init();

        this.leftPos = Math.max((width - imageWidth) / 2, (16 * 9) + SCROLLBAR_PADDING + SCROLLBAR_WIDTH);

        int buttonX = leftPos + (imageWidth - 64) - 20;
        int buttonY = topPos + 72;

        this.addRenderableWidget(Button.builder(Component.translatable("screen.uncrafteverything.uncraft"), this::onPressed).pos(buttonX, buttonY).size(64, 16).build());
    }

    private void onPressed(Button button) {
        UncraftingTableCraftButtonClickPayload payload = new UncraftingTableCraftButtonClickPayload(this.menu.blockEntity.getBlockPos(), hasShift());
        ClientPacketDistributor.sendToServer(payload);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float partialTick, int mouseX, int mouseY) {
        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        super.renderBg(pGuiGraphics, partialTick, mouseX, mouseY);
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.clearWidgets();
        this.init();

        this.renderExpRequired(pGuiGraphics, pMouseX, pMouseY);
        this.renderRecipeSelection(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        super.renderContents(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderInputSlotOverlay(pGuiGraphics);
        super.renderCarriedItem(pGuiGraphics, pMouseX, pMouseY);
        super.renderSnapbackItem(pGuiGraphics);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderExpRequired(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Identifier icon = Identifier.fromNamespaceAndPath(UncraftEverything.MODID, "textures/gui/sprites/exp.png");
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(0.5f, 0.5f);
        guiGraphics.pose().translate((this.leftPos + imageWidth - 17) * 2, (this.topPos + 72) * 2);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, icon, 0, 0, 0, 0, 16, 16, 16, 16);
        guiGraphics.pose().popMatrix();

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(0.75f, 0.75f);
        guiGraphics.pose().translate((this.leftPos + imageWidth - 12) * 1.3334f, (this.topPos + 82) * 1.3334f);
        this.drawCenteredWordWrapWithoutShadow(guiGraphics, this.font, Component.literal(this.menu.getExpAmount() + ""), 0, 0, 0xFF00AA00);
        guiGraphics.pose().popMatrix();

        if (mouseX >= (this.leftPos + imageWidth - 19) && mouseX <= (this.leftPos + imageWidth - 7) && mouseY >= this.topPos + 72 && mouseY <= this.topPos + 87){
            Component exp = Component.translatable("screen.uncrafteverything.exp_" + this.menu.getExpType().toLowerCase() + "_required",this.menu.getExpAmount());
            guiGraphics.setTooltipForNextFrame(this.font, exp, mouseX, mouseY);
        }
    }
}
