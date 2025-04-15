package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableCraftButtonClickPayload;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class UncraftingTableScreen extends AbstractContainerScreen<UncraftingTableMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "textures/gui/uncrafting_table_gui.png");
    private List<UncraftingTableRecipe> recipes = List.of();
    private int selectedRecipe = 0;
    private int hoveredRecipe = -1;
    private final List<Rectangle2D> recipeBounds = new ArrayList<>();

    public UncraftingTableScreen(UncraftingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    public void updateFromBlockEntity(List<UncraftingTableRecipe> recipes) {
        this.recipes = recipes;
    }

    @Override
    protected void init() {
        this.imageHeight = 184;
        this.inventoryLabelY = this.imageHeight - 94;

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int buttonX = x + (imageWidth - 64) - 20;
        int buttonY = y + 72;

        this.addRenderableWidget(Button
                .builder(Component.literal("UnCraft"), this::onPressed).pos(buttonX, buttonY).size(64, 16)
                .build());

        super.init();
    }

    private void onPressed(Button button) {
        UncraftingTableCraftButtonClickPayload payload = new UncraftingTableCraftButtonClickPayload(this.menu.blockEntity.getBlockPos(), "Craft");
        PacketDistributor.sendToServer(payload);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        pGuiGraphics.blit(RenderType::guiTextured, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pGuiGraphics, pMouseX, pMouseY);

        recipeBounds.clear();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int j = 0;

        for (UncraftingTableRecipe recipe : this.recipes){
            int i = 0;

            int recipeWidth = 9 * 16;
            Rectangle2D bounds = new Rectangle2D.Double(x + - recipeWidth, y + (j * 16) + 10, recipeWidth, 16);
            recipeBounds.add(bounds);

            if (selectedRecipe == j){
                pGuiGraphics.fill((int) bounds.getX(), (int) bounds.getY(),
                        (int) (bounds.getX() + bounds.getWidth()),
                        (int) (bounds.getY() + bounds.getHeight()), 0x80FFFFFF);
            }

            if (hoveredRecipe == j){
                pGuiGraphics.fill((int) bounds.getX(), (int) bounds.getY(),
                        (int) (bounds.getX() + bounds.getWidth()),
                        (int) (bounds.getY() + bounds.getHeight()), 0x70FFFFFF);
            }

            for (ItemStack itemStack : recipe.getOutputs()){
                pGuiGraphics.renderFakeItem(itemStack, x - recipeWidth + (i * 16), y + (j * 16) + 10);
                i++;
            }
            j++;
        }

        if (selectedRecipe > recipes.size()){
            selectedRecipe = 0;
        }

        if (!this.recipes.isEmpty()) {
            PacketDistributor.sendToServer(new UncraftingRecipeSelectionPayload(this.menu.blockEntity.getBlockPos(), this.recipes.get(selectedRecipe)));
            List<ItemStack> outputs = this.recipes.get(selectedRecipe).getOutputs();
            for (int i = 0; i < outputs.size(); i++) {
                ItemStack itemStack = outputs.get(i);
                pGuiGraphics.renderFakeItem(itemStack, x + 98 + 18 * (i % 3), y + 17 + (i / 3) * 18);
                pGuiGraphics.fill(x + 98 + 18 * (i % 3), y + 17 + (i / 3) * 18, x + 98 + 18 * (i % 3) + 16, y + 17 + (i / 3) * 18 + 16, 200,0xAA8B8B8B);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = 0; i < recipeBounds.size(); i++) {
            if (recipeBounds.get(i).contains(mouseX, mouseY)) {
                selectedRecipe = i;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        for (int i = 0; i < recipeBounds.size(); i++) {
            if (recipeBounds.get(i).contains(mouseX, mouseY)) {
                hoveredRecipe = i;
                return;
            }
        }
        hoveredRecipe = -1;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
