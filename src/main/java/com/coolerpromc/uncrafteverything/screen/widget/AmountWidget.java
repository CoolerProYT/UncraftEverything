package com.coolerpromc.uncrafteverything.screen.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AmountWidget extends ClickableWidget {
    private int value;
    private final TextRenderer font;

    public AmountWidget(TextRenderer font, int x, int y, int width, int height, Text message, int value) {
        super(x, y, width, height, message);
        this.font = font;
        this.value = value;
    }

    @Override
    protected void renderWidget(DrawContext guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF8B8B8B);
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width - 1, this.getY() + this.height - 1, 0xFF373737);
        guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width, this.getY() + this.height, 0xFFFFFFFF);
        guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, this.isHovered() ? 0xFFA4A4A4 : 0xFF8B8B8B);
        guiGraphics.drawCenteredTextWithShadow(this.font, Text.literal(String.valueOf(value)), this.getX() + this.width / 2, this.getY() + this.height / 2 - font.fontHeight / 2, 0xFFFFFFFF);
        if (this.isHovered()){
            List<Text> tooltips = new ArrayList<>();
            tooltips.add(Text.literal("Amount to add/remove").formatted(Formatting.BLUE));
            tooltips.add(Text.literal("Scroll to change value").formatted(Formatting.GRAY));
            guiGraphics.drawTooltip(this.font, tooltips, Optional.empty(), mouseX, mouseY);
            guiGraphics.setCursor(StandardCursors.RESIZE_NS);
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder narrationElementOutput) {
        getNarrationMessage();
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public boolean mouseClicked(Click event, boolean isDoubleClick) {
        return false;
    }
}