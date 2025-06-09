package com.coolerpromc.uncrafteverything.screen.widget;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;

public class RecipeSelectionButton extends AbstractButton {
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation(UncraftEverything.MODID,"textures/gui/widgets.png");
    protected final PressAction onPress;

    public RecipeSelectionButton(int x, int y, int width, int height, StringTextComponent message, PressAction onPress) {
        super(x, y, width, height, message);
        this.onPress = onPress;
    }

    @Override
    public void renderButton(MatrixStack pGuiGraphics, int mouseX, int mouseY, float deltaTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        FontRenderer fontrenderer = minecraft.font;
        minecraft.getTextureManager().bind(WIDGETS_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(pGuiGraphics, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
        this.blit(pGuiGraphics, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
        this.renderBg(pGuiGraphics, minecraft, mouseX, mouseY);
        int j = this.getFGColor();
        drawCenteredString(pGuiGraphics, fontrenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }

    @Override
    public void onPress() {
        if (this.onPress != null) {
            this.onPress.onPress(this);
        }
    }

    public interface PressAction {
        void onPress(RecipeSelectionButton button);
    }

    public void setFocused(boolean p_230996_1_) {
        super.setFocused(p_230996_1_);
    }
}