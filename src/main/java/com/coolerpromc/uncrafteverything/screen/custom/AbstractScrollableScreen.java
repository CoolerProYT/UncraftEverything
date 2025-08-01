package com.coolerpromc.uncrafteverything.screen.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class AbstractScrollableScreen extends Screen {
    protected int contentHeight;
    protected boolean scrolling = false;
    protected double scrollAmount = 0.0;

    protected AbstractScrollableScreen(Component title, int contentHeight) {
        super(title);
        this.contentHeight = contentHeight;
    }

    protected void renderScrollbar(PoseStack pPoseStack, int footerHeight) {
        int i = this.scrollBarX();
        int j = this.scrollerHeight();
        int k = this.scrollBarY();
        if (getMaxScroll() > 0){
            fill(pPoseStack, i, 25, i + 6, this.height - footerHeight, 0xFF000000);
            fill(pPoseStack, i, k, i + 6, k + j, 0xFFC0C0C0);
            fill(pPoseStack, i + 5, k, i + 6, k + j, 0xFF7F7F7F);
            fill(pPoseStack, i, k + j - 1, i + 6, k + j, 0xFF7F7F7F);
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
