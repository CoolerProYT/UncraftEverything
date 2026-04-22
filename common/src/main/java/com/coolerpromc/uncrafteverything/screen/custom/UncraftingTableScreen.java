package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.networking.ServerBoundUncraftingTableCraftButtonClickPayload;
import com.coolerpromc.uncrafteverything.platform.Services;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class UncraftingTableScreen extends AbstractUncraftingScreen<UncraftingTableBlockEntity, UncraftingTableMenu> {
    private static final Identifier TEXTURE = Constants.id("textures/gui/uncrafting_table_gui.png");

    public UncraftingTableScreen(UncraftingTableMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176, 184);
    }

    @Override
    protected void init() {
        this.inventoryLabelY = this.imageHeight - 94;

        super.init();

        this.leftPos = Math.max((width - imageWidth) / 2, (16 * 9) + SCROLLBAR_PADDING + SCROLLBAR_WIDTH);

        int buttonX = this.leftPos + (imageWidth - 64) - 20;
        int buttonY = this.topPos + 72;

        this.addRenderableWidget(Button.builder(Component.translatable("screen.uncrafteverything.uncraft"), this::onPressed).pos(buttonX, buttonY).size(64, 16).build());
    }

    private void onPressed(Button button) {
        ServerBoundUncraftingTableCraftButtonClickPayload payload = new ServerBoundUncraftingTableCraftButtonClickPayload(this.menu.blockEntity.getBlockPos(), hasShift());
        Services.NETWORK.sendToServer(payload);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

        this.clearWidgets();
        this.init();

        this.renderExpRequired(context, mouseX, mouseY);
        this.renderRecipeSelection(context, mouseX, mouseY, delta);
        super.extractContents(context, mouseX, mouseY, delta);
        this.renderInputSlotOverlay(context);
        super.extractCarriedItem(context, mouseX, mouseY);
        super.extractSnapbackItem(context);
        this.extractTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void renderExpRequired(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        Identifier icon = Constants.id("textures/gui/sprites/exp.png");
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

        if (mouseX >= (this.leftPos + imageWidth - 19) && mouseX <= (this.leftPos + imageWidth - 7) && mouseY >= this.topPos + 72 && mouseY <= this.topPos + 87) {
            Component exp = Component.translatable("screen.uncrafteverything.exp_" + this.menu.getExpType().toLowerCase() + "_required", this.menu.getExpAmount());
            guiGraphics.setTooltipForNextFrame(this.font, exp, mouseX, mouseY);
        }
    }

    public int getX() {
        return this.leftPos;
    }
}
