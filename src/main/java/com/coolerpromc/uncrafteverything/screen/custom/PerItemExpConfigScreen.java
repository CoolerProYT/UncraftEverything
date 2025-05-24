package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.networking.ClientPayloadHandler;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UEExpPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.network.PacketDistributor;
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

    private final List<EditBox> scrollableEditBoxes = new ArrayList<>();
    private final List<Button> scrollableButtons = new ArrayList<>();

    public PerItemExpConfigScreen(Screen parent) {
        super(Component.literal("Per Item Exp Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        saveCurrentValues();
        this.clearWidgets();
        scrollableEditBoxes.clear();
        scrollableButtons.clear();

        if (!hasLoadedFromConfig) {
            for (Map.Entry<String, Integer> entry : ClientPayloadHandler.payloadFromServer.perItemExp().entrySet()) {
                entries.add(new Entry(entry.getKey(), entry.getValue()));
            }
            hasLoadedFromConfig = true;
        }

        int visibleHeight = ENTRIES_END_Y - ENTRIES_START_Y;
        int totalHeight = entries.size() * ENTRY_HEIGHT;
        maxScrollOffset = Math.max(0, totalHeight - visibleHeight);
        scrollOffset = Mth.clamp(scrollOffset, 0, maxScrollOffset);

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

        Button addButton = Button.builder(Component.literal("Add New Entry"), b -> {
            entries.add(new Entry("", 0));
            this.init();
        }).bounds(width / 2 - 100, height - 60, 200, 20).build();
        addRenderableWidget(addButton);

        Button saveButton = Button.builder(Component.literal("Save"), this::saveButtonPressed).bounds(width / 2 - 100, height - 30, 200, 20).build();
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

        UEExpPayload configPayload = new UEExpPayload(newConfig);
        UEExpPayload.INSTANCE.send(PacketDistributor.SERVER.noArg(), configPayload);
        RequestConfigPayload.INSTANCE.send(PacketDistributor.SERVER.noArg(), new RequestConfigPayload());
        this.getMinecraft().setScreen(parent);
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
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (maxScrollOffset > 0) {
            scrollOffset = Mth.clamp(scrollOffset - (int)(delta * 10), 0, maxScrollOffset);
            init();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);

        guiGraphics.fill(width / 2 - 120, ENTRIES_START_Y - 5, width / 2 + 120, ENTRIES_END_Y + 5, 0x88000000);

        guiGraphics.drawCenteredString(font, title, width / 2, 10, 0xFFFFFF);
        guiGraphics.drawCenteredString(font, "Entries: " + entries.size(), width / 2, 25, 0xCCCCCC);

        guiGraphics.enableScissor(width / 2 - 120, ENTRIES_START_Y - 5, width / 2 + 120, ENTRIES_END_Y + 5);

        for (EditBox editBox : scrollableEditBoxes) {
            editBox.render(guiGraphics, mouseX, mouseY, delta);
        }
        for (Button button : scrollableButtons) {
            button.render(guiGraphics, mouseX, mouseY, delta);
        }

        guiGraphics.disableScissor();

        this.renderables.forEach(renderable -> {
            if (renderable instanceof Button && !scrollableButtons.contains(renderable)) {
                renderable.render(guiGraphics, mouseX, mouseY, delta);
            }
        });

        if (maxScrollOffset > 0) {
            guiGraphics.drawCenteredString(font, "Scroll to see more entries", width / 2, ENTRIES_END_Y + 10, 0xAAAAAA);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        this.getMinecraft().setScreen(parent);
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
            keyBox = new EditBox(font, x, y, 150, 20, Component.literal("Key"));
            keyBox.setValue(currentKey);

            valueBox = new EditBox(font, x + 160, y, 40, 20, Component.literal("Value"));
            valueBox.setValue(currentValue);
            valueBox.setFilter(s -> s.matches("\\d*"));

            deleteButton = Button.builder(Component.literal("X"), b -> {
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