package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.networking.ClientPayloadHandler;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UEProgressionPayload;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public class FTBQuestsProgressionConfigScreen extends AbstractScrollableScreen {
    private final Screen parent;
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

    public FTBQuestsProgressionConfigScreen(Screen parent) {
        super(new TranslatableComponent("screen.uncrafteverything.ftb_quest_progression_config"), 200);
        this.parent = parent;
    }

    @Override
    protected void init() {
        saveCurrentValues();
        this.clearWidgets();
        scrollableEditBoxes.clear();
        scrollableButtons.clear();

        if (!hasLoadedFromConfig) {
            for (Map.Entry<String, String> entry : ClientPayloadHandler.payloadFromServer.ftbQuestProgression().entrySet()) {
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
                entry.initWidgets(width / 2 - 170, y + 16);
                entry.addToScreen(this); // Add to both main widget list and scrollable lists
            }
        }

        addButton = new Button(width / 2 - 100, height - 53, 200, 20, new TranslatableComponent("screen.uncrafteverything.add_new_entry"), b -> {
            entries.add(new Entry("", ""));
            this.init();
        });
        addRenderableWidget(addButton);

        cancelButton = new Button(width / 2 - width / 3 - 10, height - 28, width / 3, 20, new TranslatableComponent("screen.uncrafteverything.cancel"), button -> onClose());
        addRenderableWidget(cancelButton);

        saveButton = new Button(width / 2 + 10, height - 28, width / 3, 20, new TranslatableComponent("screen.uncrafteverything.save"), this::saveButtonPressed);
        addRenderableWidget(saveButton);
    }

    private void saveButtonPressed(Button button){
        saveCurrentValues();
        Map<String, String> newConfig = new HashMap<>();
        for (Entry entry : entries) {
            String key = entry.currentKey.trim();
            String val = entry.currentValue.trim();
            if (!key.isEmpty() && !val.isEmpty()) {
                newConfig.put(key, val);
            }
        }

        UEProgressionPayload configPayload = new UEProgressionPayload(newConfig);
        UEProgressionPayload.INSTANCE.send(PacketDistributor.SERVER.noArg(), configPayload);
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
    public void renderBackground(PoseStack guiGraphics) {
        fillGradient(guiGraphics, 0, 0, this.width, this.height, -1072689136, -804253680);
        MinecraftForge.EVENT_BUS.post(new ScreenEvent.BackgroundDrawnEvent(this, guiGraphics));
        renderSeparator(guiGraphics);
        renderScrollbar(guiGraphics, 65);
    }

    @Override
    public void render(@NotNull PoseStack guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);

        drawCenteredString(guiGraphics, font, title, width / 2, (23 - this.font.lineHeight) / 2, 0xFFFFFFFF);

        int scale = (int) Minecraft.getInstance().getWindow().getGuiScale();
        int windowHeight = Minecraft.getInstance().getWindow().getHeight();

        RenderSystem.enableScissor(0 * scale, (windowHeight - 5 - ENTRIES_END_Y * scale), this.width * scale, (ENTRIES_END_Y - ENTRIES_START_Y + 5) * scale);

        Component key = new TranslatableComponent("screen.uncrafteverything.per_item_xp_config.key");
        font.draw(guiGraphics, key, (width / 2 - 170) + (150 - font.width(key)) / 2, (int) (ENTRIES_START_Y - scrollAmount), 0xFFFFFFFF);

        Component value = new TranslatableComponent("screen.uncrafteverything.ftb_quest_progression_config.value");
        font.draw(guiGraphics, value, (width / 2 - 170 + 160) + (150 - font.width(value)) / 2, (int) (ENTRIES_START_Y - scrollAmount), 0xFFFFFFFF);

        Component del = new TranslatableComponent("screen.uncrafteverything.per_item_xp_config.del");
        font.draw(guiGraphics, del, (width / 2 - 170 + 320) + (20 - font.width(del)) / 2, (int) (ENTRIES_START_Y - scrollAmount), 0xFFFFFFFF);

        for (EditBox editBox : scrollableEditBoxes) {
            editBox.render(guiGraphics, mouseX, mouseY, delta);
        }
        for (Button button : scrollableButtons) {
            button.render(guiGraphics, mouseX, mouseY, delta);
        }

        RenderSystem.disableScissor();

        this.renderables.forEach(renderable -> {
            if (renderable instanceof Button && !scrollableButtons.contains(renderable)) {
                renderable.render(guiGraphics, mouseX, mouseY, delta);
            }
        });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int scrollTop = 25;
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
        this.getMinecraft().setScreen(parent);
    }

    protected void renderSeparator(PoseStack poseStack){
        ResourceLocation header = new ResourceLocation(UncraftEverything.MODID, "textures/gui/widget/header_separator.png");
        ResourceLocation footer = new ResourceLocation(UncraftEverything.MODID, "textures/gui/widget/footer_separator.png");
        RenderSystem.setShaderTexture(0, header);
        blit(poseStack, 0, 25 - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        RenderSystem.setShaderTexture(0, footer);
        blit(poseStack, 0, this.height - 65, 0.0F, 0.0F, this.width, 2, 32, 2);
    }

    private class Entry {
        EditBox keyBox;
        EditBox valueBox;
        Button deleteButton;

        String currentKey;
        String currentValue;

        Entry(String key, String value) {
            this.currentKey = key;
            this.currentValue = value;
        }

        void initWidgets(int x, int y) {
            keyBox = new EditBox(font, x, y, 150, 20, new TranslatableComponent("screen.uncrafteverything.key"));
            keyBox.setValue(currentKey);

            valueBox = new EditBox(font, x + 160, y, 150, 20, new TranslatableComponent("screen.uncrafteverything.value"));
            valueBox.setValue(currentValue);

            deleteButton = new Button(x + 320, y, 20, 20, new TranslatableComponent("screen.uncrafteverything.x"), b -> {
                entries.remove(this);
                init();
            });
        }

        void addToScreen(FTBQuestsProgressionConfigScreen screen) {
            screen.addRenderableWidget(keyBox);
            screen.addRenderableWidget(valueBox);
            screen.addRenderableWidget(deleteButton);

            screen.scrollableEditBoxes.add(keyBox);
            screen.scrollableEditBoxes.add(valueBox);
            screen.scrollableButtons.add(deleteButton);
        }
    }
}