package com.coolerpromc.uncrafteverything.screen.widget;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.screen.custom.UEClientConfigScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

import java.util.function.BiConsumer;

@SuppressWarnings("all")
public class ColorPickerWidget extends AbstractWidget {
    private static final int HUE_BAR_WIDTH = 15;
    public static final int PICKER_SIZE = 100;
    private static final int PREVIEW_HEIGHT = 15;
    private static final int BUTTON_HEIGHT = 20;
    public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    public static final ResourceLocation MOD_WIDGETS_LOCATION = new ResourceLocation(UncraftEverything.MODID, "textures/gui/icons.png");

    private float hue = 0f;
    private float saturation = 1f;
    private float value = 1f;

    private final BiConsumer<Integer, ColorPickerWidget> consumer;
    private final UEClientConfigScreen parent;

    public boolean hasOverlay = false;
    private boolean draggingHue = false;
    private boolean draggingSV = false;
    private boolean isVisible = false;
    private int buttonWidth;
    private boolean isHoveredOrFocused = false;
    private boolean isCancelHoveredOrFocused = false;
    private boolean isSaveHoveredOrFocused = false;
    private int color;

    public ColorPickerWidget(int x, int y, int buttonWidth, int color, Component message, UEClientConfigScreen parent, BiConsumer<Integer, ColorPickerWidget> consumer) {
        super(x, y, Minecraft.getInstance().getWindow().getGuiScaledWidth(), PICKER_SIZE + 60, message);
        this.consumer = consumer;
        this.parent = parent;
        this.buttonWidth = buttonWidth;
        this.color = color;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        this.isHoveredOrFocused = mouseX >= screenWidth / 2 + 10 && mouseX <= screenWidth / 2 + 10 + buttonWidth && mouseY >= this.getY() && mouseY <= this.getY() + BUTTON_HEIGHT && !isVisible && !hasOverlay;

        graphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        graphics.blitNineSliced(MOD_WIDGETS_LOCATION, screenWidth / 2 + 10, this.getY(), buttonWidth, BUTTON_HEIGHT, 20, 4, 200, 20, 0, getButtonY());
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = FastColor.ABGR32.color(1, getFGColor());
        graphics.fill(screenWidth / 2 + 10 + 1, this.getY() + 1, screenWidth / 2 + 10 + buttonWidth - 1, this.getY() + BUTTON_HEIGHT - 1, FastColor.ABGR32.color(0xDD, color));
        graphics.drawCenteredString(minecraft.font, Component.literal(String.format("#%08X", color)), screenWidth / 2 + 10 + buttonWidth / 2, this.getY() + BUTTON_HEIGHT / 2 - minecraft.font.lineHeight / 2, i);

        if (!this.isVisible) return;

        int pickerX = screenWidth / 2 - (PICKER_SIZE + 10 + HUE_BAR_WIDTH) / 2;
        int pickerY = screenHeight / 2 - (PICKER_SIZE + PREVIEW_HEIGHT + 6) / 2;
        int hueBarX = pickerX + PICKER_SIZE + 10;
        int hueBarY = pickerY;

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 500);

        graphics.fillGradient(0, 0, screenWidth, screenHeight, -1072689136, -804253680);

        graphics.blitNineSliced(
                MOD_WIDGETS_LOCATION,
                screenWidth / 2 - 130,
                screenHeight / 2 - 100,
                260,
                180,
                3,
                3,
                176,
                15,
                0,
                40
        );

        graphics.drawCenteredString(minecraft.font, this.getMessage() , screenWidth / 2, screenHeight / 2 - 100 + 2 + minecraft.font.lineHeight / 2, FastColor.ABGR32.color(0xFF, ChatFormatting.WHITE.getColor()));

        // --- SV (Saturation/Value) Square ---
        for (int x = 0; x < PICKER_SIZE; x++) {
            float s = (float) x / (float) PICKER_SIZE;
            int topColor = hsvToRgbInt(hue, s, 1f);
            int bottomColor = hsvToRgbInt(hue, s, 0f);
            graphics.fillGradient(pickerX + x, pickerY, pickerX + x + 1, pickerY + PICKER_SIZE, topColor, bottomColor);
        }

