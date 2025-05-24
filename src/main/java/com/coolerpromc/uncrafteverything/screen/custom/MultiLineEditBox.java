package com.coolerpromc.uncrafteverything.screen.custom;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiLineEditBox extends AbstractGui implements IGuiEventListener, IRenderable {
    private final FontRenderer fontRenderer;
    private final List<String> lines;
    private final int x, y, width, height;
    private int maxLines;
    private int scrollOffset = 0;
    private int cursorLine = 0;
    private int cursorPos = 0;
    private boolean isFocused = false;
    private int maxLength = 32;

    // Key repeat handling
    private int lastPressedKey = -1;
    private long lastKeyPressTime = 0;
    private long keyRepeatDelay = 500; // Initial delay in milliseconds
    private long keyRepeatRate = 50;   // Repeat rate in milliseconds
    private boolean keyRepeating = false;

    public MultiLineEditBox(FontRenderer fontRenderer, int x, int y, int width, int height, int maxLines) {
        this.fontRenderer = fontRenderer;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.maxLines = maxLines;
        this.lines = new ArrayList<>();
        this.lines.add(""); // Start with one empty line
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Handle key repeat
        handleKeyRepeat();

        // Draw background
        fill(matrixStack, x, y, x + width, y + height, isFocused ? 0xFFFFFFFF : 0xFFAAAAAA);
        fill(matrixStack, x + 1, y + 1, x + width - 1, y + height - 1, 0xFF000000);

        // Calculate visible lines
        int lineHeight = fontRenderer.lineHeight + 2;
        int visibleLines = (height - 4) / lineHeight;

        // Draw text lines
        for (int i = 0; i < Math.min(visibleLines, lines.size() - scrollOffset); i++) {
            int lineIndex = i + scrollOffset;
            if (lineIndex < lines.size()) {
                String line = lines.get(lineIndex);
                fontRenderer.drawShadow(matrixStack, line, x + 2, y + 2 + i * lineHeight, 0xFFE0E0E0);

                // Draw cursor
                if (isFocused && lineIndex == cursorLine) {
                    String beforeCursor = line.substring(0, Math.min(cursorPos, line.length()));
                    int cursorX = x + 2 + fontRenderer.width(beforeCursor);
                    int cursorY = y + 2 + i * lineHeight;
                    fill(matrixStack, cursorX, cursorY, cursorX + 1, cursorY + fontRenderer.lineHeight, 0xFFE0E0E0);
                }
            }
        }

        // Draw scrollbar if needed
        if (lines.size() > visibleLines) {
            drawScrollbar(matrixStack, visibleLines);
        }
    }

    private void handleKeyRepeat() {
        if (!isFocused || lastPressedKey == -1) return;

        long currentTime = System.currentTimeMillis();
        long timeSincePress = currentTime - lastKeyPressTime;

        if (!keyRepeating && timeSincePress >= keyRepeatDelay) {
            keyRepeating = true;
            lastKeyPressTime = currentTime;
        } else if (keyRepeating && timeSincePress >= keyRepeatRate) {
            // Execute the key action again
            executeKeyAction(lastPressedKey);
            lastKeyPressTime = currentTime;
        }
    }

    private void executeKeyAction(int keyCode) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_BACKSPACE:
                handleBackspace();
                ensureCursorVisible();
                break;
            case GLFW.GLFW_KEY_DELETE:
                handleDelete();
                break;
            case GLFW.GLFW_KEY_UP:
                moveCursorUp();
                ensureCursorVisible();
                break;
            case GLFW.GLFW_KEY_DOWN:
                moveCursorDown();
                ensureCursorVisible();
                break;
            case GLFW.GLFW_KEY_LEFT:
                moveCursorLeft();
                ensureCursorVisible();
                break;
            case GLFW.GLFW_KEY_RIGHT:
                moveCursorRight();
                ensureCursorVisible();
                break;
        }
    }

    private void drawScrollbar(MatrixStack matrixStack, int visibleLines) {
        int scrollbarX = x + width - 6;
        int scrollbarY = y + 1;
        int scrollbarHeight = height - 2;

        // Scrollbar background
        fill(matrixStack, scrollbarX, scrollbarY, scrollbarX + 5, scrollbarY + scrollbarHeight, 0xFF333333);

        // Scrollbar thumb
        if (lines.size() > visibleLines) {
            int thumbHeight = Math.max(10, (scrollbarHeight * visibleLines) / lines.size());
            int thumbY = scrollbarY + (scrollOffset * (scrollbarHeight - thumbHeight)) / (lines.size() - visibleLines);
            fill(matrixStack, scrollbarX + 1, thumbY, scrollbarX + 4, thumbY + thumbHeight, 0xFF888888);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isFocused) return false;

        // Start key repeat tracking for repeatable keys
        if (isRepeatableKey(keyCode)) {
            lastPressedKey = keyCode;
            lastKeyPressTime = System.currentTimeMillis();
            keyRepeating = false;
        }

        switch (keyCode) {
            case GLFW.GLFW_KEY_ENTER:
                insertNewLine();
                ensureCursorVisible();
                return true;
            case GLFW.GLFW_KEY_BACKSPACE:
                handleBackspace();
                ensureCursorVisible();
                return true;
            case GLFW.GLFW_KEY_DELETE:
                handleDelete();
                return true;
            case GLFW.GLFW_KEY_UP:
                moveCursorUp();
                ensureCursorVisible();
                return true;
            case GLFW.GLFW_KEY_DOWN:
                moveCursorDown();
                ensureCursorVisible();
                return true;
            case GLFW.GLFW_KEY_LEFT:
                moveCursorLeft();
                ensureCursorVisible();
                return true;
            case GLFW.GLFW_KEY_RIGHT:
                moveCursorRight();
                ensureCursorVisible();
                return true;
            case GLFW.GLFW_KEY_PAGE_UP:
                scrollUp(getVisibleLines() - 1);
                return true;
            case GLFW.GLFW_KEY_PAGE_DOWN:
                scrollDown(getVisibleLines() - 1);
                return true;
            case GLFW.GLFW_KEY_HOME:
                if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                    // Ctrl+Home - go to beginning of document
                    cursorLine = 0;
                    cursorPos = 0;
                    ensureCursorVisible();
                } else {
                    // Home - go to beginning of line
                    cursorPos = 0;
                }
                return true;
            case GLFW.GLFW_KEY_END:
                if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                    // Ctrl+End - go to end of document
                    cursorLine = lines.size() - 1;
                    cursorPos = lines.get(cursorLine).length();
                    ensureCursorVisible();
                } else {
                    // End - go to end of line
                    cursorPos = lines.get(cursorLine).length();
                }
                return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        // Stop key repeat when key is released
        if (keyCode == lastPressedKey) {
            lastPressedKey = -1;
            keyRepeating = false;
        }
        return false;
    }

    private boolean isRepeatableKey(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_BACKSPACE ||
                keyCode == GLFW.GLFW_KEY_DELETE ||
                keyCode == GLFW.GLFW_KEY_UP ||
                keyCode == GLFW.GLFW_KEY_DOWN ||
                keyCode == GLFW.GLFW_KEY_LEFT ||
                keyCode == GLFW.GLFW_KEY_RIGHT;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!isFocused) return false;

        if (Character.isValidCodePoint(codePoint) && !Character.isISOControl(codePoint)) {
            insertCharacter(codePoint);
            ensureCursorVisible();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean wasInBounds = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        setFocused(wasInBounds);

        if (wasInBounds && button == 0) {
            // Check if clicking on scrollbar
            if (mouseX >= x + width - 6 && lines.size() > getVisibleLines()) {
                handleScrollbarClick(mouseY);
                return true;
            }

            // Calculate cursor position from mouse click
            int lineHeight = fontRenderer.lineHeight + 2;
            int clickedLine = (int) ((mouseY - y - 2) / lineHeight) + scrollOffset;

            if (clickedLine >= 0 && clickedLine < lines.size()) {
                cursorLine = clickedLine;
                String line = lines.get(clickedLine);
                int clickX = (int) mouseX - x - 2;

                // Find closest character position
                cursorPos = 0;
                for (int i = 0; i <= line.length(); i++) {
                    int charX = fontRenderer.width(line.substring(0, i));
                    if (charX > clickX) {
                        break;
                    }
                    cursorPos = i;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (isMouseOver(mouseX, mouseY)) {
            if (delta > 0) {
                scrollUp(3); // Scroll up 3 lines
            } else if (delta < 0) {
                scrollDown(3); // Scroll down 3 lines
            }
            return true;
        }
        return false;
    }

    private void handleScrollbarClick(double mouseY) {
        int scrollbarY = y + 1;
        int scrollbarHeight = height - 2;
        int visibleLines = getVisibleLines();

        if (lines.size() > visibleLines) {
            double clickRatio = (mouseY - scrollbarY) / scrollbarHeight;
            int targetLine = (int) (clickRatio * (lines.size() - visibleLines));
            scrollOffset = MathHelper.clamp(targetLine, 0, lines.size() - visibleLines);
        }
    }

    private void scrollUp(int amount) {
        scrollOffset = Math.max(0, scrollOffset - amount);
    }

    private void scrollDown(int amount) {
        int visibleLines = getVisibleLines();
        int maxScroll = Math.max(0, lines.size() - visibleLines);
        scrollOffset = Math.min(maxScroll, scrollOffset + amount);
    }

    private void ensureCursorVisible() {
        int visibleLines = getVisibleLines();

        // If cursor is above visible area
        if (cursorLine < scrollOffset) {
            scrollOffset = cursorLine;
        }
        // If cursor is below visible area
        else if (cursorLine >= scrollOffset + visibleLines) {
            scrollOffset = cursorLine - visibleLines + 1;
        }

        // Clamp scroll offset
        int maxScroll = Math.max(0, lines.size() - visibleLines);
        scrollOffset = MathHelper.clamp(scrollOffset, 0, maxScroll);
    }

    private int getVisibleLines() {
        int lineHeight = fontRenderer.lineHeight + 2;
        return (height - 4) / lineHeight;
    }

    private void insertCharacter(char c) {
        if (cursorLine < lines.size()) {
            String line = lines.get(cursorLine);
            if (line.length() < maxLength) {
                String newLine = line.substring(0, cursorPos) + c + line.substring(cursorPos);
                lines.set(cursorLine, newLine);
                cursorPos++;
            }
        }
    }

    private void insertNewLine() {
        if (lines.size() < maxLines) {
            String currentLine = lines.get(cursorLine);
            String beforeCursor = currentLine.substring(0, cursorPos);
            String afterCursor = currentLine.substring(cursorPos);

            lines.set(cursorLine, beforeCursor);
            lines.add(cursorLine + 1, afterCursor);

            cursorLine++;
            cursorPos = 0;
        }
    }

    private void handleBackspace() {
        if (cursorPos > 0) {
            String line = lines.get(cursorLine);
            String newLine = line.substring(0, cursorPos - 1) + line.substring(cursorPos);
            lines.set(cursorLine, newLine);
            cursorPos--;
        } else if (cursorLine > 0) {
            // Merge with previous line
            String currentLine = lines.get(cursorLine);
            String previousLine = lines.get(cursorLine - 1);
            cursorPos = previousLine.length();
            lines.set(cursorLine - 1, previousLine + currentLine);
            lines.remove(cursorLine);
            cursorLine--;
        }
    }

    private void handleDelete() {
        String line = lines.get(cursorLine);
        if (cursorPos < line.length()) {
            String newLine = line.substring(0, cursorPos) + line.substring(cursorPos + 1);
            lines.set(cursorLine, newLine);
        } else if (cursorLine < lines.size() - 1) {
            // Merge with next line
            String nextLine = lines.get(cursorLine + 1);
            lines.set(cursorLine, line + nextLine);
            lines.remove(cursorLine + 1);
        }
    }

    private void moveCursorUp() {
        if (cursorLine > 0) {
            cursorLine--;
            cursorPos = Math.min(cursorPos, lines.get(cursorLine).length());
        }
    }

    private void moveCursorDown() {
        if (cursorLine < lines.size() - 1) {
            cursorLine++;
            cursorPos = Math.min(cursorPos, lines.get(cursorLine).length());
        }
    }

    private void moveCursorLeft() {
        if (cursorPos > 0) {
            cursorPos--;
        } else if (cursorLine > 0) {
            cursorLine--;
            cursorPos = lines.get(cursorLine).length();
        }
    }

    private void moveCursorRight() {
        String line = lines.get(cursorLine);
        if (cursorPos < line.length()) {
            cursorPos++;
        } else if (cursorLine < lines.size() - 1) {
            cursorLine++;
            cursorPos = 0;
        }
    }

    public void setFocused(boolean focused) {
        this.isFocused = focused;
        if (!focused) {
            // Stop key repeat when losing focus
            lastPressedKey = -1;
            keyRepeating = false;
        }
    }

    public String getText() {
        return String.join("\n", lines);
    }

    public void setText(String text) {
        lines.clear();
        String[] textLines = text.split("\n");
        Collections.addAll(lines, textLines);
        if (lines.isEmpty()) {
            lines.add("");
        }
        cursorLine = 0;
        cursorPos = 0;
        scrollOffset = 0;
    }

    @Override
    public boolean changeFocus(boolean focus) {
        setFocused(focus);
        return focus;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    // Utility methods for external control
    public void scrollToTop() {
        scrollOffset = 0;
    }

    public void scrollToBottom() {
        int visibleLines = getVisibleLines();
        scrollOffset = Math.max(0, lines.size() - visibleLines);
    }

    public void scrollToLine(int lineNumber) {
        int visibleLines = getVisibleLines();
        scrollOffset = MathHelper.clamp(lineNumber - visibleLines / 2, 0, Math.max(0, lines.size() - visibleLines));
    }

    // Setters for customizing key repeat behavior
    public void setKeyRepeatDelay(long delayMs) {
        this.keyRepeatDelay = delayMs;
    }

    public void setKeyRepeatRate(long rateMs) {
        this.keyRepeatRate = rateMs;
    }
}