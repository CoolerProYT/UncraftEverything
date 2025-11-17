package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.config.UncraftEverythingClientConfig;
import com.coolerpromc.uncrafteverything.screen.widget.ColorPickerWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class UEClientConfigScreen extends AbstractScrollableScreen {
    private final UncraftEverythingClientConfig config = UncraftEverythingClientConfig.CONFIG;
    public boolean autoMoveToInventory = config.autoMoveToInventory.getAsBoolean();
    public int noRecipeFoundColor = config.noRecipeFoundColor.getAsInt();
    public int noSuitableOutputSlotColor = config.noSuitableOutputSlotColor.getAsInt();
    public int notEnoughExpColor = config.notEnoughExpColor.getAsInt();
    public int notEnoughInputItemColor = config.notEnoughInputItemColor.getAsInt();
    public int notEmptyShulkerColor = config.notEmptyShulkerColor.getAsInt();
    public int restrictedByConfigColor = config.restrictedByConfigColor.getAsInt();
    public int damagedItemColor = config.damagedItemColor.getAsInt();
    public int enchantedItemColor = config.enchantedItemColor.getAsInt();
    public int lockedItemColor = config.lockedItemColor.getAsInt();
    public int progressionNotDefinedColor = config.progressionNotDefinedColor.getAsInt();

    private final int baseY = 30;
    private final Map<ColorPickerWidget, Tuple<Integer, Integer>> widgets = new HashMap<>();

    private Button moveToInventoryButton;
    private Button cancelButton;
    private Button saveButton;

    public UEClientConfigScreen(Component title) {
        super(title, 250);
        ColorPickerWidget noRecipeFoundButton = new ColorPickerWidget(0, (int) (baseY + 25 - scrollAmount), 0, noRecipeFoundColor, Component.translatable("screen.uncrafteverything.config.no_recipe_found"), this, this::setNoRecipeFoundColor);
        ColorPickerWidget noSuitableOutputSlotButton = new ColorPickerWidget(0, (int) (baseY + 50 - scrollAmount), 0, noSuitableOutputSlotColor, Component.translatable("screen.uncrafteverything.config.no_suitable_output_slot"), this, this::setNoSuitableOutputSlotColor);
        ColorPickerWidget notEnoughExpButton = new ColorPickerWidget(0, (int) (baseY + 75 - scrollAmount), 0, notEnoughExpColor, Component.translatable("screen.uncrafteverything.config.not_enough_exp"), this, this::setNotEnoughExpColor);
        ColorPickerWidget notEnoughInputItemButton = new ColorPickerWidget(0, (int) (baseY + 100 - scrollAmount), 0, notEnoughInputItemColor, Component.translatable("screen.uncrafteverything.config.not_enough_input_item"), this, this::setNotEnoughInputItemColor);
        ColorPickerWidget notEmptyShulkerButton = new ColorPickerWidget(0, (int) (baseY + 125 - scrollAmount), 0, notEmptyShulkerColor, Component.translatable("screen.uncrafteverything.config.not_empty_shulker"), this, this::setNotEmptyShulkerColor);
        ColorPickerWidget restrictedByConfigButton = new ColorPickerWidget(0, (int) (baseY + 150 - scrollAmount), 0, restrictedByConfigColor, Component.translatable("screen.uncrafteverything.config.restricted_by_config"), this, this::setRestrictedByConfigColor);
        ColorPickerWidget damagedItemButton = new ColorPickerWidget(0, (int) (baseY + 175 - scrollAmount), 0, damagedItemColor, Component.translatable("screen.uncrafteverything.config.damaged_item"), this, this::setDamagedItemColor);
        ColorPickerWidget enchantedItemButton = new ColorPickerWidget(0, (int) (baseY + 200 - scrollAmount), 0, enchantedItemColor, Component.translatable("screen.uncrafteverything.config.enchanted_item"), this, this::setEnchantedItemColor);
        ColorPickerWidget lockedItemButton = new ColorPickerWidget(0, (int) (baseY + 225 - scrollAmount), 0, lockedItemColor, Component.translatable("screen.uncrafteverything.config.locked_item"), this, this::setLockedItemColor);
        ColorPickerWidget progressionNotDefinedButton = new ColorPickerWidget(0, (int) (baseY + 250 - scrollAmount), 0, progressionNotDefinedColor, Component.translatable("screen.uncrafteverything.config.progression_not_defined"), this, this::setProgressionNotDefinedColor);

        widgets.put(noRecipeFoundButton, new Tuple<>(noRecipeFoundColor, 25));
        widgets.put(noSuitableOutputSlotButton, new Tuple<>(noSuitableOutputSlotColor, 50));
        widgets.put(notEnoughExpButton, new Tuple<>(notEnoughExpColor, 75));
        widgets.put(notEnoughInputItemButton, new Tuple<>(notEnoughInputItemColor, 100));
        widgets.put(notEmptyShulkerButton, new Tuple<>(notEmptyShulkerColor, 125));
        widgets.put(restrictedByConfigButton, new Tuple<>(restrictedByConfigColor, 150));
        widgets.put(damagedItemButton, new Tuple<>(damagedItemColor, 175));
        widgets.put(enchantedItemButton, new Tuple<>(enchantedItemColor, 200));
        widgets.put(lockedItemButton, new Tuple<>(lockedItemColor, 225));
        widgets.put(progressionNotDefinedButton, new Tuple<>(progressionNotDefinedColor, 250));
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

        Tuple<ColorPickerWidget, Tuple<Integer, Integer>> visible = null;

        for (Map.Entry<ColorPickerWidget, Tuple<Integer, Integer>> entry : widgets.entrySet()){
            entry.getKey().setHasOverlay(false);
            if (entry.getKey().isVisible()){
                visible = new Tuple<>(entry.getKey(), entry.getValue());
            }
            else{
                this.initColorPicker(entry.getKey(), entry.getValue().getA(), widgetWidth, entry.getValue().getB());
            }
        }

        if (visible != null){
            this.initColorPicker(visible.getA(), visible.getB().getA(), widgetWidth, visible.getB().getB());

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
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderMenuBackground(guiGraphics);
        renderBlurredBackground(guiGraphics);
        renderSeparator(guiGraphics);
        renderScrollbar(guiGraphics, 70);
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int scissorTop = 25;
        int scissorBottom = this.height - 45;
        pGuiGraphics.enableScissor(0, scissorTop, this.width, scissorBottom);

        int x = 10;
        int textWidth = this.width / 2 - 10;
        int baseY = 30;

        Component moveToInventory = Component.translatable("screen.uncrafteverything.config.move_to_inventory");
        pGuiGraphics.drawWordWrap(this.font, moveToInventory, x, (int) (baseY - scrollAmount + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Component noRecipeFound = Component.translatable("screen.uncrafteverything.config.no_recipe_found");
        pGuiGraphics.drawWordWrap(this.font, noRecipeFound, x, (int) (baseY + 25 - scrollAmount + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Component noSuitableOutputSlot = Component.translatable("screen.uncrafteverything.config.no_suitable_output_slot");
        pGuiGraphics.drawWordWrap(this.font, noSuitableOutputSlot, x, (int) (baseY + 50 - scrollAmount + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Component notEnoughExpSlot = Component.translatable("screen.uncrafteverything.config.not_enough_exp");
        pGuiGraphics.drawWordWrap(this.font, notEnoughExpSlot, x, (int) (baseY + 75 - scrollAmount + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Component notEnoughInputItemSlot = Component.translatable("screen.uncrafteverything.config.not_enough_input_item");
        pGuiGraphics.drawWordWrap(this.font, notEnoughInputItemSlot, x, (int) (baseY + 100 - scrollAmount + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Component notEmptyShulkerSlot = Component.translatable("screen.uncrafteverything.config.not_empty_shulker");
        pGuiGraphics.drawWordWrap(this.font, notEmptyShulkerSlot, x, (int) (baseY + 125 - scrollAmount + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Component restrictedByConfigSlot = Component.translatable("screen.uncrafteverything.config.restricted_by_config");
        pGuiGraphics.drawWordWrap(this.font, restrictedByConfigSlot, x, (int) (baseY + 150 - scrollAmount + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Component damagedItemSlot = Component.translatable("screen.uncrafteverything.config.damaged_item");
        pGuiGraphics.drawWordWrap(this.font, damagedItemSlot, x, (int) (baseY + 175 - scrollAmount + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Component enchantedItemSlot = Component.translatable("screen.uncrafteverything.config.enchanted_item");
        pGuiGraphics.drawWordWrap(this.font, enchantedItemSlot, x, (int) (baseY + 200 - scrollAmount + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Component lockedItemSlot = Component.translatable("screen.uncrafteverything.config.locked_item");
        pGuiGraphics.drawWordWrap(this.font, lockedItemSlot, x, (int) (baseY + 225 - scrollAmount + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        Component progressionNotDefinedSlot = Component.translatable("screen.uncrafteverything.config.progression_not_defined");
        pGuiGraphics.drawWordWrap(this.font, progressionNotDefinedSlot, x, (int) (baseY + 250 - scrollAmount + (this.font.lineHeight / 2d) + 2), textWidth, 0xFFFFFFFF);

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        pGuiGraphics.disableScissor();

        pGuiGraphics.drawCenteredString(this.font, Component.translatable("screen.uncrafteverything.uncraft_everything_client_config"), this.width / 2, (23 - this.font.lineHeight) / 2, 0xFFFFFFFF);
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
        config.autoMoveToInventory.set(this.autoMoveToInventory);
        config.noRecipeFoundColor.set(this.noRecipeFoundColor);
        config.noSuitableOutputSlotColor.set(this.noSuitableOutputSlotColor);
        config.notEnoughExpColor.set(this.notEnoughExpColor);
        config.notEnoughInputItemColor.set(this.notEnoughInputItemColor);
        config.notEmptyShulkerColor.set(this.notEmptyShulkerColor);
        config.restrictedByConfigColor.set(this.restrictedByConfigColor);
        config.damagedItemColor.set(this.damagedItemColor);
        config.enchantedItemColor.set(this.enchantedItemColor);
        config.lockedItemColor.set(this.lockedItemColor);
        config.progressionNotDefinedColor.set(this.progressionNotDefinedColor);
        UncraftEverythingClientConfig.CONFIG_SPEC.save();
        onClose();
    }

    private void pressCancelButton(Button button){
        onClose();
    }

    protected void renderSeparator(GuiGraphics guiGraphics){
        ResourceLocation header = this.getMinecraft().level == null ? Screen.HEADER_SEPARATOR : Screen.INWORLD_HEADER_SEPARATOR;
        ResourceLocation footer = this.getMinecraft().level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, header, 0, 25 - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, footer, 0, this.height - 45, 0.0F, 0.0F, this.width, 2, 32, 2);
    }

    private void renderButtonTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY){
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
        this.widgets.put(button, new Tuple<>(noRecipeFoundColor, 25));
    }

    private void setNoSuitableOutputSlotColor(int noSuitableOutputSlotColor, ColorPickerWidget button) {
        this.noSuitableOutputSlotColor = noSuitableOutputSlotColor;
        this.widgets.put(button, new Tuple<>(noSuitableOutputSlotColor, 50));
    }

    public void setNotEnoughExpColor(int notEnoughExpColor, ColorPickerWidget button) {
        this.notEnoughExpColor = notEnoughExpColor;
        this.widgets.put(button, new Tuple<>(notEnoughExpColor, 75));
    }

    public void setNotEnoughInputItemColor(int notEnoughInputItemColor, ColorPickerWidget button) {
        this.notEnoughInputItemColor = notEnoughInputItemColor;
        this.widgets.put(button, new Tuple<>(notEnoughInputItemColor, 100));
    }

    public void setNotEmptyShulkerColor(int notEmptyShulkerColor, ColorPickerWidget button) {
        this.notEmptyShulkerColor = notEmptyShulkerColor;
        this.widgets.put(button, new Tuple<>(notEmptyShulkerColor, 125));
    }

    public void setRestrictedByConfigColor(int restrictedByConfigColor, ColorPickerWidget button) {
        this.restrictedByConfigColor = restrictedByConfigColor;
        this.widgets.put(button, new Tuple<>(restrictedByConfigColor, 150));
    }

    public void setDamagedItemColor(int damagedItemColor, ColorPickerWidget button) {
        this.damagedItemColor = damagedItemColor;
        this.widgets.put(button, new Tuple<>(damagedItemColor, 175));
    }

    public void setEnchantedItemColor(int enchantedItemColor, ColorPickerWidget button) {
        this.enchantedItemColor = enchantedItemColor;
        this.widgets.put(button, new Tuple<>(enchantedItemColor, 200));
    }

    public void setLockedItemColor(int lockedItemColor, ColorPickerWidget button) {
        this.lockedItemColor = lockedItemColor;
        this.widgets.put(button, new Tuple<>(lockedItemColor, 225));
    }

    public void setProgressionNotDefinedColor(int progressionNotDefinedColor, ColorPickerWidget button) {
        this.progressionNotDefinedColor = progressionNotDefinedColor;
        this.widgets.put(button, new Tuple<>(progressionNotDefinedColor, 250));
    }
}
