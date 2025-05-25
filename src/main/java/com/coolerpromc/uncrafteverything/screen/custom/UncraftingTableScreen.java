package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableCraftButtonClickPayload;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
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

        int buttonX = this.x + (backgroundWidth - 64) - 20;
        int buttonY = this.y + 72;

        this.addDrawableChild(ButtonWidget
                .builder(Text.literal("UnCraft"), this::onPressed).position(buttonX, buttonY).size(64, 16)
                .build());

        if (this.handler.player.isCreative() || this.handler.player.hasPermissionLevel(4)){
            TextIconButtonWidget configButton = TextIconButtonWidget
                    .builder(Text.literal(""), this::openConfigScreen, true).dimension(12, 12).texture(Identifier.of(UncraftEverything.MODID, "config"), 8, 8)
                    .build();
            configButton.setX(this.x + backgroundWidth - 16);
            configButton.setY(this.y + 3);
            this.addDrawableChild(configButton);

            TextIconButtonWidget expButton = TextIconButtonWidget
                    .builder(Text.literal(""), this::openExpScreen, true).dimension(12, 12).texture(Identifier.of(UncraftEverything.MODID, "exp"), 8, 8)
                    .build();
            expButton.setX(this.x + backgroundWidth - 30);
            expButton.setY(this.y + 3);
            this.addDrawableChild(expButton);
        }
    }

    private void onPressed(ButtonWidget button) {
        UncraftingTableCraftButtonClickPayload payload = new UncraftingTableCraftButtonClickPayload(this.handler.blockEntity.getPos(), "Craft");
        ClientPlayNetworking.send(payload);
    }

    private void openConfigScreen(ButtonWidget button){
        this.client.setScreen(new UEConfigScreen(Text.literal("Uncraft Everything Config"), this));
    }

    private void openExpScreen(ButtonWidget button){
        this.client.setScreen(new PerItemExpConfigScreen(this));
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight, 256, 256);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        String exp = "Experience " + this.handler.getExpType() + ": " + this.handler.getExpAmount();

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(0.75f, 0.75f);
        context.getMatrices().translate(this.x * 1.3334f + 115, this.y * 1.3334f + 121);
        context.drawText(this.textRenderer, exp, 0, 0, 0xFF00AA00, false);
        context.getMatrices().popMatrix();

        recipeBounds.clear();

        // Calculate maximum number of visible recipes
        maxVisibleRecipes = (backgroundHeight) / 16;  // Adjust based on your UI layout

        // Ensure scrolling offset is valid
        if (scrollOffset < 0) scrollOffset = 0;
        int maxOffset = Math.max(0, recipes.size() - maxVisibleRecipes);
        if (scrollOffset > maxOffset) scrollOffset = maxOffset;

        if (!recipes.isEmpty()){
            context.fill(x - (16 * 9) - SCROLLBAR_PADDING, y + 5 - SCROLLBAR_PADDING, x, y + 5 + (maxVisibleRecipes * 16) + SCROLLBAR_PADDING, 0x15F8F9FA);
        }

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
                        0xAA8B8B8B);
            }
        }

        super.render(context, mouseX, mouseY, delta);

        int status = this.handler.getStatus();

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

            context.fill(x + 97, y + 16, x + 151, y + 70, 0xAA8B8B8B);
            List<OrderedText> formattedText = textRenderer.wrapLines(StringVisitable.plain(statusText), 54);

            switch (formattedText.size()){
                case 1 -> textY += 27;
                case 2 -> textY += 34;
                case 3 -> textY += 30;
                case 4 -> textY += 23;
                default -> textY += 27;
            }

            for (OrderedText formattedcharsequence : formattedText) {
                int textWidth = textRenderer.getWidth(formattedcharsequence);
                int centeredX = x + 97 + (54 - textWidth) / 2;
                context.drawText(textRenderer, formattedcharsequence, centeredX, textY, 0xFFAA0000, false);
                textY += 9;
            }
        }

        if (this.handler.player.hasPermissionLevel(4) || this.handler.player.isCreative()){
            if (mouseX >= x + backgroundWidth - 16 && mouseX <= x + backgroundWidth - 4 && mouseY >= y + 3 && mouseY <= y + 15) {
                context.drawTooltip(this.textRenderer, Text.literal("Common Config"), mouseX, mouseY);
            }

            if (mouseX >= x + backgroundWidth - 30 && mouseX <= x + backgroundWidth - 18 && mouseY >= y + 3 && mouseY <= y + 15) {
                context.drawTooltip(this.textRenderer, Text.literal("Per Item Experience Config"), mouseX, mouseY);
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