        // --- Hue Bar ---
        for (int y = 0; y < PICKER_SIZE; y += 2) {
            float hTop = 360f * ((float) y / (float) PICKER_SIZE);
            float hBottom = 360f * ((float) Math.min(y + 2, PICKER_SIZE) / (float) PICKER_SIZE);
            int colorTop = hsvToRgbInt(hTop, 1f, 1f);
            int colorBottom = hsvToRgbInt(hBottom, 1f, 1f);
            graphics.fillGradient(hueBarX, hueBarY + y, hueBarX + HUE_BAR_WIDTH, hueBarY + y + 2, colorTop, colorBottom);
        }
        // --- Hue selector marker ---
        int hueY = hueBarY + Math.round((hue / 360f) * (PICKER_SIZE));
        graphics.fill(hueBarX - 2, hueY - 2, hueBarX + HUE_BAR_WIDTH + 2, hueY + 2, 0xFF000000);
        graphics.fill(hueBarX, hueY - 1, hueBarX + HUE_BAR_WIDTH, hueY + 1, 0xFFFFFFFF);

        // --- Preview Bar ---
        int previewLeft = pickerX;
        int previewTop = pickerY - PREVIEW_HEIGHT - 6;
        int previewRight = pickerX + PICKER_SIZE;
        int previewBottom = pickerY - 6;
        int previewColor = hsvToRgbInt(hue, saturation, value);
        graphics.fill(previewLeft, previewTop, previewRight, previewBottom, previewColor);

        // --- Selected Point Marker ---
        int selX = pickerX + Math.round(saturation * (PICKER_SIZE));
        int selY = pickerY + Math.round((1.0f - value) * (PICKER_SIZE));
        graphics.fill(selX - 4, selY - 4, selX + 4, selY + 4, 0xFF000000);
        graphics.fill(selX - 2, selY - 2, selX + 2, selY + 2, previewColor);

        // --- Buttons ---
        double scale = Minecraft.getInstance().getWindow().getGuiScale();

        int scaledButtonWidth = 260 / 2 - 10;

        int btnY = screenHeight / 2 + PICKER_SIZE / 2;
        int confirmX = screenWidth / 2 + 5;
        int cancelX = screenWidth / 2 - scaledButtonWidth - 5;

        this.isSaveHoveredOrFocused = mouseX >= confirmX && mouseX <= confirmX + scaledButtonWidth && mouseY >= btnY && mouseY <= btnY + BUTTON_HEIGHT;
        this.isCancelHoveredOrFocused = mouseX >= cancelX && mouseX <= cancelX + scaledButtonWidth && mouseY >= btnY && mouseY <= btnY + BUTTON_HEIGHT;

        graphics.blitNineSliced(WIDGETS_LOCATION, confirmX, btnY, scaledButtonWidth, BUTTON_HEIGHT, 3, 3, 200, 20, 0, getConfirmY());
        graphics.drawCenteredString(Minecraft.getInstance().font, "Confirm", confirmX + (scaledButtonWidth / 2), btnY + BUTTON_HEIGHT / 2 - minecraft.font.lineHeight / 2, 0xFFFFFFFF);

