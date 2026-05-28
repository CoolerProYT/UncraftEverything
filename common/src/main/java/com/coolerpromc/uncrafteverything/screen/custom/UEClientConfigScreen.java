package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.config.UncraftEverythingClientConfig;
import com.coolerpromc.uncrafteverything.platform.Services;
import com.coolerpromc.uncrafteverything.screen.widget.ColorPickerWidget;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UEClientConfigScreen extends AbstractScrollableScreen {
    public boolean autoMoveToInventory = UncraftEverythingClientConfig.CONFIG.autoMoveToInventory();
    public int noRecipeFoundColor = UncraftEverythingClientConfig.CONFIG.noRecipeFoundColor();
    public int noSuitableOutputSlotColor = UncraftEverythingClientConfig.CONFIG.noSuitableOutputSlotColor();
    public int notEnoughExpColor = UncraftEverythingClientConfig.CONFIG.notEnoughExpColor();
    public int notEnoughInputItemColor = UncraftEverythingClientConfig.CONFIG.notEnoughInputItemColor();
    public int notEmptyShulkerColor = UncraftEverythingClientConfig.CONFIG.notEmptyShulkerColor();
    public int restrictedByConfigColor = UncraftEverythingClientConfig.CONFIG.restrictedByConfigColor();
    public int damagedItemColor = UncraftEverythingClientConfig.CONFIG.damagedItemColor();
    public int enchantedItemColor = UncraftEverythingClientConfig.CONFIG.enchantedItemColor();
    public int lockedItemColor = UncraftEverythingClientConfig.CONFIG.lockedItemColor();
    public int progressionNotDefinedColor = UncraftEverythingClientConfig.CONFIG.progressionNotDefinedColor();

    private final int baseY = 30;
    private final Map<ColorPickerWidget, Pair<Integer, Integer>> widgets = new HashMap<>();

    private Button moveToInventoryButton;
    private Button cancelButton;
    private Button saveButton;

    public UEClientConfigScreen(Component title) {
        super(title, 250);
        int y = 25;

        ColorPickerWidget noRecipeFoundButton = new ColorPickerWidget(0, 0, 0, noRecipeFoundColor, Component.translatable("screen.uncrafteverything.config.no_recipe_found"), this, this::setNoRecipeFoundColor);
        widgets.put(noRecipeFoundButton, new Pair<>(noRecipeFoundColor, y)); y += 25;

        ColorPickerWidget noSuitableOutputSlotButton = new ColorPickerWidget(0, 0, 0, noSuitableOutputSlotColor, Component.translatable("screen.uncrafteverything.config.no_suitable_output_slot"), this, this::setNoSuitableOutputSlotColor);
        widgets.put(noSuitableOutputSlotButton, new Pair<>(noSuitableOutputSlotColor, y)); y += 25;

        ColorPickerWidget notEnoughExpButton = new ColorPickerWidget(0, 0, 0, notEnoughExpColor, Component.translatable("screen.uncrafteverything.config.not_enough_exp"), this, this::setNotEnoughExpColor);
        widgets.put(notEnoughExpButton, new Pair<>(notEnoughExpColor, y)); y += 25;

        ColorPickerWidget notEnoughInputItemButton = new ColorPickerWidget(0, 0, 0, notEnoughInputItemColor, Component.translatable("screen.uncrafteverything.config.not_enough_input_item"), this, this::setNotEnoughInputItemColor);
        widgets.put(notEnoughInputItemButton, new Pair<>(notEnoughInputItemColor, y)); y += 25;

        ColorPickerWidget notEmptyShulkerButton = new ColorPickerWidget(0, 0, 0, notEmptyShulkerColor, Component.translatable("screen.uncrafteverything.config.not_empty_shulker"), this, this::setNotEmptyShulkerColor);
        widgets.put(notEmptyShulkerButton, new Pair<>(notEmptyShulkerColor, y)); y += 25;

        ColorPickerWidget restrictedByConfigButton = new ColorPickerWidget(0, 0, 0, restrictedByConfigColor, Component.translatable("screen.uncrafteverything.config.restricted_by_config"), this, this::setRestrictedByConfigColor);
        widgets.put(restrictedByConfigButton, new Pair<>(restrictedByConfigColor, y)); y += 25;

        ColorPickerWidget damagedItemButton = new ColorPickerWidget(0, 0, 0, damagedItemColor, Component.translatable("screen.uncrafteverything.config.damaged_item"), this, this::setDamagedItemColor);
        widgets.put(damagedItemButton, new Pair<>(damagedItemColor, y)); y += 25;

        ColorPickerWidget enchantedItemButton = new ColorPickerWidget(0, 0, 0, enchantedItemColor, Component.translatable("screen.uncrafteverything.config.enchanted_item"), this, this::setEnchantedItemColor);
        widgets.put(enchantedItemButton, new Pair<>(enchantedItemColor, y)); y += 25;

        ColorPickerWidget lockedItemButton = new ColorPickerWidget(0, 0, 0, lockedItemColor, Component.translatable("screen.uncrafteverything.config.locked_item"), this, this::setLockedItemColor);
        widgets.put(lockedItemButton, new Pair<>(lockedItemColor, y)); y += 25;

        ColorPickerWidget progressionNotDefinedButton = new ColorPickerWidget(0, 0, 0, progressionNotDefinedColor, Component.translatable("screen.uncrafteverything.config.progression_not_defined"), this, this::setProgressionNotDefinedColor);
        widgets.put(progressionNotDefinedButton, new Pair<>(progressionNotDefinedColor, y));
    }

    @Override
    protected void init() {
        int x = this.width / 2 + 10;
        int widgetWidth = this.width - x - 10;

        moveToInventoryButton = Button.builder(Component.translatable("screen.uncrafteverything.config.move_to_inventory_" + autoMoveToInventory), button -> {
            autoMoveToInventory = !autoMoveToInventory;
            button.setMessage(Component.translatable("screen.uncrafteverything.config.move_to_inventory_" + autoMoveToInventory));
        }).bounds(x, (int) (baseY - scrollAmount), widgetWidth, 20).build();
        this.addRenderableWidget(moveToInventoryButton);

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

        cancelButton = Button.builder(Component.translatable("screen.uncrafteverything.cancel"), this::pressCancelButton).bounds(this.width / 2 - 210, (this.height - 45) + 15, 200, 20).build();
        saveButton = Button.builder(Component.translatable("screen.uncrafteverything.save"), this::pressSaveButton).bounds(this.width / 2 + 10, (this.height - 45) + 15, 200, 20).build();
        this.addRenderableWidget(cancelButton);
        this.addRenderableWidget(saveButton);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);

        renderSeparator(graphics);
        renderScrollbar(graphics, 70);
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int scissorTop = 25;
        int scissorBottom = this.height - 45;
        pGuiGraphics.enableScissor(0, scissorTop, this.width, scissorBottom);

        int x = 10;
        int textWidth = this.width / 2 - 10;
        int labelY = moveToInventoryButton.getY();

        Component moveToInventory = Component.translatable("screen.uncrafteverything.config.move_to_inventory");
        pGuiGraphics.textWithWordWrap(this.font, moveToInventory, x, (int) (labelY + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF); labelY += 25;

        Component noRecipeFound = Component.translatable("screen.uncrafteverything.config.no_recipe_found");
        pGuiGraphics.textWithWordWrap(this.font, noRecipeFound, x, (int) (labelY + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF); labelY += 25;

        Component noSuitableOutputSlot = Component.translatable("screen.uncrafteverything.config.no_suitable_output_slot");
        pGuiGraphics.textWithWordWrap(this.font, noSuitableOutputSlot, x, (int) (labelY + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF); labelY += 25;

        Component notEnoughExpSlot = Component.translatable("screen.uncrafteverything.config.not_enough_exp");
        pGuiGraphics.textWithWordWrap(this.font, notEnoughExpSlot, x, (int) (labelY + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF); labelY += 25;

        Component notEnoughInputItemSlot = Component.translatable("screen.uncrafteverything.config.not_enough_input_item");
        pGuiGraphics.textWithWordWrap(this.font, notEnoughInputItemSlot, x, (int) (labelY + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF); labelY += 25;

        Component notEmptyShulkerSlot = Component.translatable("screen.uncrafteverything.config.not_empty_shulker");
        pGuiGraphics.textWithWordWrap(this.font, notEmptyShulkerSlot, x, (int) (labelY + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF); labelY += 25;

        Component restrictedByConfigSlot = Component.translatable("screen.uncrafteverything.config.restricted_by_config");
        pGuiGraphics.textWithWordWrap(this.font, restrictedByConfigSlot, x, (int) (labelY + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF); labelY += 25;

        Component damagedItemSlot = Component.translatable("screen.uncrafteverything.config.damaged_item");
        pGuiGraphics.textWithWordWrap(this.font, damagedItemSlot, x, (int) (labelY + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF); labelY += 25;

        Component enchantedItemSlot = Component.translatable("screen.uncrafteverything.config.enchanted_item");
        pGuiGraphics.textWithWordWrap(this.font, enchantedItemSlot, x, (int) (labelY + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF); labelY += 25;

        Component lockedItemSlot = Component.translatable("screen.uncrafteverything.config.locked_item");
        pGuiGraphics.textWithWordWrap(this.font, lockedItemSlot, x, (int) (labelY + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF); labelY += 25;

        Component progressionNotDefinedSlot = Component.translatable("screen.uncrafteverything.config.progression_not_defined");
        pGuiGraphics.textWithWordWrap(this.font, progressionNotDefinedSlot, x, (int) (labelY + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        super.extractRenderState(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        pGuiGraphics.disableScissor();

        pGuiGraphics.centeredText(this.font, Component.translatable("screen.uncrafteverything.uncraft_everything_client_config"), this.width / 2, (23 - this.font.lineHeight) / 2, 0xFFFFFFFF);
        cancelButton.extractRenderState(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        saveButton.extractRenderState(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

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
    public boolean mouseDragged(MouseButtonEvent buttonEvent, double dragX, double dragY) {
        for (ColorPickerWidget x : widgets.keySet()){
            if (x.isVisible()){
                return x.mouseDragged(buttonEvent, dragX, dragY);
            }
        }
        return super.mouseDragged(buttonEvent, dragX, dragY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent buttonEvent, boolean doubled) {
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
        this.clearWidgets();
        this.init();
    }

    private void initColorPicker(ColorPickerWidget colorPickerWidget, int color, int widgetWidth, int yOffset){
        colorPickerWidget.setColor(color);
        colorPickerWidget.setButtonWidth(widgetWidth);
        colorPickerWidget.setY((int) (baseY + yOffset - scrollAmount));
        colorPickerWidget.setHeight((int) (ColorPickerWidget.PICKER_SIZE + 60 + scrollAmount));
        this.addRenderableWidget(colorPickerWidget);
    }

    private void pressSaveButton(Button button){
        UncraftEverythingClientConfig.CONFIG.autoMoveToInventory.set(this.autoMoveToInventory);
        UncraftEverythingClientConfig.CONFIG.noRecipeFoundColor.set(this.noRecipeFoundColor);
        UncraftEverythingClientConfig.CONFIG.noSuitableOutputSlotColor.set(this.noSuitableOutputSlotColor);
        UncraftEverythingClientConfig.CONFIG.notEnoughExpColor.set(this.notEnoughExpColor);
        UncraftEverythingClientConfig.CONFIG.notEnoughInputItemColor.set(this.notEnoughInputItemColor);
        UncraftEverythingClientConfig.CONFIG.notEmptyShulkerColor.set(this.notEmptyShulkerColor);
        UncraftEverythingClientConfig.CONFIG.restrictedByConfigColor.set(this.restrictedByConfigColor);
        UncraftEverythingClientConfig.CONFIG.damagedItemColor.set(this.damagedItemColor);
        UncraftEverythingClientConfig.CONFIG.enchantedItemColor.set(this.enchantedItemColor);
        UncraftEverythingClientConfig.CONFIG.lockedItemColor.set(this.lockedItemColor);
        UncraftEverythingClientConfig.CONFIG.progressionNotDefinedColor.set(this.progressionNotDefinedColor);
        UncraftEverythingClientConfig.CONFIG.save();
        onClose();
    }

    private void pressCancelButton(Button button){
        onClose();
    }

    protected void renderSeparator(GuiGraphicsExtractor guiGraphics){
        Identifier header = this.minecraft.level == null ? Screen.HEADER_SEPARATOR : Screen.INWORLD_HEADER_SEPARATOR;
        Identifier footer = this.minecraft.level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, header, 0, 25 - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, footer, 0, this.height - 45, 0.0F, 0.0F, this.width, 2, 32, 2);
    }

    private void renderButtonTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY){
        if (moveToInventoryButton.isHovered()){
            List<Component> tooltip = List.of(
                    title("tooltip.uncrafteverything.config.move_to_inventory"),
                    valueInfo("tooltip.uncrafteverything.config.true", "tooltip.uncrafteverything.config.auto_move_true"),
                    Component.empty(),
                    valueInfo("tooltip.uncrafteverything.config.false", "tooltip.uncrafteverything.config.auto_move_false")
            );
            guiGraphics.setTooltipForNextFrame(this.font, tooltip, Optional.empty(), mouseX, mouseY);
        }
    }

    private Component title(String title){
        return Component.translatable(title).withStyle(ChatFormatting.BLUE);
    }

    private Component valueInfo(String value, String info){
        return Component.translatable(value).append(": ").withStyle(ChatFormatting.AQUA).append(Component.translatable(info).withStyle(ChatFormatting.GRAY));
    }

    private Component description(String desc){
        return Component.translatable(desc).withStyle(ChatFormatting.GRAY);
    }

    private void setNoRecipeFoundColor(int noRecipeFoundColor, ColorPickerWidget button) {
        this.noRecipeFoundColor = noRecipeFoundColor;
        this.widgets.put(button, new Pair<>(noRecipeFoundColor, this.widgets.get(button).getSecond()));
    }

    private void setNoSuitableOutputSlotColor(int noSuitableOutputSlotColor, ColorPickerWidget button) {
        this.noSuitableOutputSlotColor = noSuitableOutputSlotColor;
        this.widgets.put(button, new Pair<>(noSuitableOutputSlotColor, this.widgets.get(button).getSecond()));
    }

    public void setNotEnoughExpColor(int notEnoughExpColor, ColorPickerWidget button) {
        this.notEnoughExpColor = notEnoughExpColor;
        this.widgets.put(button, new Pair<>(notEnoughExpColor, this.widgets.get(button).getSecond()));
    }

    public void setNotEnoughInputItemColor(int notEnoughInputItemColor, ColorPickerWidget button) {
        this.notEnoughInputItemColor = notEnoughInputItemColor;
        this.widgets.put(button, new Pair<>(notEnoughInputItemColor, this.widgets.get(button).getSecond()));
    }

    public void setNotEmptyShulkerColor(int notEmptyShulkerColor, ColorPickerWidget button) {
        this.notEmptyShulkerColor = notEmptyShulkerColor;
        this.widgets.put(button, new Pair<>(notEmptyShulkerColor, this.widgets.get(button).getSecond()));
    }

    public void setRestrictedByConfigColor(int restrictedByConfigColor, ColorPickerWidget button) {
        this.restrictedByConfigColor = restrictedByConfigColor;
        this.widgets.put(button, new Pair<>(restrictedByConfigColor, this.widgets.get(button).getSecond()));
    }

    public void setDamagedItemColor(int damagedItemColor, ColorPickerWidget button) {
        this.damagedItemColor = damagedItemColor;
        this.widgets.put(button, new Pair<>(damagedItemColor, this.widgets.get(button).getSecond()));
    }

    public void setEnchantedItemColor(int enchantedItemColor, ColorPickerWidget button) {
        this.enchantedItemColor = enchantedItemColor;
        this.widgets.put(button, new Pair<>(enchantedItemColor, this.widgets.get(button).getSecond()));
    }

    public void setLockedItemColor(int lockedItemColor, ColorPickerWidget button) {
        this.lockedItemColor = lockedItemColor;
        this.widgets.put(button, new Pair<>(lockedItemColor, this.widgets.get(button).getSecond()));
    }

    public void setProgressionNotDefinedColor(int progressionNotDefinedColor, ColorPickerWidget button) {
        this.progressionNotDefinedColor = progressionNotDefinedColor;
        this.widgets.put(button, new Pair<>(progressionNotDefinedColor, this.widgets.get(button).getSecond()));
    }
}