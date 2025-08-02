package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.UncraftEverythingClient;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UEExpPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

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
        super(new TranslatableText("screen.uncrafteverything.per_item_xp_config"), 200);
        this.parent = parent;
    }

    @Override
    protected void init() {
        saveCurrentValues();
        this.children.clear();
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

        addButton = new ButtonWidget(width / 2 - 100, height - 53, 200, 20, new TranslatableText("screen.uncrafteverything.add_new_entry"), button -> {
            entries.add(new Entry("", 0));
            this.init();
        });
        addChild(addButton);

        cancelButton = new ButtonWidget(width / 2 - width / 3 - 10, height - 28, width / 3, 20, new TranslatableText("screen.uncrafteverything.cancel"), button -> onClose());
        addChild(cancelButton);

        saveButton = new ButtonWidget(width / 2 + 10, height - 28, width / 3, 20, new TranslatableText("screen.uncrafteverything.save"), this::saveButtonPressed);
        addChild(saveButton);
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
        ClientPlayNetworking.send(UEExpPayload.TYPE, UEExpPayload.encode(PacketByteBufs.create(), configPayload));
        ClientPlayNetworking.send(RequestConfigPayload.TYPE, RequestConfigPayload.encode(PacketByteBufs.create(), new RequestConfigPayload()));
        this.client.openScreen(parent);
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
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        super.mouseScrolled(mouseX, mouseY, delta);
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
    public void renderBackground(MatrixStack guiGraphics) {
        fillGradient(guiGraphics, 0, 0, this.width, this.height, -1072689136, -804253680);
        renderSeparator(guiGraphics);
        renderScrollbar(guiGraphics, 65);
    }

    @Override
    public void render(MatrixStack guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);

        drawCenteredText(guiGraphics, textRenderer, title, width / 2, (23 - this.textRenderer.fontHeight) / 2, 0xFFFFFFFF);

        int scale = (int) MinecraftClient.getInstance().getWindow().getScaleFactor();
        int windowHeight = MinecraftClient.getInstance().getWindow().getHeight();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((width / 2 - 120) * scale, (windowHeight - 5 - ENTRIES_END_Y * scale), 240 * scale, (ENTRIES_END_Y + 5 - ENTRIES_START_Y) * scale);

        TranslatableText key = new TranslatableText("screen.uncrafteverything.per_item_xp_config.key");
        textRenderer.draw(guiGraphics, key, (width / 2 - 115) + (150 - textRenderer.getWidth(key)) / 2, (int) (ENTRIES_START_Y - scrollAmount), 0xFFFFFFFF);

        TranslatableText value = new TranslatableText("screen.uncrafteverything.per_item_xp_config.value");
        textRenderer.draw(guiGraphics, value, (width / 2 - 115 + 160) + (40 - textRenderer.getWidth(value)) / 2, (int) (ENTRIES_START_Y - scrollAmount), 0xFFFFFFFF);

        TranslatableText del = new TranslatableText("screen.uncrafteverything.per_item_xp_config.del");
        textRenderer.draw(guiGraphics, del, (width / 2 - 115 + 210) + (20 - textRenderer.getWidth(del)) / 2, (int) (ENTRIES_START_Y - scrollAmount), 0xFFFFFFFF);

        for (TextFieldWidget editBox : scrollableEditBoxes) {
            editBox.render(guiGraphics, mouseX, mouseY, delta);
        }
        for (ButtonWidget button : scrollableButtons) {
            button.render(guiGraphics, mouseX, mouseY, delta);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        this.children.forEach(renderable -> {
            if (renderable instanceof ButtonWidget && !scrollableButtons.contains(renderable)) {
                ((ButtonWidget) renderable).render(guiGraphics, mouseX, mouseY, delta);
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
    public void onClose() {
        this.client.openScreen(parent);
    }

    protected void renderSeparator(MatrixStack guiGraphics){
        Identifier header = new Identifier(UncraftEverything.MODID, "textures/gui/widget/header_separator.png");
        Identifier footer = new Identifier(UncraftEverything.MODID, "textures/gui/widget/footer_separator.png");
        this.client.getTextureManager().bindTexture(header);
        drawTexture(guiGraphics, 0, 25 - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        this.client.getTextureManager().bindTexture(footer);
        drawTexture(guiGraphics, 0, this.height - 65, 0.0F, 0.0F, this.width, 2, 32, 2);
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
            keyBox = new TextFieldWidget(textRenderer, x, y, 150, 20, new TranslatableText("screen.uncrafteverything.key"));
            keyBox.setText(currentKey);

            valueBox = new TextFieldWidget(textRenderer, x + 160, y, 40, 20, new TranslatableText("screen.uncrafteverything.value"));
            valueBox.setText(currentValue);
            valueBox.setTextPredicate(s -> s.matches("\\d*"));

            deleteButton = new ButtonWidget(x + 210, y, 20, 20, new TranslatableText("screen.uncrafteverything.x"),  b -> {
                entries.remove(this);
                init();
            });
        }

        void addToScreen(PerItemExpConfigScreen screen) {
            screen.addChild(keyBox);
            screen.addChild(valueBox);
            screen.addChild(deleteButton);

            screen.scrollableEditBoxes.add(keyBox);
            screen.scrollableEditBoxes.add(valueBox);
            screen.scrollableButtons.add(deleteButton);
        }
    }
}