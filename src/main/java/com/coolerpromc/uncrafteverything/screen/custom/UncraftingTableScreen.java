package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableCraftButtonClickPayload;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UncraftingTableScreen extends HandledScreen<UncraftingTableMenu> {
    private static final Identifier TEXTURE = Identifier.of(UncraftEverything.MODID, "textures/gui/uncrafting_table_gui.png");
    private List<UncraftingTableRecipe> recipes = List.of();
    private int selectedRecipe = 0;
    private int hoveredRecipe = -1;
    private final List<Rectangle2D> recipeBounds = new ArrayList<>();

    private int scrollOffset = 0;
    private int maxVisibleRecipes;
    private boolean isScrolling = false;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_PADDING = 2;
    private Rectangle2D scrollBarBounds;

    public UncraftingTableScreen(UncraftingTableMenu handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    public void updateFromBlockEntity(List<UncraftingTableRecipe> recipes) {
        this.recipes = recipes;
    }

    @Override
    protected void init() {
        this.backgroundHeight = 184;
        this.playerInventoryTitleY = this.backgroundHeight - 94;

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        super.init();

        this.x = Math.max(x, (16 * 9) + SCROLLBAR_PADDING + SCROLLBAR_WIDTH);

        int buttonX = x + (backgroundWidth - 64) - 20;
        int buttonY = y + 72;

        this.addDrawableChild(ButtonWidget
                .builder(Text.literal("UnCraft"), this::onPressed).position(buttonX, buttonY).size(64, 16)
                .build());
    }

    private void onPressed(ButtonWidget button) {
        UncraftingTableCraftButtonClickPayload payload = new UncraftingTableCraftButtonClickPayload(this.handler.blockEntity.getPos(), "Craft");
        ClientPlayNetworking.send(payload);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight, 256, 256);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        recipeBounds.clear();

        // Calculate maximum number of visible recipes
        maxVisibleRecipes = (backgroundHeight) / 16;  // Adjust based on your UI layout

        // Ensure scrolling offset is valid
        if (scrollOffset < 0) scrollOffset = 0;
        int maxOffset = Math.max(0, recipes.size() - maxVisibleRecipes);
        if (scrollOffset > maxOffset) scrollOffset = maxOffset;

        context.fill(x - (16 * 9) - SCROLLBAR_PADDING, y + 5 - SCROLLBAR_PADDING, x, y + 5 + (maxVisibleRecipes * 16) + SCROLLBAR_PADDING, 0x15F8F9FA);

        // Setup scrollbar bounds
        int scrollbarHeight;
        if (recipes.size() > maxVisibleRecipes) {
            int scrollAreaHeight = maxVisibleRecipes * 16;
            scrollbarHeight = Math.max(20, maxVisibleRecipes * scrollAreaHeight / recipes.size());
            int scrollY = y + 5 + (int)((scrollAreaHeight - scrollbarHeight) *
                    ((float)scrollOffset / (recipes.size() - maxVisibleRecipes)));

            // Draw scrollbar background
            context.fill(
                    x - (16 * 9) - SCROLLBAR_PADDING,
                    y + 5 - SCROLLBAR_PADDING,
                    x - (16 * 9) - SCROLLBAR_WIDTH - SCROLLBAR_PADDING,
                    y + 5 + (maxVisibleRecipes * 16) + SCROLLBAR_PADDING,
                    0x50F8F9FA);

            // Draw scrollbar handle
            context.fill(
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
                context.fill(
                        (int) bounds.getX(),
                        (int) bounds.getY(),
                        (int) (bounds.getX() + bounds.getWidth()),
                        (int) (bounds.getY() + bounds.getHeight()),
                        0x80FFFFFF);
            }

            if (hoveredRecipe == j) {
                context.fill(
                        (int) bounds.getX(),
                        (int) bounds.getY(),
                        (int) (bounds.getX() + bounds.getWidth()),
                        (int) (bounds.getY() + bounds.getHeight()),
                        0x70FFFFFF);
            }

            int i = 0;
            Map<Item, Integer> inputs = new HashMap<>();
            Map<Item, ComponentMap> inputComponents = new HashMap<>();

            for (ItemStack itemStack : recipe.getOutputs()) {
                if (inputs.containsKey(itemStack.getItem())){
                    inputs.put(itemStack.getItem(), itemStack.getCount() + inputs.get(itemStack.getItem()));
                    inputComponents.put(itemStack.getItem(), itemStack.getComponents());
                }
                else{
                    inputs.put(itemStack.getItem(), itemStack.getCount());
                    inputComponents.put(itemStack.getItem(), itemStack.getComponents());
                }
            }

            for (Map.Entry<Item, Integer> entry : inputs.entrySet()) {
                if (entry.getKey() == Items.AIR) continue;
                ItemStack itemStack = new ItemStack(entry.getKey(), entry.getValue());
                if (inputComponents.containsKey(entry.getKey())){
                    itemStack.applyComponentsFrom(inputComponents.get(entry.getKey()));
                }
                context.drawItemWithoutEntity(itemStack, x - recipeWidth + (i * 16), y + (displayIndex * 16) + 5);
                context.drawStackOverlay(this.textRenderer, itemStack, x - recipeWidth + (i * 16), y + (displayIndex * 16) + 5);
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
            ClientPlayNetworking.send(new UncraftingRecipeSelectionPayload(this.handler.blockEntity.getPos(), this.recipes.get(selectedRecipe)));

            List<ItemStack> outputs = this.recipes.get(selectedRecipe).getOutputs();
            for (int i = 0; i < outputs.size(); i++) {
                ItemStack itemStack = outputs.get(i);
                context.drawItemWithoutEntity(
                        itemStack,
                        x + 98 + 18 * (i % 3),
                        y + 17 + (i / 3) * 18);
                context.fill(
                        x + 98 + 18 * (i % 3),
                        y + 17 + (i / 3) * 18,
                        x + 98 + 18 * (i % 3) + 16,
                        y + 17 + (i / 3) * 18 + 16,
                        200, 0xAA8B8B8B);
            }
        }

        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollDelta) {
        if (recipes.size() > maxVisibleRecipes) {
            // Adjust scroll offset based on mouse wheel
            scrollOffset = MathHelper.clamp(scrollOffset - (int) scrollDelta, 0, recipes.size() - maxVisibleRecipes);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollDelta);
    }
}
