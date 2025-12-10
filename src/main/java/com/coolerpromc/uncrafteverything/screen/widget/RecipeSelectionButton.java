package com.coolerpromc.uncrafteverything.screen.widget;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.NotNull;

public class RecipeSelectionButton extends AbstractButton {
    private static final WidgetSprites SPRITES = new WidgetSprites(Identifier.fromNamespaceAndPath(UncraftEverything.MODID,"widget/button"), Identifier.fromNamespaceAndPath(UncraftEverything.MODID, "widget/button_disabled"), Identifier.fromNamespaceAndPath(UncraftEverything.MODID,"widget/button_highlighted"));
    protected final PressAction onPress;

    public RecipeSelectionButton(int x, int y, int width, int height, Component message, PressAction onPress) {
        super(x, y, width, height, message);
        this.onPress = onPress;
    }

    @Override
    protected void renderContents(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        context.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                SPRITES.get(this.active, this.isHoveredOrFocused()),
                this.getX(),
                this.getY(),
                this.getWidth(),
                this.getHeight(),
                ARGB.white(this.alpha)
        );
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    public void onPress(InputWithModifiers modifiers) {
        if (this.onPress != null) {
            this.onPress.onPress(this);
        }
    }

    public interface PressAction {
        void onPress(RecipeSelectionButton button);
    }
}