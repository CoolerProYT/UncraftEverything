package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableCraftButtonClickPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class UncraftingTableScreen extends AbstractUncraftingScreen<UncraftingTableBlockEntity, UncraftingTableMenu> {
    private static final Identifier TEXTURE = Identifier.of(UncraftEverything.MODID, "textures/gui/uncrafting_table_gui.png");

    public UncraftingTableScreen(UncraftingTableMenu handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        this.backgroundHeight = 184;
        this.playerInventoryTitleY = this.backgroundHeight - 94;

        super.init();

        this.x = Math.max((width - backgroundWidth) / 2, (16 * 9) + SCROLLBAR_PADDING + SCROLLBAR_WIDTH);

        int buttonX = this.x + (backgroundWidth - 64) - 20;
        int buttonY = this.y + 72;

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.uncrafteverything.uncraft"), this::onPressed).position(buttonX, buttonY).size(64, 16).build());
    }

    private void onPressed(ButtonWidget button) {
        UncraftingTableCraftButtonClickPayload payload = new UncraftingTableCraftButtonClickPayload(this.handler.blockEntity.getPos(), hasShift());
        ClientPlayNetworking.send(payload);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight, 256, 256);
        super.drawBackground(context, delta, mouseX, mouseY);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.clearChildren();
        this.init();

        this.renderExpRequired(context, mouseX, mouseY);
        this.renderRecipeSelection(context, mouseX, mouseY, delta);
        super.renderMain(context, mouseX, mouseY, delta);
        this.renderInputSlotOverlay(context);
        super.renderCursorStack(context, mouseX, mouseY);
        super.renderLetGoTouchStack(context);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void renderExpRequired(DrawContext guiGraphics, int mouseX, int mouseY) {
        Identifier icon = Identifier.of(UncraftEverything.MODID, "textures/gui/sprites/exp.png");
        guiGraphics.getMatrices().pushMatrix();
        guiGraphics.getMatrices().scale(0.5f, 0.5f);
        guiGraphics.getMatrices().translate((this.x + backgroundWidth - 17) * 2, (this.y + 72) * 2);
        guiGraphics.drawTexture(RenderPipelines.GUI_TEXTURED, icon, 0, 0, 0, 0, 16, 16, 16, 16);
        guiGraphics.getMatrices().popMatrix();

        guiGraphics.getMatrices().pushMatrix();
        guiGraphics.getMatrices().scale(0.75f, 0.75f);
        guiGraphics.getMatrices().translate((this.x + backgroundWidth - 12) * 1.3334f, (this.y + 82) * 1.3334f);
        this.drawCenteredWordWrapWithoutShadow(guiGraphics, this.textRenderer, Text.literal(this.handler.getExpAmount() + ""), 0, 0, 0xFF00AA00);
        guiGraphics.getMatrices().popMatrix();

        if (mouseX >= (this.x + backgroundWidth - 19) && mouseX <= (this.x + backgroundWidth - 7) && mouseY >= this.y + 72 && mouseY <= this.y + 87) {
            Text exp = Text.translatable("screen.uncrafteverything.exp_" + this.handler.getExpType().toLowerCase() + "_required", this.handler.getExpAmount());
            guiGraphics.drawTooltip(this.textRenderer, exp, mouseX, mouseY);
        }
    }

    public int getX() {
        return this.x;
    }
}
