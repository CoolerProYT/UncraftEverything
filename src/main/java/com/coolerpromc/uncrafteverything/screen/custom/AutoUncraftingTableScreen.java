package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.blockentity.custom.AutoUncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.networking.AmountToAddPayload;
import com.coolerpromc.uncrafteverything.networking.ExpTransferPayload;
import com.coolerpromc.uncrafteverything.networking.SelectedIndexSyncPayload;
import com.coolerpromc.uncrafteverything.networking.TypeChangePayload;
import com.coolerpromc.uncrafteverything.screen.widget.AmountWidget;
import com.coolerpromc.uncrafteverything.screen.widget.RecipeSelectionButton;
import com.coolerpromc.uncrafteverything.screen.widget.TypeWidget;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AutoUncraftingTableScreen extends AbstractUncraftingScreen<AutoUncraftingTableBlockEntity, AutoUncraftingTableMenu> {
    private static final Identifier TEXTURE = Identifier.of(UncraftEverything.MODID, "textures/gui/uncrafting_table_gui.png");
    private static final Identifier EXPERIENCE_BAR_BACKGROUND_SPRITE = Identifier.ofVanilla("hud/experience_bar_background");
    private static final Identifier EXPERIENCE_BAR_PROGRESS_SPRITE = Identifier.ofVanilla("hud/experience_bar_progress");

    private AmountWidget amountWidget;
    private TypeWidget typeWidget;
    private ButtonWidget removeExp;
    private ButtonWidget addExp;

    public AutoUncraftingTableScreen(AutoUncraftingTableMenu menu, PlayerInventory playerInventory, Text title) {
        super(menu, playerInventory, title);
        this.page = this.getScreenHandler().getPage();
        this.selectedRecipe = this.getScreenHandler().getIndex();
    }

    @Override
    protected void init() {
        this.backgroundHeight = 184;
        this.playerInventoryTitleY = 10000;

        super.init();

        this.x = Math.max((width - backgroundWidth) / 2, (16 * 9) + SCROLLBAR_PADDING + SCROLLBAR_WIDTH);

        amountWidget = new AmountWidget(this.textRenderer, this.width / 2 - 31, this.y + 76, 30, 14, Text.empty(), this.handler.getAmountToAdd());
        this.addDrawableChild(amountWidget);

        typeWidget = new TypeWidget(this.textRenderer, this.width / 2, this.y + 76, 30, 14, Text.empty(), this.handler.getTypeToAdd());
        this.addDrawableChild(typeWidget);

        removeExp = ButtonWidget.builder(Text.literal("-"), this::remove).position(this.x + 12, this.y + 91).size(9, 9).build();
        this.addDrawableChild(removeExp);
        addExp = ButtonWidget.builder(Text.literal("+"), this::add).position(this.x + 155, this.y + 91).size(9, 9).build();
        this.addDrawableChild(addExp);
    }

    private void remove(ButtonWidget button){
        ClientPlayNetworking.send(new ExpTransferPayload(this.handler.blockEntity.getPos(), -this.handler.getAmountToAdd(), this.handler.getTypeToAdd()));
    }

    private void add(ButtonWidget button){
        ClientPlayNetworking.send(new ExpTransferPayload(this.handler.blockEntity.getPos(), this.handler.getAmountToAdd(), this.handler.getTypeToAdd()));
    }

    @Override
    protected void drawBackground(DrawContext pGuiGraphics, float partialTick, int mouseX, int mouseY) {
        pGuiGraphics.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, this.x, this.y, 0, 0, backgroundWidth, backgroundHeight, 256, 256);
        this.renderExpStored(pGuiGraphics, mouseX, mouseY);
        super.drawBackground(pGuiGraphics, partialTick, mouseX, mouseY);
    }

    private void renderExpStored(DrawContext guiGraphics, int mouseX, int mouseY){
        int k = this.handler.blockEntity.getXpNeededForNextLevel();
        if (k > 0) {
            int l = this.handler.getExpProgress();
            guiGraphics.drawGuiTexture(RenderPipelines.GUI_TEXTURED, EXPERIENCE_BAR_BACKGROUND_SPRITE, this.x + 25, this.y + 93, 125, 5);
            if (l > 0) {
                guiGraphics.drawGuiTexture(RenderPipelines.GUI_TEXTURED, EXPERIENCE_BAR_PROGRESS_SPRITE, 125, 5, 0, 0, this.x + 25, this.y + 93, l, 5);
            }
        }

        if (this.handler.getExpLevels() > 0){
            Text component = Text.translatable("gui.experience.level", this.handler.getExpLevels());
            int i = (this.width - textRenderer.getWidth(component)) / 2;
            int j = this.y + 92;
            guiGraphics.drawText(textRenderer, component, i + 1, j, -16777216, false);
            guiGraphics.drawText(textRenderer, component, i - 1, j, -16777216, false);
            guiGraphics.drawText(textRenderer, component, i, j + 1, -16777216, false);
            guiGraphics.drawText(textRenderer, component, i, j - 1, -16777216, false);
            guiGraphics.drawText(textRenderer, component, i, j, -8323296, false);
        }

        if (mouseX >= (this.x + 25) && mouseX <= (this.x + 150) && mouseY >= this.y + 93 && mouseY <= this.y + 98){
            List<Text> tooltip = new ArrayList<>();
            tooltip.add(Text.translatable("screen.uncrafteverything.tooltip.recipe_require").formatted(Formatting.BLUE));
            tooltip.add(Text.translatable("screen.uncrafteverything.exp_" + this.handler.getExpType().toLowerCase() + "_required",this.handler.getExpAmount()).formatted(Formatting.GRAY));
            tooltip.add(Text.empty());
            tooltip.add(Text.translatable("screen.uncrafteverything.tooltip.exp_stored").formatted(Formatting.BLUE));
            tooltip.add(Text.literal(this.handler.getExpLevels() + " ").append(Text.translatable("tooltip.uncrafteverything.config.level")).formatted(Formatting.GRAY));
            tooltip.add(Text.literal(this.handler.getExpPoints() + " ").append(Text.translatable("tooltip.uncrafteverything.config.point")).formatted(Formatting.GRAY));
            guiGraphics.drawTooltip(this.textRenderer, tooltip, Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    public void render(@NotNull DrawContext pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.clearChildren();
        this.init();
        this.page = this.getScreenHandler().getPage();
        this.selectedRecipe = this.getScreenHandler().getIndex();
        this.renderRecipeSelection(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        super.renderMain(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderInputSlotOverlay(pGuiGraphics);
        super.renderCursorStack(pGuiGraphics, pMouseX, pMouseY);
        super.renderLetGoTouchStack(pGuiGraphics);
        this.drawMouseoverTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderExpRequired(DrawContext guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    protected void chooseRecipe(RecipeSelectionButton button, int index) {
        super.chooseRecipe(button, index);
        ClientPlayNetworking.send(new SelectedIndexSyncPayload(this.handler.blockEntity.getPos(), index));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollDelta) {
        if (amountWidget.isHovered()){
            if (scrollDelta == 1.0d){
                if (this.handler.getAmountToAdd() < 100){
                    ClientPlayNetworking.send(new AmountToAddPayload(this.handler.blockEntity.getPos(), this.handler.getAmountToAdd() + 1));
                }
                else{
                    ClientPlayNetworking.send(new AmountToAddPayload(this.handler.blockEntity.getPos(), 1));
                }
            }
            else if (scrollDelta == -1.0d){
                if (this.handler.getAmountToAdd() > 1){
                    ClientPlayNetworking.send(new AmountToAddPayload(this.handler.blockEntity.getPos(), this.handler.getAmountToAdd() - 1));
                }
                else{
                    ClientPlayNetworking.send(new AmountToAddPayload(this.handler.blockEntity.getPos(), 100));
                }
            }
            return true;
        }
        if (typeWidget.isHovered()){
            if (scrollDelta == 1.0d || scrollDelta == -1.0d){
                ClientPlayNetworking.send(new TypeChangePayload(this.handler.blockEntity.getPos(), this.handler.getTypeToAdd().invert()));
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollDelta);
    }

    @Override
    protected void drawMouseoverTooltip(@NotNull DrawContext guiGraphics, int x, int y) {
        super.drawMouseoverTooltip(guiGraphics, x, y);

        int maxWidth = 220; // adjust if needed

        if (removeExp.isHovered()) {
            List<OrderedText> tooltip = new ArrayList<>();
            tooltip.addAll(textRenderer.wrapLines(Text.translatable("screen.uncrafteverything.tooltip.remove_exp"), maxWidth));
            tooltip.addAll(textRenderer.wrapLines(Text.translatable("screen.uncrafteverything.tooltip.level_detail").formatted(Formatting.GRAY), maxWidth));
            tooltip.addAll(textRenderer.wrapLines(Text.translatable("screen.uncrafteverything.tooltip.level_detail_2").formatted(Formatting.DARK_GRAY), maxWidth));
            tooltip.addAll(textRenderer.wrapLines(Text.translatable("screen.uncrafteverything.tooltip.level_detail_3").formatted(Formatting.DARK_GRAY), maxWidth));
            guiGraphics.drawTooltip(textRenderer, tooltip, HoveredTooltipPositioner.INSTANCE, x, y, false);
        }

        if (addExp.isHovered()) {
            List<OrderedText> tooltip = new ArrayList<>();
            tooltip.addAll(textRenderer.wrapLines(Text.translatable("screen.uncrafteverything.tooltip.add_exp"), maxWidth));
            tooltip.addAll(textRenderer.wrapLines(Text.translatable("screen.uncrafteverything.tooltip.level_detail").formatted(Formatting.GRAY), maxWidth));
            tooltip.addAll(textRenderer.wrapLines(Text.translatable("screen.uncrafteverything.tooltip.level_detail_2").formatted(Formatting.DARK_GRAY), maxWidth));
            tooltip.addAll(textRenderer.wrapLines(Text.translatable("screen.uncrafteverything.tooltip.level_detail_3").formatted(Formatting.DARK_GRAY), maxWidth));
            guiGraphics.drawTooltip(textRenderer, tooltip, HoveredTooltipPositioner.INSTANCE, x, y, false);
        }
    }

}