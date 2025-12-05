package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.blockentity.custom.AutoUncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.networking.AmountToAddPayload;
import com.coolerpromc.uncrafteverything.networking.ExpTransferPayload;
import com.coolerpromc.uncrafteverything.networking.SelectedIndexSyncPayload;
import com.coolerpromc.uncrafteverything.networking.TypeChangePayload;
import com.coolerpromc.uncrafteverything.screen.widget.AmountWidget;
import com.coolerpromc.uncrafteverything.screen.widget.RecipeSelectionButton;
import com.coolerpromc.uncrafteverything.screen.widget.TypeWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AutoUncraftingTableScreen extends AbstractUncraftingScreen<AutoUncraftingTableBlockEntity, AutoUncraftingTableMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "textures/gui/uncrafting_table_gui.png");
    private static final ResourceLocation EXPERIENCE_BAR_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("hud/experience_bar_background");
    private static final ResourceLocation EXPERIENCE_BAR_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("hud/experience_bar_progress");

    private AmountWidget amountWidget;
    private TypeWidget typeWidget;
    private Button removeExp;
    private Button addExp;

    public AutoUncraftingTableScreen(AutoUncraftingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.page = this.getMenu().getPage();
        this.selectedRecipe = this.getMenu().getIndex();
    }

    @Override
    protected void init() {
        this.imageHeight = 184;
        this.inventoryLabelY = 10000;

        super.init();

        this.leftPos = Math.max((width - imageWidth) / 2, (16 * 9) + SCROLLBAR_PADDING + SCROLLBAR_WIDTH);

        amountWidget = new AmountWidget(this.font, this.width / 2 - 31, this.topPos + 76, 30, 14, Component.empty(), this.menu.getAmountToAdd());
        this.addRenderableWidget(amountWidget);

        typeWidget = new TypeWidget(this.font, this.width / 2, this.topPos + 76, 30, 14, Component.empty(), this.menu.getTypeToAdd());
        this.addRenderableWidget(typeWidget);

        removeExp = Button.builder(Component.literal("-"), this::remove).pos(this.leftPos + 12, this.topPos + 91).size(9, 9).build();
        this.addRenderableWidget(removeExp);
        addExp = Button.builder(Component.literal("+"), this::add).pos(this.leftPos + 155, this.topPos + 91).size(9, 9).build();
        this.addRenderableWidget(addExp);
    }

    private void remove(Button button){
        UncraftEverything.CHANNEL.send(new ExpTransferPayload(this.menu.blockEntity.getBlockPos(), -this.menu.getAmountToAdd(), this.menu.getTypeToAdd()), PacketDistributor.SERVER.noArg());
    }

    private void add(Button button){
        UncraftEverything.CHANNEL.send(new ExpTransferPayload(this.menu.blockEntity.getBlockPos(), this.menu.getAmountToAdd(), this.menu.getTypeToAdd()), PacketDistributor.SERVER.noArg());
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float partialTick, int mouseX, int mouseY) {
        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        this.renderExpStored(pGuiGraphics, mouseX, mouseY);
        super.renderBg(pGuiGraphics, partialTick, mouseX, mouseY);
    }

    private void renderExpStored(GuiGraphics guiGraphics, int mouseX, int mouseY){
        int k = this.menu.blockEntity.getXpNeededForNextLevel();
        if (k > 0) {
            int l = this.menu.getExpProgress();
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, EXPERIENCE_BAR_BACKGROUND_SPRITE, this.leftPos + 25, this.topPos + 93, 125, 5);
            if (l > 0) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, EXPERIENCE_BAR_PROGRESS_SPRITE, 125, 5, 0, 0, this.leftPos + 25, this.topPos + 93, l, 5);
            }
        }

        if (this.menu.getExpLevels() > 0){
            Component component = Component.translatable("gui.experience.level", this.menu.getExpLevels());
            int i = (this.width - font.width(component)) / 2;
            int j = this.topPos + 92;
            guiGraphics.drawString(font, component, i + 1, j, -16777216, false);
            guiGraphics.drawString(font, component, i - 1, j, -16777216, false);
            guiGraphics.drawString(font, component, i, j + 1, -16777216, false);
            guiGraphics.drawString(font, component, i, j - 1, -16777216, false);
            guiGraphics.drawString(font, component, i, j, -8323296, false);
        }

        if (mouseX >= (this.leftPos + 25) && mouseX <= (this.leftPos + 150) && mouseY >= this.topPos + 93 && mouseY <= this.topPos + 98){
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("screen.uncrafteverything.tooltip.recipe_require").withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.translatable("screen.uncrafteverything.exp_" + this.menu.getExpType().toLowerCase() + "_required",this.menu.getExpAmount()).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("screen.uncrafteverything.tooltip.exp_stored").withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.literal(this.menu.getExpLevels() + " ").append(Component.translatable("tooltip.uncrafteverything.config.level")).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(this.menu.getExpPoints() + " ").append(Component.translatable("tooltip.uncrafteverything.config.point")).withStyle(ChatFormatting.GRAY));
            guiGraphics.setTooltipForNextFrame(this.font, tooltip, Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.clearWidgets();
        this.init();
        this.page = this.getMenu().getPage();
        this.selectedRecipe = this.getMenu().getIndex();
        this.renderRecipeSelection(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        super.renderContents(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderInputSlotOverlay(pGuiGraphics);
        super.renderCarriedItem(pGuiGraphics, pMouseX, pMouseY);
        super.renderSnapbackItem(pGuiGraphics);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderExpRequired(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    protected void chooseRecipe(RecipeSelectionButton button, int index) {
        super.chooseRecipe(button, index);
        UncraftEverything.CHANNEL.send(new SelectedIndexSyncPayload(this.menu.blockEntity.getBlockPos(), index), PacketDistributor.SERVER.noArg());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollDelta) {
        if (amountWidget.isHovered()){
            if (scrollDelta == 1.0d){
                if (this.menu.getAmountToAdd() < 100){
                    UncraftEverything.CHANNEL.send(new AmountToAddPayload(this.menu.blockEntity.getBlockPos(), this.menu.getAmountToAdd() + 1), PacketDistributor.SERVER.noArg());
                }
                else{
                    UncraftEverything.CHANNEL.send(new AmountToAddPayload(this.menu.blockEntity.getBlockPos(), 1), PacketDistributor.SERVER.noArg());
                }
            }
            else if (scrollDelta == -1.0d){
                if (this.menu.getAmountToAdd() > 1){
                    UncraftEverything.CHANNEL.send(new AmountToAddPayload(this.menu.blockEntity.getBlockPos(), this.menu.getAmountToAdd() - 1), PacketDistributor.SERVER.noArg());
                }
                else{
                    UncraftEverything.CHANNEL.send(new AmountToAddPayload(this.menu.blockEntity.getBlockPos(), 100), PacketDistributor.SERVER.noArg());
                }
            }
            return true;
        }
        if (typeWidget.isHovered()){
            if (scrollDelta == 1.0d || scrollDelta == -1.0d){
                UncraftEverything.CHANNEL.send(new TypeChangePayload(this.menu.blockEntity.getBlockPos(), this.menu.getTypeToAdd().invert()), PacketDistributor.SERVER.noArg());
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollDelta);
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);
        if (removeExp.isHovered()){
            List<Component> tooltips = new ArrayList<>();
            tooltips.add(Component.translatable("screen.uncrafteverything.tooltip.remove_exp"));
            tooltips.add(Component.translatable("screen.uncrafteverything.tooltip.level_detail").withStyle(ChatFormatting.GRAY));
            tooltips.add(Component.translatable("screen.uncrafteverything.tooltip.level_detail_2").withStyle(ChatFormatting.DARK_GRAY));
            tooltips.add(Component.translatable("screen.uncrafteverything.tooltip.level_detail_3").withStyle(ChatFormatting.DARK_GRAY));
            guiGraphics.setTooltipForNextFrame(this.font, tooltips, Optional.empty(), x, y);
        }
        if (addExp.isHovered()){
            List<Component> tooltips = new ArrayList<>();
            tooltips.add(Component.translatable("screen.uncrafteverything.tooltip.add_exp"));
            tooltips.add(Component.translatable("screen.uncrafteverything.tooltip.level_detail").withStyle(ChatFormatting.GRAY));
            tooltips.add(Component.translatable("screen.uncrafteverything.tooltip.level_detail_2").withStyle(ChatFormatting.DARK_GRAY));
            tooltips.add(Component.translatable("screen.uncrafteverything.tooltip.level_detail_3").withStyle(ChatFormatting.DARK_GRAY));
            guiGraphics.setTooltipForNextFrame(this.font, tooltips, Optional.empty(), x, y);
        }
    }
}