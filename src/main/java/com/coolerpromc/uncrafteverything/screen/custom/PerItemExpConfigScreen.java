package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.networking.ClientPayloadHandler;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UEExpPayload;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mojang.blaze3d.systems.RenderSystem.disableScissor;
import static com.mojang.blaze3d.systems.RenderSystem.enableScissor;

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
    private final List<Button> scrollableButtons = new ArrayList<>();

    public PerItemExpConfigScreen(Screen parent) {
        super(new StringTextComponent("Per Item Exp Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        saveCurrentValues();
        this.children.clear();
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

        Button addButton = new Button(width / 2 - 100, height - 60, 200, 20, new StringTextComponent("Add New Entry"), button -> {
            entries.add(new Entry("", 0));
            this.init();
        });

        addWidget(addButton);

        Button saveButton = new Button(width / 2 - 100, height - 30, 200, 20, new StringTextComponent("Save Changes"), this::saveButtonPressed);
        addWidget(saveButton);
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
            scrollOffset = MathHelper.clamp(scrollOffset - (int)(delta * 10), 0, maxScrollOffset);
            init();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(MatrixStack guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);

        fill(guiGraphics,width / 2 - 120, ENTRIES_START_Y - 5, width / 2 + 120, ENTRIES_END_Y + 5, 0x88000000);

        drawCenteredString(guiGraphics, font, title, width / 2, 10, 0xFFFFFF);
        drawCenteredString(guiGraphics, font, "Entries: " + entries.size(), width / 2, 25, 0xCCCCCC);

        enableScissor(width / 2 - 120, ENTRIES_START_Y - 5, width / 2 + 120, ENTRIES_END_Y + 5);

        for (TextFieldWidget editBox : scrollableEditBoxes) {
            editBox.render(guiGraphics, mouseX, mouseY, delta);
        }
        for (Button button : scrollableButtons) {
            button.render(guiGraphics, mouseX, mouseY, delta);
        }

        disableScissor();

        this.children.forEach(renderable -> {
            if (renderable instanceof Button && !scrollableButtons.contains(renderable)) {
                ((Button) renderable).render(guiGraphics, mouseX, mouseY, delta);
            }
        });

        if (maxScrollOffset > 0) {
            drawCenteredString(guiGraphics, font, "Scroll to see more entries", width / 2, ENTRIES_END_Y + 10, 0xAAAAAA);
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
        TextFieldWidget keyBox;
        TextFieldWidget valueBox;
        Button deleteButton;

        String currentKey;
        String currentValue;

        Entry(String key, int value) {
            this.currentKey = key;
            this.currentValue = String.valueOf(value);
        }

        void initWidgets(int x, int y) {
            keyBox = new TextFieldWidget(font, x, y, 150, 20, new StringTextComponent("Key"));
            keyBox.setValue(currentKey);

            valueBox = new TextFieldWidget(font, x + 160, y, 40, 20, new StringTextComponent("Value"));
            valueBox.setValue(currentValue);
            valueBox.setFilter(s -> s.matches("\\d*"));

            deleteButton = new Button(x + 210, y, 20, 20, new StringTextComponent("X"),  b -> {
                entries.remove(this);
                init();
            });
        }

        void addToScreen(PerItemExpConfigScreen screen) {
            screen.addWidget(keyBox);
            screen.addWidget(valueBox);
            screen.addWidget(deleteButton);

            screen.scrollableEditBoxes.add(keyBox);
            screen.scrollableEditBoxes.add(valueBox);
            screen.scrollableButtons.add(deleteButton);
        }
    }
}