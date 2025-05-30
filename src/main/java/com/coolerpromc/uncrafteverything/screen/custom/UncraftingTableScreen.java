package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableCraftButtonClickPayload;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UncraftingTableScreen extends AbstractContainerScreen<UncraftingTableMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "textures/gui/uncrafting_table_gui.png");
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

        super.init();

        this.leftPos = Math.max(x, (16 * 9) + SCROLLBAR_PADDING + SCROLLBAR_WIDTH);

        int buttonX = leftPos + (imageWidth - 64) - 20;
        int buttonY = topPos + 72;

        this.addRenderableWidget(Button
                .builder(Component.literal("UnCraft"), this::onPressed).pos(buttonX, buttonY).size(64, 16)
                .build());

        if (this.menu.player.isCreative() || this.menu.player.hasPermissions(4)){
            SpriteIconButton configButton = SpriteIconButton
                    .builder(Component.literal(""), this::openConfigScreen, true).size(12, 12).sprite(ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "config"), 8, 8)
                    .build();
            configButton.setX(leftPos + imageWidth - 16);
            configButton.setY(topPos + 3);
            this.addRenderableWidget(configButton);

            SpriteIconButton expButton = SpriteIconButton
                    .builder(Component.literal(""), this::openExpScreen, true).size(12, 12).sprite(ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "exp"), 8, 8)
                    .build();
            expButton.setX(leftPos + imageWidth - 30);
            expButton.setY(topPos + 3);
            this.addRenderableWidget(expButton);
        }
    }

    private void onPressed(Button button) {
        UncraftingTableCraftButtonClickPayload payload = new UncraftingTableCraftButtonClickPayload(this.menu.blockEntity.getBlockPos(), "Craft");
        PacketDistributor.sendToServer(payload);
    }

    private void openConfigScreen(Button button){
        this.getMinecraft().setScreen(new UEConfigScreen(Component.literal("Uncraft Everything Config"), this));
    }

    private void openExpScreen(Button button){
        this.getMinecraft().setScreen(new PerItemExpConfigScreen(this));
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = this.leftPos;
        int y = this.topPos;

        pGuiGraphics.blit(RenderType::guiTextured, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        String exp = "Experience " + this.menu.getExpType() + ": " + this.menu.getExpAmount();

        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(0.75f, 0.75f, 0.75f);
        pGuiGraphics.pose().translate(this.leftPos * 1.3334 + 115, this.topPos * 1.3334 + 121, 0);
        pGuiGraphics.drawString(this.font, exp, 0, 0, 0x00AA00, false);
        pGuiGraphics.pose().popPose();

        recipeBounds.clear();

        int x = this.leftPos;
        int y = this.topPos;

        // Calculate maximum number of visible recipes
        maxVisibleRecipes = (imageHeight) / 16;  // Adjust based on your UI layout

        // Ensure scrolling offset is valid
        if (scrollOffset < 0) scrollOffset = 0;
        int maxOffset = Math.max(0, recipes.size() - maxVisibleRecipes);
        if (scrollOffset > maxOffset) scrollOffset = maxOffset;

        if (!recipes.isEmpty()){
            pGuiGraphics.fill(x - (16 * 9) - SCROLLBAR_PADDING, y + 5 - SCROLLBAR_PADDING, x, y + 5 + (maxVisibleRecipes * 16) + SCROLLBAR_PADDING, 0x15F8F9FA);
        }

        // Setup scrollbar bounds
        int scrollbarHeight;
        if (recipes.size() > maxVisibleRecipes) {
            int scrollAreaHeight = maxVisibleRecipes * 16;
            scrollbarHeight = Math.max(20, maxVisibleRecipes * scrollAreaHeight / recipes.size());
            int scrollY = y + 5 + (int)((scrollAreaHeight - scrollbarHeight) *
                    ((float)scrollOffset / (recipes.size() - maxVisibleRecipes)));

            // Draw scrollbar background
            pGuiGraphics.fill(
                    x - (16 * 9) - SCROLLBAR_PADDING,
                    y + 5 - SCROLLBAR_PADDING,
                    x - (16 * 9) - SCROLLBAR_WIDTH - SCROLLBAR_PADDING,
                    y + 5 + (maxVisibleRecipes * 16) + SCROLLBAR_PADDING,
                    0x50F8F9FA);

            // Draw scrollbar handle
            pGuiGraphics.fill(
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
                pGuiGraphics.fill(
                        (int) bounds.getX(),
                        (int) bounds.getY(),
                        (int) (bounds.getX() + bounds.getWidth()),
                        (int) (bounds.getY() + bounds.getHeight()),
                        0x80FFFFFF);
            }

            if (hoveredRecipe == j) {
                pGuiGraphics.fill(
                        (int) bounds.getX(),
                        (int) bounds.getY(),
                        (int) (bounds.getX() + bounds.getWidth()),
                        (int) (bounds.getY() + bounds.getHeight()),
                        0x70FFFFFF);
            }

            int i = 0;
            Map<Item, Integer> inputs = new HashMap<>();
            Map<Item, DataComponentMap> inputComponents = new HashMap<>();

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
                    itemStack.applyComponents(inputComponents.get(entry.getKey()));
                }
                pGuiGraphics.renderFakeItem(itemStack, x - recipeWidth + (i * 16), y + (displayIndex * 16) + 5);
                pGuiGraphics.renderItemDecorations(this.font, itemStack, x - recipeWidth + (i * 16), y + (displayIndex * 16) + 5);
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
            PacketDistributor.sendToServer(new UncraftingRecipeSelectionPayload(
                    this.menu.blockEntity.getBlockPos(),
                    this.recipes.get(selectedRecipe)));

            List<ItemStack> outputs = this.recipes.get(selectedRecipe).getOutputs();
            for (int i = 0; i < outputs.size(); i++) {
                ItemStack itemStack = outputs.get(i);
                pGuiGraphics.renderFakeItem(
                        itemStack,
                        x + 98 + 18 * (i % 3),
                        y + 17 + (i / 3) * 18);
                pGuiGraphics.fill(
                        x + 98 + 18 * (i % 3),
                        y + 17 + (i / 3) * 18,
                        x + 98 + 18 * (i % 3) + 16,
                        y + 17 + (i / 3) * 18 + 16,
                        200, 0xAA8B8B8B);
            }
        }

        int status = this.menu.getStatus();

        if (status != -1){
            String statusText = switch (status){
                case 0 -> "No Recipe Found!";
                case 1 -> "No Suitable Output Slot!";
                case 2 -> "Not Enough Experience!";
                case 3 -> "Not Enough Input Item!";
                case 4 -> "Shulker Box is Not Empty!";
                case 5 -> "Item Restricted by Config!";
                case 6 -> "Item is Damaged!";
                case 7 -> "Enchanted Item Not Allowed!";
                default -> "";
            };

            int textY = y;

            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().translate(0, 0, 400);
            pGuiGraphics.fill(x + 97, y + 16, x + 151, y + 70, 0xAA8B8B8B);
            pGuiGraphics.pose().popPose();
            List<FormattedCharSequence> formattedText = font.split(FormattedText.of(statusText), 54);

            switch (formattedText.size()){
                case 1 -> textY += 27;
                case 2 -> textY += 34;
                case 3 -> textY += 30;
                case 4 -> textY += 23;
                default -> textY += 27;
            }

            for (FormattedCharSequence formattedcharsequence : formattedText) {
                int textWidth = font.width(formattedcharsequence);
                int centeredX = x + 97 + (54 - textWidth) / 2;
                pGuiGraphics.pose().pushPose();
                pGuiGraphics.pose().translate(centeredX, textY, 400);
                pGuiGraphics.drawString(font, formattedcharsequence, 0, 0, 0xAA0000, false);
                pGuiGraphics.pose().popPose();
                textY += 9;
            }
        }

        if (this.menu.player.hasPermissions(4) || this.menu.player.isCreative()){
            if (pMouseX >= leftPos + imageWidth - 16 && pMouseX <= leftPos + imageWidth - 4 && pMouseY >= topPos + 3 && pMouseY <= topPos + 15) {
                pGuiGraphics.renderTooltip(this.font, Component.literal("Common Config"), pMouseX, pMouseY);
            }

            if (pMouseX >= leftPos + imageWidth - 30 && pMouseX <= leftPos + imageWidth - 18 && pMouseY >= topPos + 3 && pMouseY <= topPos + 15) {
                pGuiGraphics.renderTooltip(this.font, Component.literal("Per Item Experience Config"), pMouseX, pMouseY);
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
            relativeY = Mth.clamp(relativeY, 0.0F, 1.0F);

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
            scrollOffset = Mth.clamp(scrollOffset - (int) scrollDelta, 0, recipes.size() - maxVisibleRecipes);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollDelta);
    }
}
