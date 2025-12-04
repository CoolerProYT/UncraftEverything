package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.config.UncraftEverythingClientConfig;
import com.coolerpromc.uncrafteverything.screen.widget.ColorPickerWidget;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class UEClientConfigScreen extends AbstractScrollableScreen {
    public boolean autoMoveToInventory = UncraftEverythingClientConfig.autoMoveToInventory;
    public int noRecipeFoundColor = UncraftEverythingClientConfig.noRecipeFoundColor;
    public int noSuitableOutputSlotColor = UncraftEverythingClientConfig.noSuitableOutputSlotColor;
    public int notEnoughExpColor = UncraftEverythingClientConfig.notEnoughExpColor;
    public int notEnoughInputItemColor = UncraftEverythingClientConfig.notEnoughInputItemColor;
    public int notEmptyShulkerColor = UncraftEverythingClientConfig.notEmptyShulkerColor;
    public int restrictedByConfigColor = UncraftEverythingClientConfig.restrictedByConfigColor;
    public int damagedItemColor = UncraftEverythingClientConfig.damagedItemColor;
    public int enchantedItemColor = UncraftEverythingClientConfig.enchantedItemColor;
    public int lockedItemColor = UncraftEverythingClientConfig.lockedItemColor;
    public int progressionNotDefinedColor = UncraftEverythingClientConfig.progressionNotDefinedColor;

    private final int baseY = 30;
    private final Map<ColorPickerWidget, Pair<Integer, Integer>> widgets = new HashMap<>();

    private ButtonWidget moveToInventoryButton;
    private ButtonWidget cancelButton;
    private ButtonWidget saveButton;

    public UEClientConfigScreen(Text title) {
        super(title, 250);
        ColorPickerWidget noRecipeFoundButton = new ColorPickerWidget(0, (int) (baseY + 25 - scrollAmount), 0, noRecipeFoundColor, Text.translatable("screen.uncrafteverything.config.no_recipe_found"), this, this::setNoRecipeFoundColor);
        ColorPickerWidget noSuitableOutputSlotButton = new ColorPickerWidget(0, (int) (baseY + 50 - scrollAmount), 0, noSuitableOutputSlotColor, Text.translatable("screen.uncrafteverything.config.no_suitable_output_slot"), this, this::setNoSuitableOutputSlotColor);
        ColorPickerWidget notEnoughExpButton = new ColorPickerWidget(0, (int) (baseY + 75 - scrollAmount), 0, notEnoughExpColor, Text.translatable("screen.uncrafteverything.config.not_enough_exp"), this, this::setNotEnoughExpColor);
        ColorPickerWidget notEnoughInputItemButton = new ColorPickerWidget(0, (int) (baseY + 100 - scrollAmount), 0, notEnoughInputItemColor, Text.translatable("screen.uncrafteverything.config.not_enough_input_item"), this, this::setNotEnoughInputItemColor);
        ColorPickerWidget notEmptyShulkerButton = new ColorPickerWidget(0, (int) (baseY + 125 - scrollAmount), 0, notEmptyShulkerColor, Text.translatable("screen.uncrafteverything.config.not_empty_shulker"), this, this::setNotEmptyShulkerColor);
        ColorPickerWidget restrictedByConfigButton = new ColorPickerWidget(0, (int) (baseY + 150 - scrollAmount), 0, restrictedByConfigColor, Text.translatable("screen.uncrafteverything.config.restricted_by_config"), this, this::setRestrictedByConfigColor);
        ColorPickerWidget damagedItemButton = new ColorPickerWidget(0, (int) (baseY + 175 - scrollAmount), 0, damagedItemColor, Text.translatable("screen.uncrafteverything.config.damaged_item"), this, this::setDamagedItemColor);
        ColorPickerWidget enchantedItemButton = new ColorPickerWidget(0, (int) (baseY + 200 - scrollAmount), 0, enchantedItemColor, Text.translatable("screen.uncrafteverything.config.enchanted_item"), this, this::setEnchantedItemColor);
        ColorPickerWidget lockedItemButton = new ColorPickerWidget(0, (int) (baseY + 225 - scrollAmount), 0, lockedItemColor, Text.translatable("screen.uncrafteverything.config.locked_item"), this, this::setLockedItemColor);
        ColorPickerWidget progressionNotDefinedButton = new ColorPickerWidget(0, (int) (baseY + 250 - scrollAmount), 0, progressionNotDefinedColor, Text.translatable("screen.uncrafteverything.config.progression_not_defined"), this, this::setProgressionNotDefinedColor);

        widgets.put(noRecipeFoundButton, new Pair<>(noRecipeFoundColor, 25));
        widgets.put(noSuitableOutputSlotButton, new Pair<>(noSuitableOutputSlotColor, 50));
        widgets.put(notEnoughExpButton, new Pair<>(notEnoughExpColor, 75));
        widgets.put(notEnoughInputItemButton, new Pair<>(notEnoughInputItemColor, 100));
        widgets.put(notEmptyShulkerButton, new Pair<>(notEmptyShulkerColor, 125));
        widgets.put(restrictedByConfigButton, new Pair<>(restrictedByConfigColor, 150));
        widgets.put(damagedItemButton, new Pair<>(damagedItemColor, 175));
        widgets.put(enchantedItemButton, new Pair<>(enchantedItemColor, 200));
        widgets.put(lockedItemButton, new Pair<>(lockedItemColor, 225));
        widgets.put(progressionNotDefinedButton, new Pair<>(progressionNotDefinedColor, 250));
    }

    @Override
    protected void init() {
        int x = this.width / 2 + 10;
        int widgetWidth = this.width - x - 10;

        moveToInventoryButton = ButtonWidget.builder(Text.translatable("screen.uncrafteverything.config.move_to_inventory_" + autoMoveToInventory), button -> {
            autoMoveToInventory = !autoMoveToInventory;
            button.setMessage(Text.translatable("screen.uncrafteverything.config.move_to_inventory_" + autoMoveToInventory));
        }).dimensions(x, (int) (baseY - scrollAmount), widgetWidth, 20).build();
        this.addDrawableChild(moveToInventoryButton);

        Pair<ColorPickerWidget, Pair<Integer, Integer>> visible = null;

        for (Map.Entry<ColorPickerWidget, Pair<Integer, Integer>> entry : widgets.entrySet()){
            entry.getKey().setHasOverlay(false);
            if (entry.getKey().isVisible()){
                visible = new Pair<>(entry.getKey(), entry.getValue());
            }
            else{
                this.initColorPicker(entry.getKey(), entry.getValue().getFirst(), widgetWidth, entry.getValue().getSecond());
            }
        }

        if (visible != null){
            this.initColorPicker(visible.getFirst(), visible.getSecond().getFirst(), widgetWidth, visible.getSecond().getSecond());

            for (ColorPickerWidget widget : widgets.keySet()){
                widget.setHasOverlay(true);
            }
        }

        cancelButton = ButtonWidget.builder(Text.translatable("screen.uncrafteverything.cancel"), this::pressCancelButton).dimensions(this.width / 2 - 210, (this.height - 45) + 15, 200, 20).build();
        saveButton = ButtonWidget.builder(Text.translatable("screen.uncrafteverything.save"), this::pressSaveButton).dimensions(this.width / 2 + 10, (this.height - 45) + 15, 200, 20).build();
        this.addDrawableChild(cancelButton);
        this.addDrawableChild(saveButton);
    }

    @Override
    public void renderBackground(@NotNull DrawContext guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderDarkening(guiGraphics);
        applyBlur(guiGraphics);
        renderSeparator(guiGraphics);
        renderScrollbar(guiGraphics, 70);
    }

    @Override
    public void render(@NotNull DrawContext pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int scissorTop = 25;
        int scissorBottom = this.height - 45;
        pGuiGraphics.enableScissor(0, scissorTop, this.width, scissorBottom);

        int x = 10;
        int textWidth = this.width / 2 - 10;
        int baseY = 30;

        Text moveToInventory = Text.translatable("screen.uncrafteverything.config.move_to_inventory");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, moveToInventory, x, (int) (baseY - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Text noRecipeFound = Text.translatable("screen.uncrafteverything.config.no_recipe_found");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, noRecipeFound, x, (int) (baseY + 25 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Text noSuitableOutputSlot = Text.translatable("screen.uncrafteverything.config.no_suitable_output_slot");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, noSuitableOutputSlot, x, (int) (baseY + 50 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Text notEnoughExpSlot = Text.translatable("screen.uncrafteverything.config.not_enough_exp");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, notEnoughExpSlot, x, (int) (baseY + 75 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Text notEnoughInputItemSlot = Text.translatable("screen.uncrafteverything.config.not_enough_input_item");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, notEnoughInputItemSlot, x, (int) (baseY + 100 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Text notEmptyShulkerSlot = Text.translatable("screen.uncrafteverything.config.not_empty_shulker");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, notEmptyShulkerSlot, x, (int) (baseY + 125 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Text restrictedByConfigSlot = Text.translatable("screen.uncrafteverything.config.restricted_by_config");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, restrictedByConfigSlot, x, (int) (baseY + 150 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Text damagedItemSlot = Text.translatable("screen.uncrafteverything.config.damaged_item");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, damagedItemSlot, x, (int) (baseY + 175 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Text enchantedItemSlot = Text.translatable("screen.uncrafteverything.config.enchanted_item");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, enchantedItemSlot, x, (int) (baseY + 200 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Text lockedItemSlot = Text.translatable("screen.uncrafteverything.config.locked_item");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, lockedItemSlot, x, (int) (baseY + 225 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Text progressionNotDefinedSlot = Text.translatable("screen.uncrafteverything.config.progression_not_defined");
        pGuiGraphics.drawWrappedTextWithShadow(this.textRenderer, progressionNotDefinedSlot, x, (int) (baseY + 250 - scrollAmount + (this.textRenderer.fontHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        pGuiGraphics.disableScissor();

        pGuiGraphics.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.uncrafteverything.uncraft_everything_client_config"), this.width / 2, (23 - this.textRenderer.fontHeight) / 2, 0xFFFFFFFF);
        cancelButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        saveButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        renderButtonTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected int scrollBarX() {
        return this.width - 6;
    }

    @Override
    protected int scrollBarY() {
        int scrollBarHeight = Math.max(10, (int) ((this.height - 70) * (this.height - 70) / (double) contentHeight));
        return (int) (25 + (scrollAmount / getMaxScroll()) * (this.height - 70 - scrollBarHeight));
    }

    @Override
    protected int scrollerHeight() {
        return Math.max(10, (int) ((this.height - 70) * (this.height - 70) / (double) contentHeight));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        boolean visible = false;
        for (ColorPickerWidget x : widgets.keySet()){
            if (x.isVisible()){
                visible = true;
                break;
            }
        }
        if (!visible){
            boolean result =super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            this.redraw();
            return result;
        }
        return true;
    }

    @Override
    protected int getMaxScroll() {
        return Math.max(0, contentHeight - (height - 100)); // 100 for header and footer space
    }

    @Override
    public boolean mouseDragged(Click buttonEvent, double dragX, double dragY) {
        for (ColorPickerWidget x : widgets.keySet()){
            if (x.isVisible()){
                return x.mouseDragged(buttonEvent, dragX, dragY);
            }
        }
        return super.mouseDragged(buttonEvent, dragX, dragY);
    }

    @Override
    public boolean mouseClicked(Click buttonEvent, boolean doubled) {
        int scrollTop = 25;
        int scrollBottom = this.height - 45;

        boolean inScrollArea = buttonEvent.y() >= scrollTop && buttonEvent.y() <= scrollBottom;

        ColorPickerWidget visible = null;

        for (ColorPickerWidget x : widgets.keySet()){
            if (x.isVisible()){
                visible = x;
                break;
            }
        }

        if (!inScrollArea && visible == null) {
            if (!saveButton.isMouseOver(buttonEvent.x(), buttonEvent.y()) && !cancelButton.isMouseOver(buttonEvent.x(), buttonEvent.y())) {
                return false;
            }
            else{
                if (cancelButton.isMouseOver(buttonEvent.x(), buttonEvent.y())){
                    return cancelButton.mouseClicked(buttonEvent, doubled);
                }
                return saveButton.mouseClicked(buttonEvent, doubled);
            }
        }

        if (visible != null){
            return visible.mouseClicked(buttonEvent, doubled);
        }
        else{
            for (ColorPickerWidget x : widgets.keySet()){
                boolean result = x.mouseClicked(buttonEvent, doubled);
                if (x.isVisible()){
                    return result;
                }
            }
        }

        return super.mouseClicked(buttonEvent, doubled);
    }

    public void redraw(){
        this.clearChildren();
        this.init();
    }

    private void initColorPicker(ColorPickerWidget colorPickerWidget, int color, int widgetWidth, int yOffset){
        colorPickerWidget.setColor(color);
        colorPickerWidget.setButtonWidth(widgetWidth);
        colorPickerWidget.setY((int) (baseY + yOffset - scrollAmount));
        colorPickerWidget.setHeight((int) (ColorPickerWidget.PICKER_SIZE + 60 + scrollAmount));
        this.addDrawableChild(colorPickerWidget);
    }

    private void pressSaveButton(ButtonWidget button){
        UncraftEverythingClientConfig.autoMoveToInventory = this.autoMoveToInventory;
        UncraftEverythingClientConfig.noRecipeFoundColor = this.noRecipeFoundColor;
        UncraftEverythingClientConfig.noSuitableOutputSlotColor = this.noSuitableOutputSlotColor;
        UncraftEverythingClientConfig.notEnoughExpColor = this.notEnoughExpColor;
        UncraftEverythingClientConfig.notEnoughInputItemColor = this.notEnoughInputItemColor;
        UncraftEverythingClientConfig.notEmptyShulkerColor = this.notEmptyShulkerColor;
        UncraftEverythingClientConfig.restrictedByConfigColor = this.restrictedByConfigColor;
        UncraftEverythingClientConfig.damagedItemColor = this.damagedItemColor;
        UncraftEverythingClientConfig.enchantedItemColor = this.enchantedItemColor;
        UncraftEverythingClientConfig.lockedItemColor = this.lockedItemColor;
        UncraftEverythingClientConfig.progressionNotDefinedColor = this.progressionNotDefinedColor;
        UncraftEverythingClientConfig.save();
        close();
    }

    private void pressCancelButton(ButtonWidget button){
        close();
    }

    protected void renderSeparator(DrawContext guiGraphics){
        Identifier header = this.client.world == null ? Screen.HEADER_SEPARATOR_TEXTURE : Screen.INWORLD_HEADER_SEPARATOR_TEXTURE;
        Identifier footer = this.client.world == null ? Screen.FOOTER_SEPARATOR_TEXTURE : Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE;
        guiGraphics.drawTexture(RenderPipelines.GUI_TEXTURED, header, 0, 25 - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        guiGraphics.drawTexture(RenderPipelines.GUI_TEXTURED, footer, 0, this.height - 45, 0.0F, 0.0F, this.width, 2, 32, 2);
    }

    private void renderButtonTooltip(DrawContext guiGraphics, int mouseX, int mouseY){
        if (moveToInventoryButton.isHovered()){
            List<Text> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.move_to_inventory"),
                    valueInfo("tooltip.uncrafteverything.config.true", "tooltip.uncrafteverything.config.auto_move_true"),
                    Text.empty(),
                    valueInfo("tooltip.uncrafteverything.config.false", "tooltip.uncrafteverything.config.auto_move_false")
            );
            guiGraphics.drawTooltip(this.textRenderer, tooltip, Optional.empty(), mouseX, mouseY);
        }
    }

    private Text title(String title){
        return Text.translatable(title).formatted(Formatting.BLUE);
    }

    private Text valueInfo(String value, String info){
        return Text.translatable(value).append(": ").formatted(Formatting.AQUA).append(Text.translatable(info).formatted(Formatting.GRAY));
    }

    private Text description(String desc){
        return Text.translatable(desc).formatted(Formatting.GRAY);
    }

    private void setNoRecipeFoundColor(int noRecipeFoundColor, ColorPickerWidget button) {
        this.noRecipeFoundColor = noRecipeFoundColor;
        this.widgets.put(button, new Pair<>(noRecipeFoundColor, 25));
    }

    private void setNoSuitableOutputSlotColor(int noSuitableOutputSlotColor, ColorPickerWidget button) {
        this.noSuitableOutputSlotColor = noSuitableOutputSlotColor;
        this.widgets.put(button, new Pair<>(noSuitableOutputSlotColor, 50));
    }

    public void setNotEnoughExpColor(int notEnoughExpColor, ColorPickerWidget button) {
        this.notEnoughExpColor = notEnoughExpColor;
        this.widgets.put(button, new Pair<>(notEnoughExpColor, 75));
    }

    public void setNotEnoughInputItemColor(int notEnoughInputItemColor, ColorPickerWidget button) {
        this.notEnoughInputItemColor = notEnoughInputItemColor;
        this.widgets.put(button, new Pair<>(notEnoughInputItemColor, 100));
    }

    public void setNotEmptyShulkerColor(int notEmptyShulkerColor, ColorPickerWidget button) {
        this.notEmptyShulkerColor = notEmptyShulkerColor;
        this.widgets.put(button, new Pair<>(notEmptyShulkerColor, 125));
    }

    public void setRestrictedByConfigColor(int restrictedByConfigColor, ColorPickerWidget button) {
        this.restrictedByConfigColor = restrictedByConfigColor;
        this.widgets.put(button, new Pair<>(restrictedByConfigColor, 150));
    }

    public void setDamagedItemColor(int damagedItemColor, ColorPickerWidget button) {
        this.damagedItemColor = damagedItemColor;
        this.widgets.put(button, new Pair<>(damagedItemColor, 175));
    }

    public void setEnchantedItemColor(int enchantedItemColor, ColorPickerWidget button) {
        this.enchantedItemColor = enchantedItemColor;
        this.widgets.put(button, new Pair<>(enchantedItemColor, 200));
    }

    public void setLockedItemColor(int lockedItemColor, ColorPickerWidget button) {
        this.lockedItemColor = lockedItemColor;
        this.widgets.put(button, new Pair<>(lockedItemColor, 225));
    }

    public void setProgressionNotDefinedColor(int progressionNotDefinedColor, ColorPickerWidget button) {
        this.progressionNotDefinedColor = progressionNotDefinedColor;
        this.widgets.put(button, new Pair<>(progressionNotDefinedColor, 250));
    }
}