package com.coolerpromc.uncrafteverything.screen.widget;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AmountWidget extends AbstractWidget {
    private int value;
    private final Font font;

    public AmountWidget(Font font, int x, int y, int width, int height, Component message, int value) {
        super(x, y, width, height, message);
        this.font = font;
        this.value = value;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF8B8B8B);
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width - 1, this.getY() + this.height - 1, 0xFF373737);
        guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width, this.getY() + this.height, 0xFFFFFFFF);
        guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, this.isHovered ? 0xFFA4A4A4 : 0xFF8B8B8B);
        guiGraphics.centeredText(this.font, Component.literal(String.valueOf(value)), this.getX() + this.width / 2, this.getY() + this.height / 2 - font.lineHeight / 2, 0xFFFFFFFF);
        if (this.isHovered){
            List<Component> tooltips = new ArrayList<>();
            tooltips.add(Component.translatable("screen.uncrafteverything.tooltip.amount_info").withStyle(ChatFormatting.BLUE));
            tooltips.add(Component.translatable("screen.uncrafteverything.tooltip.scroll_info").withStyle(ChatFormatting.GRAY));
            guiGraphics.setTooltipForNextFrame(this.font, tooltips, Optional.empty(), mouseX, mouseY);
            guiGraphics.requestCursor(CursorTypes.RESIZE_NS);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        createNarrationMessage();
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return false;
    }
}