        graphics.blitNineSliced(WIDGETS_LOCATION, cancelX, btnY, scaledButtonWidth, BUTTON_HEIGHT, 3, 3, 200, 20, 0, getCancelY());
        graphics.drawCenteredString(Minecraft.getInstance().font, "Cancel", cancelX + (scaledButtonWidth / 2), btnY + BUTTON_HEIGHT / 2 - minecraft.font.lineHeight / 2, 0xFFFFFFFF);
    }

    private int getButtonY() {
        int i = 0;
        if (!this.active) {
            i = 0;
        } else if (this.isHoveredOrFocused) {
            i = 1;
        }

        return i * 20;
    }

    private int getConfirmY() {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (this.isSaveHoveredOrFocused) {
            i = 2;
        }

        return 46 + i * 20;
    }

    private int getCancelY() {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (this.isCancelHoveredOrFocused) {
            i = 2;
        }

        return 46 + i * 20;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Minecraft minecraft = Minecraft.getInstance();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        if (mouseX >= screenWidth / 2 + 10 && mouseX <= screenWidth / 2 + 10 + buttonWidth && mouseY >= this.getY() && mouseY <= this.getY() + BUTTON_HEIGHT && !isVisible){
            playDownSound(minecraft.getSoundManager());
            this.isVisible = true;
            parent.redraw();
            rgbToHsv(color);
            return true;
        }

        if (button != 0 || !this.isVisible) return false;
        onRelease(mouseX, mouseY);

        double mx = mouseX;
        double my = mouseY;

        int pickerX = screenWidth / 2 - (PICKER_SIZE + 10 + HUE_BAR_WIDTH) / 2;
        int pickerY = screenHeight / 2 - (PICKER_SIZE + PREVIEW_HEIGHT + 6) / 2;
        int hueBarX = pickerX + PICKER_SIZE + 10;
        int hueBarY = pickerY;

        if (insideSV(mx, my, pickerX, pickerY)) {
            updateSV((float) mx, (float) my, pickerX, pickerY);
            draggingSV = true;
            return true;
        } else if (insideHue(mx, my, hueBarX, hueBarY)) {
            updateHue((float) my, hueBarY);
            draggingHue = true;
            return true;
        }

        int btnY = screenHeight / 2 + PICKER_SIZE / 2;
        int confirmX = screenWidth / 2 + 10;
        int cancelX = screenWidth / 2 - buttonWidth / 2 - 10;

        if (this.isSaveHoveredOrFocused) {
            playDownSound(minecraft.getSoundManager());
            confirm();
            return true;
        }

        if (this.isCancelHoveredOrFocused) {
            playDownSound(minecraft.getSoundManager());
            this.isVisible = false;
            parent.redraw();
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button != 0 || !this.isVisible) return false;

        Minecraft minecraft = Minecraft.getInstance();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        int pickerX = screenWidth / 2 - (PICKER_SIZE + 10 + HUE_BAR_WIDTH) / 2;
        int pickerY = screenHeight / 2 - (PICKER_SIZE + PREVIEW_HEIGHT + 6) / 2;
        int hueBarX = pickerX + PICKER_SIZE + 10;
        int hueBarY = pickerY;

        if (draggingSV) {
            updateSV((float) mouseX, (float) mouseY, pickerX, pickerY);
            return true;
        } else if (draggingHue) {
            updateHue((float) mouseY, hueBarY);
            return true;
        }
        return false;
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        draggingSV = false;
        draggingHue = false;
    }

    public void confirm() {
        int color = hsvToRgbInt(hue, saturation, value);
        consumer.accept(color, this);
        this.setColor(color);
        this.isVisible = false;
        parent.redraw();
    }

    private boolean insideSV(double mx, double my, int x, int y) {
        return mx >= x && mx <= x + PICKER_SIZE && my >= y && my <= y + PICKER_SIZE;
    }

    private boolean insideHue(double mx, double my, int x, int y) {
        return mx >= x && mx <= x + HUE_BAR_WIDTH && my >= y && my <= y + PICKER_SIZE;
    }

    private void updateSV(float mouseX, float mouseY, int x, int y) {
        saturation = clamp01((mouseX - x) / (float) PICKER_SIZE);
        value = 1f - clamp01((mouseY - y) / (float) PICKER_SIZE);
    }

    private void updateHue(float mouseY, int y) {
        hue = 360f * clamp01((mouseY - y) / (float) PICKER_SIZE);
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static int hsvToRgbInt(float h, float s, float v) {
        h = (h % 360f + 360f) % 360f;
        s = clamp01(s);
        v = clamp01(v);

        float c = v * s;
        float hh = h / 60f;
        float x = c * (1f - Math.abs(hh % 2f - 1f));
        float r = 0f, g = 0f, b = 0f;

        if (0f <= hh && hh < 1f) { r = c; g = x; }
        else if (1f <= hh && hh < 2f) { r = x; g = c; }
        else if (2f <= hh && hh < 3f) { g = c; b = x; }
        else if (3f <= hh && hh < 4f) { g = x; b = c; }
        else if (4f <= hh && hh < 5f) { r = x; b = c; }
        else { r = c; b = x; }

        float m = v - c;
        int ir = Math.round((r + m) * 255f);
        int ig = Math.round((g + m) * 255f);
        int ib = Math.round((b + m) * 255f);

        return 0xFF000000 | (ir << 16) | (ig << 8) | ib;
    }

    private void rgbToHsv(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        float rf = r / 255f;
        float gf = g / 255f;
        float bf = b / 255f;

        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;

        this.value = max;

        if (max == 0f) {
            this.saturation = 0f;
        } else {
            this.saturation = delta / max;
        }

        if (delta == 0f) {
            this.hue = 0f;
        } else if (max == rf) {
            this.hue = 60f * (((gf - bf) / delta) % 6f);
        } else if (max == gf) {
            this.hue = 60f * (((bf - rf) / delta) + 2f);
        } else {
            this.hue = 60f * (((rf - gf) / delta) + 4f);
        }

        if (this.hue < 0f) {
            this.hue += 360f;
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    public void setButtonWidth(int buttonWidth) {
        this.buttonWidth = buttonWidth;
    }

    @Override
    public boolean isHoveredOrFocused() {
        return isHoveredOrFocused;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setHasOverlay(boolean hasOverlay) {
        this.hasOverlay = hasOverlay;
    }
}
