package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverythingClient;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UEExpPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerItemExpConfigScreen extends AbstractScrollableScreen {
    private final Screen parent;
    private final List<Entry> entries = new ArrayList<>();
    private final int ENTRY_HEIGHT = 24;
    private final int ENTRIES_START_Y = 30;
    private final int ENTRIES_END_Y = 200;
    private boolean hasLoadedFromConfig = false;

    private ButtonWidget addButton;
    private ButtonWidget cancelButton;
    private ButtonWidget saveButton;

    private final List<TextFieldWidget> scrollableEditBoxes = new ArrayList<>();
    private final List<ButtonWidget> scrollableButtons = new ArrayList<>();

    public PerItemExpConfigScreen(Screen parent) {
        super(Text.translatable("screen.uncrafteverything.per_item_xp_config"), 200);
        this.parent = parent;
    }

    @Override
    protected void init() {
        saveCurrentValues();
        this.clearChildren();
        scrollableEditBoxes.clear();
        scrollableButtons.clear();

        if (!hasLoadedFromConfig) {
            for (Map.Entry<String, Integer> entry : UncraftEverythingClient.payloadFromServer.perItemExp().entrySet()) {
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

        addButton = ButtonWidget.builder(Text.translatable("screen.uncrafteverything.add_new_entry"), b -> {
            entries.add(new Entry("", 0));
            this.init();
        }).dimensions(width / 2 - 100, height - 53, 200, 20).build();
        addDrawableChild(addButton);

        cancelButton = ButtonWidget.builder(Text.translatable("screen.uncrafteverything.cancel"), button -> close()).dimensions(width / 2 - width / 3 - 10, height - 28, width / 3, 20).build();
        addDrawableChild(cancelButton);

        saveButton = ButtonWidget.builder(Text.translatable("screen.uncrafteverything.save"), this::saveButtonPressed).dimensions(width / 2 + 10, height - 28, width / 3, 20).build();
        addDrawableChild(saveButton);
    }

    private void saveButtonPressed(ButtonWidget button){
        saveCurrentValues();
        Map<String, Integer> newConfig = new HashMap<>();
        for (Entry entry : entries) {
            String key = entry.currentKey.trim();
            String val = entry.currentValue.trim();
            if (!key.isEmpty() && val.matches("\\d+")) {
                newConfig.put(key, Integer.parseInt(val));
            }
        }

        UEExpPayload configPayload = new UEExpPayload(newConfig);
        ClientPlayNetworking.send(configPayload);
        ClientPlayNetworking.send(new RequestConfigPayload());
        this.client.setScreen(parent);
    }

    private void saveCurrentValues() {
        for (Entry entry : entries) {
            if (entry.keyBox != null) {
                entry.currentKey = entry.keyBox.getText();
            }
            if (entry.valueBox != null) {
                entry.currentValue = entry.valueBox.getText();
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
    public void renderBackground(DrawContext guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderDarkening(guiGraphics);
        applyBlur(guiGraphics);
        renderSeparator(guiGraphics);
        renderScrollbar(guiGraphics, 90);
    }

    @Override
    public void render(@NotNull DrawContext guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.drawCenteredTextWithShadow(textRenderer, title, width / 2, (23 - this.textRenderer.fontHeight) / 2, 0xFFFFFFFF);

        guiGraphics.enableScissor(0, ENTRIES_START_Y - 5, width, this.height - 65);

        Text key = Text.translatable("screen.uncrafteverything.per_item_xp_config.key");
        guiGraphics.drawText(textRenderer, key, (width / 2 - 115) + (150 - textRenderer.getWidth(key)) / 2, (int) (ENTRIES_START_Y - scrollAmount), 0xFFFFFFFF, false);

        Text value = Text.translatable("screen.uncrafteverything.per_item_xp_config.value");
        guiGraphics.drawText(textRenderer, value, (width / 2 - 115 + 160) + (40 - textRenderer.getWidth(value)) / 2, (int) (ENTRIES_START_Y - scrollAmount), 0xFFFFFFFF, false);

        Text del = Text.translatable("screen.uncrafteverything.per_item_xp_config.del");
        guiGraphics.drawText(textRenderer, del, (width / 2 - 115 + 210) + (20 - textRenderer.getWidth(del)) / 2, (int) (ENTRIES_START_Y - scrollAmount), 0xFFFFFFFF, false);

        for (TextFieldWidget editBox : scrollableEditBoxes) {
            editBox.render(guiGraphics, mouseX, mouseY, delta);
        }
        for (ButtonWidget button : scrollableButtons) {
            button.render(guiGraphics, mouseX, mouseY, delta);
        }

        guiGraphics.disableScissor();

        this.children().forEach(renderable -> {
            if (renderable instanceof ButtonWidget buttonWidget && !scrollableButtons.contains(buttonWidget)) {
                buttonWidget.render(guiGraphics, mouseX, mouseY, delta);
            }
        });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {int scrollTop = 25;
        int scrollBottom = this.height - 65;

        boolean inScrollArea = mouseY >= scrollTop && mouseY <= scrollBottom;

        if (!inScrollArea) {
            if (!(addButton.isMouseOver(mouseX, mouseY) || cancelButton.isMouseOver(mouseX, mouseY) || saveButton.isMouseOver(mouseX, mouseY))) {
                return false;
            }
            else{
                if (addButton.isMouseOver(mouseX, mouseY)){
                    return addButton.mouseClicked(mouseX, mouseY, button);
                }

                if (cancelButton.isMouseOver(mouseX, mouseY)){
                    return cancelButton.mouseClicked(mouseX, mouseY, button);
                }

                if (saveButton.isMouseOver(mouseX, mouseY)){
                    return saveButton.mouseClicked(mouseX, mouseY, button);
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    protected void renderSeparator(DrawContext guiGraphics){
        Identifier header = this.client.world == null ? Screen.HEADER_SEPARATOR_TEXTURE : Screen.INWORLD_HEADER_SEPARATOR_TEXTURE;
        Identifier footer = this.client.world == null ? Screen.FOOTER_SEPARATOR_TEXTURE : Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE;
        guiGraphics.drawTexture(RenderPipelines.GUI_TEXTURED, header, 0, 25 - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        guiGraphics.drawTexture(RenderPipelines.GUI_TEXTURED, footer, 0, this.height - 65, 0.0F, 0.0F, this.width, 2, 32, 2);
    }

    private class Entry {
        TextFieldWidget keyBox;
        TextFieldWidget valueBox;
        ButtonWidget deleteButton;

        String currentKey;
        String currentValue;

        Entry(String key, int value) {
            this.currentKey = key;
            this.currentValue = String.valueOf(value);
        }

        void initWidgets(int x, int y) {
            keyBox = new TextFieldWidget(textRenderer, x, y, 150, 20, Text.translatable("screen.uncrafteverything.key"));
            keyBox.setText(currentKey);

            valueBox = new TextFieldWidget(textRenderer, x + 160, y, 40, 20, Text.translatable("screen.uncrafteverything.value"));
            valueBox.setText(currentValue);
            valueBox.setTextPredicate(s -> s.matches("\\d*"));

            deleteButton = ButtonWidget.builder(Text.translatable("screen.uncrafteverything.x"), b -> {
                entries.remove(this);
                init();
            }).dimensions(x + 210, y, 20, 20).build();
        }

        void addToScreen(PerItemExpConfigScreen screen) {
            screen.addDrawableChild(keyBox);
            screen.addDrawableChild(valueBox);
            screen.addDrawableChild(deleteButton);

            screen.scrollableEditBoxes.add(keyBox);
            screen.scrollableEditBoxes.add(valueBox);
            screen.scrollableButtons.add(deleteButton);
        }
    }
}