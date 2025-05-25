package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverythingClient;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UEExpPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerItemExpConfigScreen extends Screen {
    private final Screen parent;
    private final List<Entry> entries = new ArrayList<>();
    private final int ENTRY_HEIGHT = 24;
    private final int ENTRIES_START_Y = 40;
    private final int ENTRIES_END_Y = 180;
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;
    private boolean hasLoadedFromConfig = false;

    private final List<TextFieldWidget> scrollableEditBoxes = new ArrayList<>();
    private final List<ButtonWidget> scrollableButtons = new ArrayList<>();

    public PerItemExpConfigScreen(Screen parent) {
        super(Text.literal("Per Item Exp Config"));
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
        int totalHeight = entries.size() * ENTRY_HEIGHT;
        maxScrollOffset = Math.max(0, totalHeight - visibleHeight);
        scrollOffset = MathHelper.clamp(scrollOffset, 0, maxScrollOffset);

        int startIndex = scrollOffset / ENTRY_HEIGHT;
        int endIndex = Math.min(entries.size(), startIndex + (visibleHeight / ENTRY_HEIGHT) + 2);

        for (int i = startIndex; i < endIndex; i++) {
            Entry entry = entries.get(i);
            int y = ENTRIES_START_Y + (i * ENTRY_HEIGHT) - scrollOffset;

            if (y >= ENTRIES_START_Y - ENTRY_HEIGHT && y <= ENTRIES_END_Y) {
                entry.initWidgets(width / 2 - 115, y);
                entry.addToScreen(this); // Add to both main widget list and scrollable lists
            }
        }

        ButtonWidget addButton = ButtonWidget.builder(Text.literal("Add New Entry"), b -> {
            entries.add(new Entry("", 0));
            this.init();
        }).dimensions(width / 2 - 100, height - 60, 200, 20).build();
        addDrawableChild(addButton);

        ButtonWidget saveButton = ButtonWidget.builder(Text.literal("Save"), this::saveButtonPressed).dimensions(width / 2 - 100, height - 30, 200, 20).build();
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
        if (maxScrollOffset > 0) {
            scrollOffset = MathHelper.clamp(scrollOffset - (int)(verticalAmount * 10), 0, maxScrollOffset);
            init();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(@NotNull DrawContext guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);

        guiGraphics.fill(width / 2 - 120, ENTRIES_START_Y - 5, width / 2 + 120, ENTRIES_END_Y + 5, 0x88000000);

        guiGraphics.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFFFF);
        guiGraphics.drawCenteredTextWithShadow(textRenderer, "Entries: " + entries.size(), width / 2, 25, 0xCCCCCC);

        guiGraphics.enableScissor(width / 2 - 120, ENTRIES_START_Y - 5, width / 2 + 120, ENTRIES_END_Y + 5);

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

        if (maxScrollOffset > 0) {
            guiGraphics.drawCenteredTextWithShadow(textRenderer, "Scroll to see more entries", width / 2, ENTRIES_END_Y + 10, 0xAAAAAA);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
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
            keyBox = new TextFieldWidget(textRenderer, x, y, 150, 20, Text.literal("Key"));
            keyBox.setText(currentKey);

            valueBox = new TextFieldWidget(textRenderer, x + 160, y, 40, 20, Text.literal("Value"));
            valueBox.setText(currentValue);
            valueBox.setTextPredicate(s -> s.matches("\\d*"));

            deleteButton = ButtonWidget.builder(Text.literal("X"), b -> {
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