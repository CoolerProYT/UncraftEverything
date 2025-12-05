package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.blockentity.custom.AbstractUncraftingTableBE;
import com.coolerpromc.uncrafteverything.compat.ftbquests.QuestHelper;
import com.coolerpromc.uncrafteverything.networking.UncraftingPageChangePayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionPayload;
import com.coolerpromc.uncrafteverything.screen.widget.RecipeSelectionButton;
import com.coolerpromc.uncrafteverything.util.Status;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractUncraftingScreen<B extends AbstractUncraftingTableBE, T extends AbstractUncraftingMenu<B>> extends HandledScreen<T> {
    protected static final Identifier RECIPE_PANEL_TEXTURE = Identifier.of(UncraftEverything.MODID, "textures/gui/recipe_selection_panel.png");

    protected List<UncraftingTableRecipe> recipes = List.of();
    protected int selectedRecipe = 0;
    protected boolean hasShift = false;

    protected static final int SCROLLBAR_WIDTH = 6;
    protected static final int SCROLLBAR_PADDING = 2;
    protected int page = 0;
    protected final int MAX_PAGE_SIZE = 7;
    protected int recipeSize = 0;

    private boolean isInfoHovered = false;

    public AbstractUncraftingScreen(T menu, PlayerInventory playerInventory, Text title) {
        super(menu, playerInventory, title);
    }

    public void updateFromBlockEntity(List<UncraftingTableRecipe> recipes, int size, boolean shouldSendPacket) {
        this.recipes = recipes;
        this.recipeSize = size;

        if (size < 7 && this.page != 0){
            this.page = 0;
            ClientPlayNetworking.send(new UncraftingPageChangePayload(page, this.handler.blockEntity.getPos()));
        }

        if (!recipes.isEmpty()){
            if (selectedRecipe >= this.recipes.size()){
                selectedRecipe = 0;
            }
            if (shouldSendPacket){
                ClientPlayNetworking.send(new UncraftingRecipeSelectionPayload(this.handler.blockEntity.getPos(), this.recipes.get(selectedRecipe)));
            }
        }
    }

    protected abstract void renderExpRequired(DrawContext guiGraphics, int mouseX, int mouseY);

    @Override
    protected void drawBackground(DrawContext guiGraphics, float partialTick, int mouseX, int mouseY) {
        int buttonSize = 8;
        int x = this.x + this.backgroundWidth - 5 - buttonSize;
        int y = this.y + 5;
        isInfoHovered = mouseX >= x && mouseX <= x + buttonSize && mouseY >= y && mouseY <= y + buttonSize;
        guiGraphics.fill(x, y, x + buttonSize, y + buttonSize, isInfoHovered ? 0xFFFFFFFF: 0xFF000000);
        guiGraphics.fill(x + 1, y + 1, x + buttonSize - 1, y + buttonSize - 1, 0xFF6F6F6F);
        guiGraphics.fill(x + 1, y + 1, x + buttonSize - 2, y + buttonSize - 2, 0xFFAAAAAA);
        guiGraphics.fill(x + 2, y + 2, x + buttonSize - 1, y + buttonSize - 1, 0xFF565656);
        guiGraphics.fill(x + 2, y + 2, x + buttonSize - 2, y + buttonSize - 2, 0xFF6D6D6D);

        guiGraphics.getMatrices().pushMatrix();
        guiGraphics.getMatrices().scale(0.5f);
        guiGraphics.getMatrices().translate((x + buttonSize / 2f) * 2, ((y + buttonSize / 2f) - this.textRenderer.fontHeight / (2f * 2f)) * 2f);
        guiGraphics.drawCenteredTextWithShadow(this.textRenderer, Text.literal("?"), 0, 0, 0xFFFFFFFF);
        guiGraphics.getMatrices().popMatrix();
    }

    @Override
    protected void drawMouseoverTooltip(@NotNull DrawContext guiGraphics, int x, int y) {
        Status status = Status.byIndex(this.handler.getStatus());
        if (isInfoHovered){
            List<OrderedText> tooltips = new ArrayList<>();
            int maxWidth = Math.max(this.client.getWindow().getScaledWidth() - x, x - 30);

            Consumer<Text> addWrapped = (text) -> tooltips.addAll(this.textRenderer.wrapLines(text, maxWidth));
            Runnable addEmpty = () -> tooltips.add(OrderedText.EMPTY);

            addWrapped.accept(Text.literal("Configs").formatted(Formatting.BLUE));
            addWrapped.accept(Text.literal("/ueconfig client").formatted(Formatting.AQUA).append(Text.literal(" to change status color and other client config (Client side only)").formatted(Formatting.GRAY)));
            addEmpty.run();

            addWrapped.accept(Text.literal("/ueconfig common").formatted(Formatting.AQUA).append(Text.literal(" to access common config to modify uncrafting recipe searching behaviour " + "(Server side only, OP level 4 required)").formatted(Formatting.GRAY)));
            addEmpty.run();

            addWrapped.accept(Text.literal("/ueconfig exp").formatted(Formatting.AQUA).append(Text.literal(" to access per item exp config to modify exp needed for specific items " + "(Server side only, OP level 4 required)").formatted(Formatting.GRAY)));

            if (QuestHelper.FTBQUESTS_LOADED) {
                addEmpty.run();
                addWrapped.accept(Text.literal("/ueconfig progression").formatted(Formatting.AQUA).append(Text.literal(" to access ftb progression config to modify progression uncrafting " + "(Server side only, OP level 4 required)").formatted(Formatting.GRAY)));
            }

            guiGraphics.drawTooltip(this.textRenderer, tooltips, HoveredTooltipPositioner.INSTANCE, x, y, false);

        }
        if (this.focusedSlot != null && status != Status.BLANK && this.focusedSlot.getIndex() == 0){
            ItemStack itemStack = this.focusedSlot.getStack();
            List<Text> tooltip = new ArrayList<>(this.getTooltipFromItem(itemStack));
            tooltip.add(Text.translatable(status.getTranslationKey()).withColor(status.getOverlay()));
            guiGraphics.drawTooltip(this.textRenderer, tooltip, itemStack.getTooltipData(), x, y, itemStack.get(DataComponentTypes.TOOLTIP_STYLE));
        }
        else{
            super.drawMouseoverTooltip(guiGraphics, x, y);
        }
    }

    protected void renderInputSlotOverlay(DrawContext guiGraphics){
        Status status = Status.byIndex(this.handler.getStatus());

        if (status != Status.BLANK){
            guiGraphics.fill(this.x + 25, this.y + 34, this.x + 43, this.y + 52, ColorHelper.withAlpha(70, status.getOverlay()));
        }
    }

    protected void renderRecipeSelection(DrawContext pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick){
        int maxPageCount = (int) Math.ceil((double) recipeSize / MAX_PAGE_SIZE);
        int pageToDisplay = recipes.isEmpty() ? 0 : page + 1;

        if (page > maxPageCount - 1) {
            page = 0;
        }

        pGuiGraphics.drawTexture(RenderPipelines.GUI_TEXTURED, RECIPE_PANEL_TEXTURE, this.x - 152, this.y, 0, 0, 152, 184, 152, 184);
        this.drawCenteredWordWrapWithoutShadow(pGuiGraphics, textRenderer, Text.translatable("screen.uncrafteverything.uncraft_recipe_selection"), this.x - 75, this.y + 7, 0xFF404040);
        this.drawCenteredWordWrapWithoutShadow(pGuiGraphics, textRenderer, Text.translatable("screen.uncrafteverything.page",pageToDisplay, maxPageCount), this.x - 75, this.y + backgroundHeight - 18, 0xFF404040);


        this.renderNavigationButton(pGuiGraphics, pMouseX, pMouseY, pPartialTick, maxPageCount);
        this.renderRecipeButton(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        if (selectedRecipe >= recipes.size()) {
            selectedRecipe = 0;
        }
    }

    protected void renderNavigationButton(DrawContext pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick, int maxPageCount){
        ButtonWidget prevButton = ButtonWidget.builder(Text.translatable("screen.uncrafteverything.prev_button"), button -> {
            if (this.page > 0) {
                this.page--;
            }
            else{
                this.page = Math.max(maxPageCount - 1, 0);
            }
            ClientPlayNetworking.send(new UncraftingPageChangePayload(page, this.handler.blockEntity.getPos()));
        }).position(this.x - 152 + 5, this.y + backgroundHeight - 23).size(16, 16).build();
        this.addDrawableChild(prevButton).render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        ButtonWidget nextButton = ButtonWidget.builder(Text.translatable("screen.uncrafteverything.next_button"), button -> {
            if (this.page < maxPageCount - 1) {
                this.page++;
            }
            else{
                this.page = 0;
            }
            ClientPlayNetworking.send(new UncraftingPageChangePayload(page, this.handler.blockEntity.getPos()));
        }).position(this.x - 21, this.y + backgroundHeight - 23).size(16, 16).build();
        this.addDrawableChild(nextButton).render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    protected void chooseRecipe(RecipeSelectionButton button, int index){
        selectedRecipe = index;
        ClientPlayNetworking.send(new UncraftingRecipeSelectionPayload(this.handler.blockEntity.getPos(), this.recipes.get(selectedRecipe)));
    }

    protected void renderRecipeButton(DrawContext pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick){
        int visibleCount = 0;
        for (int j = 0; j < recipes.size() && visibleCount < MAX_PAGE_SIZE; j++) {
            UncraftingTableRecipe recipe = recipes.get(j);
            int displayIndex = visibleCount;

            int recipeWidth = 9 * 16 + 5;
            Rectangle2D bounds = new Rectangle2D.Double(this.x - recipeWidth, this.y + (displayIndex * 18) + 30, recipeWidth - 3, 18);

            int finalJ = j;
            RecipeSelectionButton button = new RecipeSelectionButton((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight(), Text.translatable("screen.uncrafteverything.blank"), button1 -> chooseRecipe(button1, finalJ));
            if (selectedRecipe == j) {
                button.setFocused(true);
            }
            this.addSelectableChild(button).render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

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
                pGuiGraphics.drawItemWithoutEntity(itemStack, this.x - recipeWidth + (i * 16) + 1, this.y + (displayIndex * 18) + 31);
                pGuiGraphics.drawStackOverlay(this.textRenderer, itemStack, this.x - recipeWidth + (i * 16) + 1, this.y + (displayIndex * 18) + 31);
                if (pMouseX >= this.x - recipeWidth + (i * 16) + 1 && pMouseX <= this.x - recipeWidth + (i * 16) + 17 && pMouseY >= this.y + (displayIndex * 18) + 31 && pMouseY <= this.y + (displayIndex * 18) + 31 + 16) {
                    pGuiGraphics.drawItemTooltip(this.textRenderer, itemStack, pMouseX, pMouseY);
                }
                i++;
            }

            visibleCount++;
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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollDelta) {
        if (mouseX >= this.x - 152 && mouseX <= this.x && mouseY >= this.y && mouseY <= this.y + 184){
            if (scrollDelta == 1.0d && this.page > 0) {
                this.page--;
            } else if (scrollDelta == -1.0d && (this.page + 1) * MAX_PAGE_SIZE < recipeSize) {
                this.page++;
            }
            else if (scrollDelta == 1.0d && this.page == 0 && !recipes.isEmpty()) {
                this.page = (int) Math.ceil((double) recipeSize / MAX_PAGE_SIZE) - 1;
            } else if (scrollDelta == -1.0d && (this.page + 1) * MAX_PAGE_SIZE >= recipeSize) {
                this.page = 0;
            }
            ClientPlayNetworking.send(new UncraftingPageChangePayload(page, this.handler.blockEntity.getPos()));
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

    @Override
    public boolean keyPressed(KeyInput input) {
        this.hasShift = input.hasShift();
        return super.keyPressed(input);
    }

    @Override
    public boolean keyReleased(@NotNull KeyInput keyInput) {
        this.hasShift = false;
        return super.keyReleased(keyInput);
    }

    public boolean hasShift(){
        return hasShift;
    }
}