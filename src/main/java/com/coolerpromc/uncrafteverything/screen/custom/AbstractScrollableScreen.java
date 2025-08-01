package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractScrollableScreen extends Screen {
    private static final ResourceLocation SCROLLER_SPRITE = new ResourceLocation(UncraftEverything.MODID, "textures/gui/widget/scroller.png");
    private static final ResourceLocation SCROLLER_BACKGROUND_SPRITE = new ResourceLocation(UncraftEverything.MODID,"textures/gui/widget/scroller_background.png");

    protected int contentHeight;
    protected boolean scrolling = false;
    protected double scrollAmount = 0.0;

    protected AbstractScrollableScreen(Component title, int contentHeight) {
        super(title);
        this.contentHeight = contentHeight;
    }

    protected void renderScrollbar(GuiGraphics guiGraphics, int footerHeight) {
        int i = this.scrollBarX();
        int j = this.scrollerHeight();
        int k = this.scrollBarY();
        if (getMaxScroll() > 0){

            guiGraphics.blitNineSliced(SCROLLER_BACKGROUND_SPRITE, i, 25, 6, this.height - footerHeight, 6, this.height - footerHeight, 0, this.height - footerHeight, 6, 32);
            guiGraphics.fill(i, k, i + 6, k + j, 0xFFC0C0C0);
            guiGraphics.fill(i + 5, k, i + 6, k + j, 0xFF7F7F7F);
            guiGraphics.fill(i, k + j - 1, i + 6, k + j, 0xFF7F7F7F);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 264) { // Down arrow
            mouseScrolled(0, 0, -1);
            return true;
        } else if (keyCode == 265) { // Up arrow
            mouseScrolled(0, 0, 1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= scrollBarX() && mouseY >= scrollBarY() & mouseY <= scrollBarY() + scrollerHeight()) {
            this.scrolling = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.scrolling = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scrolling) {
            mouseScrolled(mouseX, mouseY, -(dragY / 4));
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta){
        double newScroll = scrollAmount - delta * 10;
        scrollAmount = Math.max(0, Math.min(newScroll, getMaxScroll()));
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    protected abstract int scrollBarX();
    protected abstract int scrollBarY();
    protected abstract int scrollerHeight();
    protected abstract int getMaxScroll();
}
