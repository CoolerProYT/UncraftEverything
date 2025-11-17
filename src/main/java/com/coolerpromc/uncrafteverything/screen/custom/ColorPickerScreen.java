package com.coolerpromc.uncrafteverything.screen.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class ColorPickerScreen extends Screen {
    private static final int HUE_BAR_WIDTH = 15;
    private static final int PICKER_SIZE = 100;
    private static final int PREVIEW_HEIGHT = 15;

    private float hue = 0f;
    private float saturation = 1f;
    private float value = 1f;

    private int pickerX, pickerY, hueBarX, hueBarY;

    private final Screen parent;
    private final Consumer<Integer> consumer;

    public ColorPickerScreen(Screen parent, Consumer<Integer> consumer) {
        super(Component.literal("HSV Color Picker"));
        this.parent = parent;
        this.consumer = consumer;
    }

    @Override
    protected void init() {
        super.init();

        pickerX = this.width / 2 - (PICKER_SIZE + HUE_BAR_WIDTH + 10) / 2;
        pickerY = this.height / 2 - PICKER_SIZE / 2;
        hueBarX = pickerX + PICKER_SIZE + 10;
        hueBarY = pickerY;

        addRenderableWidget(Button.builder(Component.literal("Confirm"), btn -> {
            int selected = hsvToRgbInt(hue, saturation, value);
            consumer.accept(selected);
            this.minecraft.setScreen(parent);
        }).bounds(this.width / 2 - 80, pickerY + PICKER_SIZE + 28, 70, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Cancel"), btn -> onClose()).bounds(this.width / 2 + 10, pickerY + PICKER_SIZE + 28, 70, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int svLeft = pickerX;
        int svTop = pickerY;
        int svBottom = pickerY + PICKER_SIZE;

        for (int x = 0; x < PICKER_SIZE; x++) {
            float s = (float) x / (float) PICKER_SIZE;
            int topColor = hsvToRgbInt(hue, s, 1f);
            int bottomColor = hsvToRgbInt(hue, s, 0f);
            graphics.fillGradient(svLeft + x, svTop, svLeft + x + 1, svBottom, topColor, bottomColor);
        }

        for (int y = 0; y < PICKER_SIZE; y += 2) {
            float hTop = 360f * (float) y / (float) PICKER_SIZE;
            float hBottom = 360f * (float) Math.min(y + 2, PICKER_SIZE) / (float) PICKER_SIZE;
            int colorTop = hsvToRgbInt(hTop, 1f, 1f);
            int colorBottom = hsvToRgbInt(hBottom, 1f, 1f);
            graphics.fillGradient(hueBarX, hueBarY + y, hueBarX + HUE_BAR_WIDTH, hueBarY + y + 2, colorTop, colorBottom);
        }

        int hueY = hueBarY + Math.round((hue / 360f) * (PICKER_SIZE));
        graphics.fill(hueBarX - 2, hueY - 2, hueBarX + HUE_BAR_WIDTH + 2, hueY + 2, 0xFF000000);
        graphics.fill(hueBarX, hueY - 1, hueBarX + HUE_BAR_WIDTH, hueY + 1, 0xFFFFFFFF);

        int previewLeft = pickerX;
        int previewTop = pickerY - PREVIEW_HEIGHT - 6;
        int previewRight = pickerX + PICKER_SIZE;
        int previewBottom = pickerY - 6;
        int previewColor = hsvToRgbInt(hue, saturation, value);
        graphics.fill(previewLeft, previewTop, previewRight, previewBottom, previewColor);
        Component colorCode = Component.literal(String.format("#%08X", previewColor));
        graphics.drawCenteredString(this.font, colorCode, previewLeft + ((previewRight - previewLeft) / 2), previewTop + this.font.lineHeight / 2, 0xFFFFFFFF);

        int selX = svLeft + Math.round(saturation * (PICKER_SIZE));
        int selY = svTop + Math.round((1.0f - value) * (PICKER_SIZE));
        graphics.fill(selX - 4, selY - 4, selX + 4, selY + 4, 0xFF000000);
        graphics.fill(selX - 2, selY - 2, selX + 2, selY + 2, previewColor);

        graphics.drawString(this.font, "Hue: " + Math.round(hue), pickerX, previewTop - 12, 0xFFFFFF);
        graphics.drawString(this.font, String.format("#%08X", previewColor), pickerX + (PICKER_SIZE / 2) - 30, previewTop - 12, 0xFFFFFF);

        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != 0) return super.mouseClicked(event, isDoubleClick);

        if (insideSV(event.x(), event.y())) {
            updateSV((float) event.x(), (float) event.y());
            return true;
        } else if (insideHue(event.x(), event.y())) {
            updateHue((float) event.y());
            return true;
        }
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double mouseX, double mouseY) {
        if (event.button() != 0) return super.mouseDragged(event, mouseX, mouseY);

        if (insideSV(event.x(), event.y())) {
            updateSV((float) event.x(), (float) event.y());
            return true;
        } else if (insideHue(event.x(), event.y())) {
            updateHue((float) event.y());
            return true;
        }

        return super.mouseDragged(event, mouseX, mouseY);
    }

    private boolean insideSV(double mx, double my) {
        return mx >= pickerX && mx <= pickerX + PICKER_SIZE && my >= pickerY && my <= pickerY + PICKER_SIZE;
    }

    private boolean insideHue(double mx, double my) {
        return mx >= hueBarX && mx <= hueBarX + HUE_BAR_WIDTH && my >= hueBarY && my <= hueBarY + PICKER_SIZE;
    }

    private void updateSV(float mouseX, float mouseY) {
        saturation = clamp01((mouseX - pickerX) / (float) PICKER_SIZE);
        value = 1f - clamp01((mouseY - pickerY) / (float) PICKER_SIZE);
    }

    private void updateHue(float mouseY) {
        hue = 360f * clamp01((mouseY - hueBarY) / (float) PICKER_SIZE);
    }

    private static float clamp01(float v) {
        if (v < 0f) return 0f;
        return Math.min(v, 1f);
    }

    private static int hsvToRgbInt(float h, float s, float v) {
        h = (h % 360f + 360f) % 360f;
        s = clamp01(s);
        v = clamp01(v);

        float c = v * s;
        float hh = h / 60f;
        float x = c * (1f - Math.abs(hh % 2f - 1f));
        float r = 0f, g = 0f, b = 0f;

        if (0f <= hh && hh < 1f) { r = c; g = x; b = 0f; }
        else if (1f <= hh && hh < 2f) { r = x; g = c; b = 0f; }
        else if (2f <= hh && hh < 3f) { r = 0f; g = c; b = x; }
        else if (3f <= hh && hh < 4f) { r = 0f; g = x; b = c; }
        else if (4f <= hh && hh < 5f) { r = x; g = 0f; b = c; }
        else { r = c; g = 0f; b = x; }

        float m = v - c;
        int ir = Math.round((r + m) * 255f);
        int ig = Math.round((g + m) * 255f);
        int ib = Math.round((b + m) * 255f);

        return 0xFF000000 | (ir << 16) | (ig << 8) | ib;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
