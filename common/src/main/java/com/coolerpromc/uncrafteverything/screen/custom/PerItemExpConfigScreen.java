package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.CommonClientClass;
import com.coolerpromc.uncrafteverything.networking.ServerBoundRequestConfigPayload;
import com.coolerpromc.uncrafteverything.networking.ServerBoundUEExpPayload;
import com.coolerpromc.uncrafteverything.platform.Services;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerItemExpConfigScreen extends AbstractScrollableScreen {
    private final List<Entry> entries = new ArrayList<>();
    private final int ENTRY_HEIGHT = 24;
    private final int ENTRIES_START_Y = 30;
    private final int ENTRIES_END_Y = 200;
    private boolean hasLoadedFromConfig = false;

    private Button addButton;
    private Button cancelButton;
    private Button saveButton;

    private final List<EditBox> scrollableEditBoxes = new ArrayList<>();
    private final List<Button> scrollableButtons = new ArrayList<>();

    public PerItemExpConfigScreen(Component text) {
        super(text, 200);
    }

    @Override
    protected void init() {
        saveCurrentValues();
        this.clearWidgets();
        scrollableEditBoxes.clear();
        scrollableButtons.clear();

        if (!hasLoadedFromConfig) {
            for (Map.Entry<String, Integer> entry : CommonClientClass.payloadFromServer.perItemExp().entrySet()) {
                entries.add(new Entry(entry.getKey(), entry.getValue()));
            }
            hasLoadedFromConfig = true;
        }

        int visibleHeight = ENTRIES_END_Y - ENTRIES_START_Y;
        contentHeight = entries.size() * ENTRY_HEIGHT + 16;

        int startIndex = (int) (scrollAmount / ENTRY_HEIGHT);
        int endIndex = Math.min(entries.size(), startIndex + (visibleHeight / ENTRY_HEIGHT) + 2);

        for (int i = startIndex; i < endIndex; i++) {
            Entry entry = entries.get(i);
            int y = (int) (ENTRIES_START_Y + (i * ENTRY_HEIGHT) - scrollAmount);

            if (y >= ENTRIES_START_Y - ENTRY_HEIGHT && y <= ENTRIES_END_Y) {
                entry.initWidgets(width / 2 - 115, y + 16);
                entry.addToScreen(this); // Add to both main widget list and scrollable lists
            }
        }

        addButton = Button.builder(Component.translatable("screen.uncrafteverything.add_new_entry"), b -> {
            entries.add(new Entry("", 0));
            this.init();
        }).bounds(width / 2 - 100, height - 53, 200, 20).build();
        addRenderableWidget(addButton);

        cancelButton = Button.builder(Component.translatable("screen.uncrafteverything.cancel"), button -> onClose()).bounds(width / 2 - width / 3 - 10, height - 28, width / 3, 20).build();
        addRenderableWidget(cancelButton);

        saveButton = Button.builder(Component.translatable("screen.uncrafteverything.save"), this::saveButtonPressed).bounds(width / 2 + 10, height - 28, width / 3, 20).build();
        addRenderableWidget(saveButton);
    }

    private void saveButtonPressed(Button button){
        saveCurrentValues();
        Map<String, Integer> newConfig = new HashMap<>();
        for (Entry entry : entries) {
            String key = entry.currentKey.trim();
            String val = entry.currentValue.trim();
            if (!key.isEmpty() && val.matches("\\d+")) {
                newConfig.put(key, Integer.parseInt(val));
            }
        }

        ServerBoundUEExpPayload configPayload = new ServerBoundUEExpPayload(newConfig);
        Services.NETWORK.sendToServer(configPayload);
        Services.NETWORK.sendToServer(new ServerBoundRequestConfigPayload());
        onClose();
    }

    private void saveCurrentValues() {
        for (Entry entry : entries) {
            if (entry.keyBox != null) {
                entry.currentKey = entry.keyBox.getValue();
            }
            if (entry.valueBox != null) {
                entry.currentValue = entry.valueBox.getValue();
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        init();
        return true;
    }

    @Override
    protected int scrollBarX() {
        return this.width - 6;
    }

    @Override
    protected int scrollBarY() {
        int scrollBarHeight = Math.max(10, (int) ((this.height - 90) * (this.height - 90) / (double) contentHeight));
        return (int) (25 + (scrollAmount / getMaxScroll()) * (this.height - 90 - scrollBarHeight));
    }

    @Override
    protected int scrollerHeight() {
        return Math.max(10, (int) ((this.height - 90) * (this.height - 90) / (double) contentHeight));
    }

    @Override
    protected int getMaxScroll() {
        return Math.max(0, contentHeight - (height - 95));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);

        renderSeparator(graphics);
        renderScrollbar(graphics, 90);
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.centeredText(font, title, width / 2, (23 - this.font.lineHeight) / 2, 0xFFFFFFFF);

        guiGraphics.enableScissor(0, ENTRIES_START_Y - 5, width, this.height - 65);

        Component key = Component.translatable("screen.uncrafteverything.per_item_xp_config.key");
        guiGraphics.text(font, key, (width / 2 - 115) + (150 - font.width(key)) / 2, (int) (ENTRIES_START_Y - scrollAmount), 0xFFFFFFFF, false);

        Component value = Component.translatable("screen.uncrafteverything.per_item_xp_config.value");
        guiGraphics.text(font, value, (width / 2 - 115 + 160) + (40 - font.width(value)) / 2, (int) (ENTRIES_START_Y - scrollAmount), 0xFFFFFFFF, false);

        Component del = Component.translatable("screen.uncrafteverything.per_item_xp_config.del");
        guiGraphics.text(font, del, (width / 2 - 115 + 210) + (20 - font.width(del)) / 2, (int) (ENTRIES_START_Y - scrollAmount), 0xFFFFFFFF, false);

        for (EditBox editBox : scrollableEditBoxes) {
            editBox.extractRenderState(guiGraphics, mouseX, mouseY, delta);
        }
        for (Button button : scrollableButtons) {
            button.extractRenderState(guiGraphics, mouseX, mouseY, delta);
        }

        guiGraphics.disableScissor();

        this.children().forEach(renderable -> {
            if (renderable instanceof Button buttonWidget && !scrollableButtons.contains(buttonWidget)) {
                buttonWidget.extractRenderState(guiGraphics, mouseX, mouseY, delta);
            }
        });
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        int scrollTop = 25;
        int scrollBottom = this.height - 65;

        boolean inScrollArea = click.y() >= scrollTop && click.y() <= scrollBottom;

        if (!inScrollArea) {
            if (!(addButton.isMouseOver(click.x(), click.y()) || cancelButton.isMouseOver(click.x(), click.y()) || saveButton.isMouseOver(click.x(), click.y()))) {
                return false;
            }
            else{
                if (addButton.isMouseOver(click.x(), click.y())){
                    return addButton.mouseClicked(click, doubled);
                }

                if (cancelButton.isMouseOver(click.x(), click.y())){
                    return cancelButton.mouseClicked(click, doubled);
                }

                if (saveButton.isMouseOver(click.x(), click.y())){
                    return saveButton.mouseClicked(click, doubled);
                }
            }
        }

        return super.mouseClicked(click, doubled);
    }

    protected void renderSeparator(GuiGraphicsExtractor guiGraphics){
        Identifier header = this.minecraft.level == null ? Screen.HEADER_SEPARATOR : Screen.INWORLD_HEADER_SEPARATOR;
        Identifier footer = this.minecraft.level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, header, 0, 25 - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, footer, 0, this.height - 65, 0.0F, 0.0F, this.width, 2, 32, 2);
    }

    private class Entry {
        EditBox keyBox;
        EditBox valueBox;
        Button deleteButton;

        String currentKey;
        String currentValue;

        Entry(String key, int value) {
            this.currentKey = key;
            this.currentValue = String.valueOf(value);
        }

        void initWidgets(int x, int y) {
            keyBox = new EditBox(font, x, y, 150, 20, Component.translatable("screen.uncrafteverything.key"));
            keyBox.setValue(currentKey);

            valueBox = new EditBox(font, x + 160, y, 40, 20, Component.translatable("screen.uncrafteverything.value"));
            valueBox.setValue(currentValue);

            deleteButton = Button.builder(Component.translatable("screen.uncrafteverything.x"), b -> {
                entries.remove(this);
                init();
            }).bounds(x + 210, y, 20, 20).build();
        }

        void addToScreen(PerItemExpConfigScreen screen) {
            screen.addRenderableWidget(keyBox);
            screen.addRenderableWidget(valueBox);
            screen.addRenderableWidget(deleteButton);

            screen.scrollableEditBoxes.add(keyBox);
            screen.scrollableEditBoxes.add(valueBox);
            screen.scrollableButtons.add(deleteButton);
        }
    }
}