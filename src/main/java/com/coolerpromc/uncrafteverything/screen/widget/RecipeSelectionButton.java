package com.coolerpromc.uncrafteverything.screen.widget;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

public class RecipeSelectionButton extends PressableWidget {
    private static final ButtonTextures TEXTURES = new ButtonTextures(Identifier.of(UncraftEverything.MODID,"widget/button"), Identifier.of(UncraftEverything.MODID, "widget/button_disabled"), Identifier.of(UncraftEverything.MODID,"widget/button_highlighted"));
    protected final PressAction onPress;

    public RecipeSelectionButton(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message);
        this.onPress = onPress;
    }

    @Override
    public void onPress(AbstractInput input) {
        if (this.onPress != null) {
            this.onPress.onPress(this);
        }
    }

    @Override
    protected void drawIcon(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TEXTURES.get(this.active, this.isSelected()), this.getX(), this.getY(), this.getWidth(), this.getHeight(), ColorHelper.getWhite(this.alpha));
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    public interface PressAction {
        void onPress(RecipeSelectionButton button);
    }
}
