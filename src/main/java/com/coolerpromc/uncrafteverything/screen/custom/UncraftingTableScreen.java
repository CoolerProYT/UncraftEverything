package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableCraftButtonClickPayload;
import com.coolerpromc.uncrafteverything.screen.widget.RecipeSelectionButton;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("DataFlowIssue")
public class UncraftingTableScreen extends HandledScreen<UncraftingTableMenu> {
    private static final Identifier TEXTURE = Identifier.of(UncraftEverything.MODID, "textures/gui/uncrafting_table_gui.png");
    private static final Identifier RECIPE_PANEL_TEXTURE = Identifier.of(UncraftEverything.MODID, "textures/gui/recipe_selection_panel.png");
    private List<UncraftingTableRecipe> recipes = List.of();
    private int selectedRecipe = 0;

    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_PADDING = 2;
    private int page = 0;
    private final int MAX_PAGE_SIZE = 7;

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

        super.init();

        this.x = Math.max((width - backgroundWidth) / 2, (16 * 9) + SCROLLBAR_PADDING + SCROLLBAR_WIDTH);

        int buttonX = this.x + (backgroundWidth - 64) - 20;
        int buttonY = this.y + 72;

        this.addDrawableChild(ButtonWidget
                .builder(Text.translatable("screen.uncrafteverything.uncraft"), this::onPressed).position(buttonX, buttonY).size(64, 16)
                .build());

