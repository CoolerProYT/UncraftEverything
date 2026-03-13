package com.coolerpromc.uncrafteverything.screen.custom;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public abstract class AbstractScrollableScreen extends Screen {
    private static final Identifier SCROLLER_SPRITE = Identifier.parse("widget/scroller");
    private static final Identifier SCROLLER_BACKGROUND_SPRITE = Identifier.parse("widget/scroller_background");

    protected int contentHeight;
    protected boolean scrolling = false;
    protected double scrollAmount = 0.0;

    protected AbstractScrollableScreen(Component title, int contentHeight) {
        super(title);
        this.contentHeight = contentHeight;
    }

    protected void renderScrollbar(GuiGraphicsExtractor guiGraphics, int footerHeight) {
        int i = this.scrollBarX();
        int j = this.scrollerHeight();
        int k = this.scrollBarY();
        if (getMaxScroll() > 0){
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_BACKGROUND_SPRITE, i, 25, 6, this.height - footerHeight);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_SPRITE, i, k, 6, j);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.input() == 264) { // Down arrow
            mouseScrolled(0, 0, 0, -1);
            return true;
        } else if (input.input() == 265) { // Up arrow
            mouseScrolled(0, 0, 0, 1);
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.x() >= scrollBarX() && click.y() >= scrollBarY() && click.y() <= scrollBarY() + scrollerHeight()) {
            this.scrolling = true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        this.scrolling = false;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double dragX, double dragY) {
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