package com.coolerpromc.uncrafteverything.screen.custom;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public abstract class AbstractScrollableScreen extends Screen {
    private static final Identifier SCROLLER_SPRITE = Identifier.of("widget/scroller");
    private static final Identifier SCROLLER_BACKGROUND_SPRITE = Identifier.of("widget/scroller_background");

    protected int contentHeight;
    protected boolean scrolling = false;
    protected double scrollAmount = 0.0;

    protected AbstractScrollableScreen(Text title, int contentHeight) {
        super(title);
        this.contentHeight = contentHeight;
    }

    protected void renderScrollbar(DrawContext guiGraphics, int footerHeight) {
        int i = this.scrollBarX();
        int j = this.scrollerHeight();
        int k = this.scrollBarY();
        if (getMaxScroll() > 0){
            guiGraphics.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SCROLLER_BACKGROUND_SPRITE, i, 25, 6, this.height - footerHeight);
            guiGraphics.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SCROLLER_SPRITE, i, k, 6, j);
        }
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.getKeycode() == 264) { // Down arrow
            mouseScrolled(0, 0, 0, -1);
            return true;
        } else if (input.getKeycode() == 265) { // Up arrow
            mouseScrolled(0, 0, 0, 1);
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.x() >= scrollBarX() && click.y() >= scrollBarY() && click.y() <= scrollBarY() + scrollerHeight()) {
            this.scrolling = true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        this.scrolling = false;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double dragX, double dragY) {
        if (this.scrolling) {
            mouseScrolled(click.x(), click.y(), 0, -(dragY / 4));
        }
        return super.mouseDragged(click, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount){
        double newScroll = scrollAmount - verticalAmount * 10;
        scrollAmount = Math.max(0, Math.min(newScroll, getMaxScroll()));
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    protected abstract int scrollBarX();
    protected abstract int scrollBarY();
    protected abstract int scrollerHeight();
    protected abstract int getMaxScroll();
}