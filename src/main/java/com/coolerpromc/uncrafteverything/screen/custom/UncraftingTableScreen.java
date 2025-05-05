package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableCraftButtonClickPayload;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UncraftingTableScreen extends ContainerScreen<UncraftingTableMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(UncraftEverything.MODID, "textures/gui/uncrafting_table_gui.png");
    private List<UncraftingTableRecipe> recipes = new ArrayList<>();
    private int selectedRecipe = 0;
    private int hoveredRecipe = -1;
    private final List<Rectangle2D> recipeBounds = new ArrayList<>();

    private int scrollOffset = 0;
    private int maxVisibleRecipes;
    private boolean isScrolling = false;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_PADDING = 2;
    private Rectangle2D scrollBarBounds;

    public UncraftingTableScreen(UncraftingTableMenu menu, PlayerInventory playerInventory, ITextComponent title) {
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

        super.init();

        this.leftPos = Math.max(x, (16 * 9) + SCROLLBAR_PADDING + SCROLLBAR_WIDTH);

        int buttonX = leftPos + (imageWidth - 64) - 20;
        int buttonY = topPos + 72;

        this.addButton(new Button(buttonX, buttonY, 64, 20, new StringTextComponent("UnCraft"), this::onPressed));
    }

    private void onPressed(Button button) {
        UncraftingTableCraftButtonClickPayload payload = new UncraftingTableCraftButtonClickPayload(this.menu.blockEntity.getBlockPos());
        UncraftingTableCraftButtonClickPayload.INSTANCE.sendToServer(payload);
    }

    @Override
    protected void renderBg(MatrixStack pGuiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(TEXTURE);
        int x = this.leftPos;
        int y = this.topPos;
        blit(pGuiGraphics, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(MatrixStack pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);

        String exp = "Experience " + this.menu.getExpType() + ": " + this.menu.getExpAmount();

        pGuiGraphics.pushPose();
        pGuiGraphics.scale(0.75f, 0.75f, 0.75f);
        pGuiGraphics.translate(this.leftPos * 1.3334 + 115, this.topPos * 1.3334 + 124, 0);
        drawString(pGuiGraphics, this.font, exp, 0, 0, 0x00AA00);
        pGuiGraphics.popPose();

        recipeBounds.clear();

        int x = this.leftPos;
        int y = this.topPos;

        // Calculate maximum number of visible recipes
        maxVisibleRecipes = (imageHeight) / 16;  // Adjust based on your UI layout

        // Ensure scrolling offset is valid
        if (scrollOffset < 0) scrollOffset = 0;
        int maxOffset = Math.max(0, recipes.size() - maxVisibleRecipes);
        if (scrollOffset > maxOffset) scrollOffset = maxOffset;

        fill(pGuiGraphics, x - (16 * 9) - SCROLLBAR_PADDING, y + 5 - SCROLLBAR_PADDING, x, y + 5 + (maxVisibleRecipes * 16) + SCROLLBAR_PADDING, 0x15F8F9FA);

        // Setup scrollbar bounds
        int scrollbarHeight;
        if (recipes.size() > maxVisibleRecipes) {
            int scrollAreaHeight = maxVisibleRecipes * 16;
            scrollbarHeight = Math.max(20, maxVisibleRecipes * scrollAreaHeight / recipes.size());
            int scrollY = y + 5 + (int)((scrollAreaHeight - scrollbarHeight) *
                    ((float)scrollOffset / (recipes.size() - maxVisibleRecipes)));

            // Draw scrollbar background
            fill(
                    pGuiGraphics,
                    x - (16 * 9) - SCROLLBAR_PADDING,
                    y + 5 - SCROLLBAR_PADDING,
                    x - (16 * 9) - SCROLLBAR_WIDTH - SCROLLBAR_PADDING,
                    y + 5 + (maxVisibleRecipes * 16) + SCROLLBAR_PADDING,
                    0x50F8F9FA);

            // Draw scrollbar handle
            fill(
                    pGuiGraphics,
                    x - (16 * 9) - SCROLLBAR_PADDING,
                    scrollY,
                    x - (16 * 9) - SCROLLBAR_WIDTH - SCROLLBAR_PADDING,
                    scrollY + scrollbarHeight,
                    0xFFBBBBBB);

            scrollBarBounds = new Rectangle2D.Double(
                    0, y + 5,
                    SCROLLBAR_WIDTH, maxVisibleRecipes * 16);
        }

        // Render visible recipes
        int visibleCount = 0;
        for (int j = scrollOffset; j < recipes.size() && visibleCount < maxVisibleRecipes; j++) {
            UncraftingTableRecipe recipe = recipes.get(j);
            int displayIndex = visibleCount;  // Visual position index

            int recipeWidth = 9 * 16;
            Rectangle2D bounds = new Rectangle2D.Double(
                    x - recipeWidth,
                    y + (displayIndex * 16) + 5,
                    recipeWidth, 16);
            recipeBounds.add(bounds);

            if (selectedRecipe == j) {
                fill(
                        pGuiGraphics,
                        (int) bounds.getX(),
                        (int) bounds.getY(),
                        (int) (bounds.getX() + bounds.getWidth()),
                        (int) (bounds.getY() + bounds.getHeight()),
                        0x80FFFFFF);
            }

            if (hoveredRecipe == j) {
                fill(
                        pGuiGraphics,
                        (int) bounds.getX(),
                        (int) bounds.getY(),
                        (int) (bounds.getX() + bounds.getWidth()),
                        (int) (bounds.getY() + bounds.getHeight()),
                        0x70FFFFFF);
            }

            int i = 0;
            Map<Item, Integer> inputs = new HashMap<>();
            Map<Item, CompoundNBT> inputComponents = new HashMap<>();

            for (ItemStack itemStack : recipe.getOutputs()) {
                if (inputs.containsKey(itemStack.getItem())){
                    inputs.put(itemStack.getItem(), itemStack.getCount() + inputs.get(itemStack.getItem()));
                    inputComponents.put(itemStack.getItem(), itemStack.getTag());
                }
                else{
                    inputs.put(itemStack.getItem(), itemStack.getCount());
                    inputComponents.put(itemStack.getItem(), itemStack.getTag());
                }
            }

            for (Map.Entry<Item, Integer> entry : inputs.entrySet()) {
                if (entry.getKey() == Items.AIR) continue;
                ItemStack itemStack = new ItemStack(entry.getKey(), entry.getValue());
                if (inputComponents.containsKey(entry.getKey())){
                    itemStack.setTag(inputComponents.get(entry.getKey()));
                }
                itemRenderer.renderAndDecorateFakeItem(itemStack, x - recipeWidth + (i * 16), y + (displayIndex * 16) + 5);
                itemRenderer.renderGuiItemDecorations(this.font, itemStack, x - recipeWidth + (i * 16), y + (displayIndex * 16) + 5);
                i++;
            }

            visibleCount++;
        }

        // Ensure selection is valid
        if (selectedRecipe >= recipes.size()) {
            selectedRecipe = 0;
        }

        // Show selected recipe outputs
        if (!recipes.isEmpty()) {
            UncraftingRecipeSelectionPayload.INSTANCE.sendToServer(new UncraftingRecipeSelectionPayload(this.menu.blockEntity.getBlockPos(), this.recipes.get(selectedRecipe)));

            List<ItemStack> outputs = this.recipes.get(selectedRecipe).getOutputs();
            for (int i = 0; i < outputs.size(); i++) {
                ItemStack itemStack = outputs.get(i);
                itemRenderer.renderGuiItem(itemStack,
                        x + 98 + 18 * (i % 3),
                        y + 17 + (i / 3) * 18);
                RenderSystem.disableDepthTest();
                fill(
                        pGuiGraphics,
                        x + 98 + 18 * (i % 3),
                        y + 17 + (i / 3) * 18,
                        x + 98 + 18 * (i % 3) + 16,
                        y + 17 + (i / 3) * 18 + 16,
                        0x998B8B8B);
                RenderSystem.enableDepthTest();
            }
        }
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        int status = this.menu.getStatus();

        if (status != -1){
            String statusText;
            switch (status){
                case 0 : statusText = "No Recipe Found!"; break;
                case 1 : statusText = "No Suitable Output Slot!"; break;
                case 2 : statusText = "Not Enough Experience!"; break;
                case 3 : statusText = "Not Enough Input Item!"; break;
                case 4 : statusText = "Shulker Box is Not Empty!"; break;
                case 5 : statusText = "Item Restricted by Config!"; break;
                case 6 : statusText = "Item is Damaged!"; break;
                case 7 : statusText = "Enchanted Item Not Allowed!"; break;
                default : statusText = "";
            }

            int textY = y;

            pGuiGraphics.pushPose();
            pGuiGraphics.translate(0, 0, 400);
            fill(pGuiGraphics, x + 97, y + 16, x + 151, y + 70, 0xAA8B8B8B);
            pGuiGraphics.popPose();
            List<IReorderingProcessor> formattedText = font.split(ITextProperties.of(statusText), 54);

            switch (formattedText.size()){
                case 1 : textY += 27; break;
                case 2 : textY += 34; break;
                case 3 : textY += 30; break;
                case 4 : textY += 23; break;
                default : textY += 27;
            }

            for (IReorderingProcessor formattedcharsequence : formattedText) {
                int textWidth = font.width(formattedcharsequence);
                int centeredX = x + 97 + (54 - textWidth) / 2;
                pGuiGraphics.pushPose();
                pGuiGraphics.translate(centeredX, textY, 400);
                font.draw(pGuiGraphics, formattedcharsequence, 0, 0, 0xAA0000);
                pGuiGraphics.popPose();
                textY += 9;
            }
        }

        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check if clicking on scrollbar
        if (scrollBarBounds != null && scrollBarBounds.contains(mouseX, mouseY)) {
            isScrolling = true;
            mouseDragged(mouseX, mouseY, button, 0, 0); // Initial position
            return true;
        }

        // Check if clicking on recipe
        for (int i = 0; i < recipeBounds.size(); i++) {
            if (recipeBounds.get(i).contains(mouseX, mouseY)) {
                selectedRecipe = i + scrollOffset;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        hoveredRecipe = -1;

        for (int i = 0; i < recipeBounds.size(); i++) {
            if (recipeBounds.get(i).contains(mouseX, mouseY)) {
                hoveredRecipe = i + scrollOffset;
                return;
            }
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isScrolling = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isScrolling && recipes.size() > maxVisibleRecipes) {
            int x = this.leftPos;
            int y = this.topPos;

            // Calculate scroll position based on mouse Y position relative to scrollable area
            int scrollAreaHeight = maxVisibleRecipes * 16;
            float relativeY = (float)(mouseY - (y + 5)) / scrollAreaHeight;
            relativeY = MathHelper.clamp(relativeY, 0.0F, 1.0F);

            // Set scroll offset
            scrollOffset = Math.round(relativeY * (recipes.size() - maxVisibleRecipes));
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if (recipes.size() > maxVisibleRecipes) {
            // Adjust scroll offset based on mouse wheel
            scrollOffset = MathHelper.clamp(scrollOffset - (int) scrollDelta, 0, recipes.size() - maxVisibleRecipes);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollDelta);
    }
}
