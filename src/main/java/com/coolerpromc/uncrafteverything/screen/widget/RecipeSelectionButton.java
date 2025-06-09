package com.coolerpromc.uncrafteverything.screen.widget;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class RecipeSelectionButton extends AbstractButton {
    private static final WidgetSprites TEXTURES = new WidgetSprites(ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID,"widget/button"), ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "widget/button_disabled"), ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID,"widget/button_highlighted"));
    protected final PressAction onPress;

    public RecipeSelectionButton(int x, int y, int width, int height, Component message, PressAction onPress) {
        super(x, y, width, height, message);
        this.onPress = onPress;
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        Minecraft minecraftClient = Minecraft.getInstance();
        context.blitSprite(TEXTURES.get(this.active, this.isFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());
        int i = this.active ? 16777215 : 10526880;
        this.renderString(context, minecraftClient.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
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
}