        if (this.handler.player.isCreative() || this.handler.player.hasPermissionLevel(4)){
            TextIconButtonWidget configButton = TextIconButtonWidget
                    .builder(Text.translatable("screen.uncrafteverything.blank"), this::openConfigScreen, true).dimension(12, 12).texture(Identifier.of(UncraftEverything.MODID, "config"), 8, 8)
                    .build();
            configButton.setX(this.x + backgroundWidth - 16);
            configButton.setY(this.y + 3);
            this.addDrawableChild(configButton);

            TextIconButtonWidget expButton = TextIconButtonWidget
                    .builder(Text.translatable("screen.uncrafteverything.blank"), this::openExpScreen, true).dimension(12, 12).texture(Identifier.of(UncraftEverything.MODID, "exp"), 8, 8)
                    .build();
            expButton.setX(this.x + backgroundWidth - 30);
            expButton.setY(this.y + 3);
            this.addDrawableChild(expButton);
        }
    }

    private void onPressed(ButtonWidget button) {
        UncraftingTableCraftButtonClickPayload payload = new UncraftingTableCraftButtonClickPayload(this.handler.blockEntity.getPos(), hasShiftDown());
        ClientPlayNetworking.send(payload);
    }

    private void openConfigScreen(ButtonWidget button){
        this.client.setScreen(new UEConfigScreen(Text.translatable("screen.uncrafteverything.uncraft_everything_config"), this));
    }

    private void openExpScreen(ButtonWidget button){
        this.client.setScreen(new PerItemExpConfigScreen(this));
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

        this.clearChildren();
        this.init();

        Text exp = Text.translatable("screen.uncrafteverything.exp_" + this.handler.getExpType().toLowerCase() + "_required",this.handler.getExpAmount());
        int expX = x + (backgroundWidth - 64) - 20 + 32;

        context.getMatrices().push();
        context.getMatrices().scale(0.75f, 0.75f, 0.75f);
        context.getMatrices().translate(expX * 1.3334f, this.y * 1.3334f + 121, 0);
        this.drawCenteredWordWrapWithoutShadow(context, this.textRenderer, exp, 0, 0, 0xFF00AA00);
        context.getMatrices().pop();

        int maxPageCount = (int) Math.ceil((double) recipes.size() / MAX_PAGE_SIZE);
        int pageToDisplay = recipes.isEmpty() ? 0 : page + 1;

        if (page > maxPageCount - 1) {
            page = 0;
        }

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 600);
        context.drawTexture(RenderLayer::getGuiTextured, RECIPE_PANEL_TEXTURE, x - 152, y, 0, 0, 152, 184, 152, 184);
        this.drawCenteredWordWrapWithoutShadow(context, textRenderer, Text.translatable("screen.uncrafteverything.uncraft_recipe_selection"), x - 75, y + 7, 0xFF404040);
        this.drawCenteredWordWrapWithoutShadow(context, textRenderer, Text.translatable("screen.uncrafteverything.page", pageToDisplay, maxPageCount), x - 75, y + backgroundHeight - 18, 0xFF404040);

        ButtonWidget prevButton = ButtonWidget.builder(Text.translatable("screen.uncrafteverything.prev_button"), button -> {
            if (this.page > 0) {
                this.page--;
            }
            else{
                this.page = Math.max(maxPageCount - 1, 0);
            }
        }).position(x - 152 + 5, y + backgroundHeight - 23).size(16, 16).build();
        this.addDrawableChild(prevButton).render(context, mouseX, mouseY, delta);

        ButtonWidget nextButton = ButtonWidget.builder(Text.translatable("screen.uncrafteverything.next_button"), button -> {
            if (this.page < maxPageCount - 1) {
                this.page++;
            }
            else{
                this.page = 0;
            }
        }).position(x - 21, y + backgroundHeight - 23).size(16, 16).build();
        this.addDrawableChild(nextButton).render(context, mouseX, mouseY, delta);

        int visibleCount = 0;
        for (int j = page * MAX_PAGE_SIZE; j < recipes.size() && visibleCount < MAX_PAGE_SIZE; j++) {
            UncraftingTableRecipe recipe = recipes.get(j);
            int displayIndex = visibleCount;

            int recipeWidth = 9 * 16 + 5;
            Rectangle2D bounds = new Rectangle2D.Double(x - recipeWidth, y + (displayIndex * 18) + 30, recipeWidth - 3, 18);

            int finalJ = j;
            RecipeSelectionButton button = new RecipeSelectionButton((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight(), Text.translatable("screen.uncrafteverything.blank"), ignored -> selectedRecipe = finalJ);
            if (selectedRecipe == j) {
                button.setFocused(true);
            }
            this.addDrawableChild(button).render(context, mouseX, mouseY, delta);

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
                context.drawItemWithoutEntity(itemStack, x - recipeWidth + (i * 16) + 1, y + (displayIndex * 18) + 31);
                context.drawStackOverlay(this.textRenderer, itemStack, x - recipeWidth + (i * 16) + 1, y + (displayIndex * 18) + 31);
                if (mouseX >= x - recipeWidth + (i * 16) + 1 && mouseX <= x - recipeWidth + (i * 16) + 17 && mouseY >= y + (displayIndex * 18) + 31 && mouseY <= y + (displayIndex * 18) + 31 + 16) {
                    context.drawItemTooltip(this.textRenderer, itemStack, mouseX, mouseY);
                }
                i++;
            }

            visibleCount++;
        }
        context.getMatrices().pop();

        if (selectedRecipe >= recipes.size()) {
            selectedRecipe = 0;
        }

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

        int status = this.handler.getStatus();

        if (status != -1){
            Text statusText = Text.translatable(switch (status){
                case 0 -> "screen.uncrafteverything.no_recipe_found";
                case 1 -> "screen.uncrafteverything.no_suitable_output_slot";
                case 2 -> "screen.uncrafteverything.not_enough_exp";
                case 3 -> "screen.uncrafteverything.not_enough_input";
                case 4 -> "screen.uncrafteverything.not_empty_shulker";
                case 5 -> "screen.uncrafteverything.restricted_by_config";
                case 6 -> "screen.uncrafteverything.damaged_item";
                case 7 -> "screen.uncrafteverything.enchanted_item";
                default -> "screen.uncrafteverything.blank";
            });

            int textY = y;

            context.getMatrices().push();
            context.getMatrices().translate(0,0,400);
            context.fill(x + 97, y + 16, x + 151, y + 70, 0xAA8B8B8B);
            context.getMatrices().pop();
            List<OrderedText> formattedText = textRenderer.wrapLines(StringVisitable.plain(statusText.getString()), 54);

            switch (formattedText.size()){
                case 1 -> textY += 38;
                case 2 -> textY += 34;
                case 3 -> textY += 30;
                case 4 -> textY += 23;
                default -> textY += 27;
            }

            for (OrderedText formattedcharsequence : formattedText) {
                int textWidth = textRenderer.getWidth(formattedcharsequence);
                int centeredX = x + 97 + (54 - textWidth) / 2;
                context.getMatrices().push();
                context.getMatrices().translate(centeredX,textY,400);
                context.drawText(textRenderer, formattedcharsequence, 0, 0, 0xFFAA0000, false);
                context.getMatrices().pop();
                textY += 9;
            }
        }

        if (this.handler.player.hasPermissionLevel(4) || this.handler.player.isCreative()){
            if (mouseX >= x + backgroundWidth - 16 && mouseX <= x + backgroundWidth - 4 && mouseY >= y + 3 && mouseY <= y + 15) {
                context.drawTooltip(this.textRenderer, Text.translatable("screen.uncrafteverything.uncraft_everything_config"), mouseX, mouseY);
            }

            if (mouseX >= x + backgroundWidth - 30 && mouseX <= x + backgroundWidth - 18 && mouseY >= y + 3 && mouseY <= y + 15) {
                context.drawTooltip(this.textRenderer, Text.translatable("screen.uncrafteverything.per_item_xp_config"), mouseX, mouseY);
            }
        }

        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollDelta) {
        if (scrollDelta == 1.0d && this.page > 0) {
            this.page--;
        } else if (scrollDelta == -1.0d && (this.page + 1) * MAX_PAGE_SIZE < recipes.size()) {
            this.page++;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollDelta);
    }

    public void drawCenteredWordWrapWithoutShadow(DrawContext context, TextRenderer textRenderer, Text text, int centerX, int y, int color) {
        List<OrderedText> lines = textRenderer.wrapLines(text, 140);

        int lineHeight = textRenderer.fontHeight + 2;

        for (int i = 0; i < lines.size(); i++) {
            OrderedText line = lines.get(i);
            int lineWidth = textRenderer.getWidth(line);
            int lineX = centerX - lineWidth / 2;
            int lineY = y + (i * lineHeight);

            context.drawText(textRenderer, line, lineX, lineY, color, false);
        }
    }

    public void getRecipeSelection(){
        UncraftingTableRecipe recipe = null;
        try{
            if (!recipes.isEmpty()){
                recipe = this.recipes.get(this.selectedRecipe);
            }
        }
        catch (Exception ignored){

        }
        finally {
            if (recipe != null){
                ClientPlayNetworking.send(new UncraftingRecipeSelectionPayload(this.handler.blockEntity.getPos(), recipe));
            }
        }
    }
}
