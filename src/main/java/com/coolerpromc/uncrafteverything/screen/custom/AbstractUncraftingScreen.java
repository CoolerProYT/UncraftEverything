package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.blockentity.custom.AbstractUncraftingTableBE;
import com.coolerpromc.uncrafteverything.compat.ftbquests.QuestHelper;
import com.coolerpromc.uncrafteverything.networking.UncraftingPageChangePayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionPayload;
import com.coolerpromc.uncrafteverything.screen.widget.RecipeSelectionButton;
import com.coolerpromc.uncrafteverything.util.Status;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Rectangle2D;
import java.util.*;

public abstract class AbstractUncraftingScreen<B extends AbstractUncraftingTableBE, T extends AbstractUncraftingMenu<B>> extends AbstractContainerScreen<T> {
    protected static final Identifier RECIPE_PANEL_TEXTURE = Identifier.fromNamespaceAndPath(UncraftEverything.MODID, "textures/gui/recipe_selection_panel.png");

    protected List<UncraftingTableRecipe> recipes = List.of();
    protected int selectedRecipe = 0;
    protected boolean hasShift = false;

    protected static final int SCROLLBAR_WIDTH = 6;
    protected static final int SCROLLBAR_PADDING = 2;
    protected int page = 0;
    protected final int MAX_PAGE_SIZE = 7;
    protected int recipeSize = 0;

    private boolean isInfoHovered = false;

    public AbstractUncraftingScreen(T menu, Inventory playerInventory, Component title, int imageWidth, int imageHeight) {
        super(menu, playerInventory, title, imageWidth, imageHeight);
    }

    public void updateFromBlockEntity(List<UncraftingTableRecipe> recipes, int size, boolean shouldSendPacket) {
        this.recipes = recipes;
        this.recipeSize = size;

        if (size < 7 && this.page != 0){
            this.page = 0;
            ClientPacketDistributor.sendToServer(new UncraftingPageChangePayload(page, this.menu.blockEntity.getBlockPos()));
        }

        if (!recipes.isEmpty()){
            if (selectedRecipe >= this.recipes.size()){
                selectedRecipe = 0;
            }
            if(shouldSendPacket){
                ClientPacketDistributor.sendToServer(new UncraftingRecipeSelectionPayload(this.menu.blockEntity.getBlockPos(), this.recipes.get(selectedRecipe)));
            }
        }
    }

    protected abstract void renderExpRequired(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY);

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float a) {
        super.extractBackground(guiGraphics, mouseX, mouseY, a);

        int buttonSize = 8;
        int x = this.leftPos + this.imageWidth - 5 - buttonSize;
        int y = this.topPos + 5;
        isInfoHovered = mouseX >= x && mouseX <= x + buttonSize && mouseY >= y && mouseY <= y + buttonSize;
        guiGraphics.fill(x, y, x + buttonSize, y + buttonSize, isInfoHovered ? 0xFFFFFFFF: 0xFF000000);
        guiGraphics.fill(x + 1, y + 1, x + buttonSize - 1, y + buttonSize - 1, 0xFF6F6F6F);
        guiGraphics.fill(x + 1, y + 1, x + buttonSize - 2, y + buttonSize - 2, 0xFFAAAAAA);
        guiGraphics.fill(x + 2, y + 2, x + buttonSize - 1, y + buttonSize - 1, 0xFF565656);
        guiGraphics.fill(x + 2, y + 2, x + buttonSize - 2, y + buttonSize - 2, 0xFF6D6D6D);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(0.5f);
        guiGraphics.pose().translate((x + buttonSize / 2f) * 2, ((y + buttonSize / 2f) - this.font.lineHeight / (2f * 2f)) * 2f);
        guiGraphics.centeredText(this.font, Component.literal("?"), 0, 0, 0xFFFFFFFF);
        guiGraphics.pose().popMatrix();
    }

    @Override
    protected void extractTooltip(@NotNull GuiGraphicsExtractor guiGraphics, int x, int y) {
        Status status = Status.byIndex(this.menu.getStatus());
        if (isInfoHovered){
            List<Component> tooltips = new ArrayList<>();
            tooltips.add(Component.translatable("screen.uncrafteverything.tooltip.configs").withStyle(ChatFormatting.BLUE));
            tooltips.add(Component.literal("/ueconfig client").withStyle(ChatFormatting.AQUA).append(Component.translatable("screen.uncrafteverything.tooltip.client_config_info").withStyle(ChatFormatting.GRAY)));
            tooltips.add(Component.empty());
            tooltips.add(Component.literal("/ueconfig common").withStyle(ChatFormatting.AQUA).append(Component.translatable("screen.uncrafteverything.tooltip.common_config_info").withStyle(ChatFormatting.GRAY)));
            tooltips.add(Component.empty());
            tooltips.add(Component.literal("/ueconfig exp").withStyle(ChatFormatting.AQUA).append(Component.translatable("screen.uncrafteverything.tooltip.exp_config_info").withStyle(ChatFormatting.GRAY)));
            if (QuestHelper.FTBQUESTS_LOADED){
                tooltips.add(Component.empty());
                tooltips.add(Component.literal("/ueconfig progression").withStyle(ChatFormatting.AQUA).append(Component.translatable("screen.uncrafteverything.tooltip.progression_config_info").withStyle(ChatFormatting.GRAY)));
            }
            guiGraphics.setTooltipForNextFrame(this.font, tooltips, Optional.empty(), x, y);
        }
        if (this.hoveredSlot != null && status != Status.BLANK && this.hoveredSlot.index == 36){
            ItemStack itemStack = this.hoveredSlot.getItem();
            List<Component> tooltip = new ArrayList<>(this.getTooltipFromContainerItem(itemStack));
            tooltip.add(Component.translatable(status.getTranslationKey()).withColor(status.getOverlay()));
            guiGraphics.setTooltipForNextFrame(this.font, tooltip, itemStack.getTooltipImage(), itemStack, x, y, itemStack.get(DataComponents.TOOLTIP_STYLE));
        }
        else{
            super.extractTooltip(guiGraphics, x, y);
        }
    }

    protected void renderInputSlotOverlay(GuiGraphicsExtractor guiGraphics){
        Status status = Status.byIndex(this.menu.getStatus());

        if (status != Status.BLANK){
            guiGraphics.fill(this.leftPos + 25, this.topPos + 34, this.leftPos + 43, this.topPos + 52, ARGB.color(70, status.getOverlay()));
        }
    }

    protected void renderRecipeSelection(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick){
        int maxPageCount = (int) Math.ceil((double) recipeSize / MAX_PAGE_SIZE);
        int pageToDisplay = recipes.isEmpty() ? 0 : page + 1;

        if (page > maxPageCount - 1) {
            page = 0;
        }

        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, RECIPE_PANEL_TEXTURE, this.leftPos - 152, this.topPos, 0, 0, 152, 184, 152, 184);
        this.drawCenteredWordWrapWithoutShadow(pGuiGraphics, font, Component.translatable("screen.uncrafteverything.uncraft_recipe_selection"), this.leftPos - 75, this.topPos + 7, 0xFF404040);
        this.drawCenteredWordWrapWithoutShadow(pGuiGraphics, font, Component.translatable("screen.uncrafteverything.page",pageToDisplay, maxPageCount), this.leftPos - 75, this.topPos + imageHeight - 18, 0xFF404040);

        this.renderNavigationButton(pGuiGraphics, pMouseX, pMouseY, pPartialTick, maxPageCount);
        this.renderRecipeButton(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        if (selectedRecipe >= recipes.size()) {
            selectedRecipe = 0;
        }
    }

    protected void renderNavigationButton(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick, int maxPageCount){
        Button prevButton = Button.builder(Component.translatable("screen.uncrafteverything.prev_button"), button -> {
            if (this.page > 0) {
                this.page--;
            }
            else{
                this.page = Math.max(maxPageCount - 1, 0);
            }
            ClientPacketDistributor.sendToServer(new UncraftingPageChangePayload(page, this.menu.blockEntity.getBlockPos()));
        }).pos(this.leftPos - 152 + 5, this.topPos + imageHeight - 23).size(16, 16).build();
        this.addRenderableWidget(prevButton).extractRenderState(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        Button nextButton = Button.builder(Component.translatable("screen.uncrafteverything.next_button"), button -> {
            if (this.page < maxPageCount - 1) {
                this.page++;
            }
            else{
                this.page = 0;
            }
            ClientPacketDistributor.sendToServer(new UncraftingPageChangePayload(page, this.menu.blockEntity.getBlockPos()));
        }).pos(this.leftPos - 21, this.topPos + imageHeight - 23).size(16, 16).build();
        this.addRenderableWidget(nextButton).extractRenderState(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    protected void chooseRecipe(RecipeSelectionButton button, int index){
        selectedRecipe = index;
        ClientPacketDistributor.sendToServer(new UncraftingRecipeSelectionPayload(this.menu.blockEntity.getBlockPos(), this.recipes.get(selectedRecipe)));
    }

    protected void renderRecipeButton(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick){
        int visibleCount = 0;
        for (int j = 0; j < recipes.size() && visibleCount < MAX_PAGE_SIZE; j++) {
            UncraftingTableRecipe recipe = recipes.get(j);
            int displayIndex = visibleCount;

            int recipeWidth = 9 * 16 + 5;
            Rectangle2D bounds = new Rectangle2D.Double(this.leftPos - recipeWidth, this.topPos + (displayIndex * 18) + 30, recipeWidth - 3, 18);

            int finalJ = j;
            RecipeSelectionButton button = new RecipeSelectionButton((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight(), Component.translatable("screen.uncrafteverything.blank"), button1 -> chooseRecipe(button1, finalJ));
            if (selectedRecipe == j) {
                button.setFocused(true);
            }
            this.addWidget(button).extractRenderState(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

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
                pGuiGraphics.fakeItem(itemStack, this.leftPos - recipeWidth + (i * 16) + 1, this.topPos + (displayIndex * 18) + 31);
                pGuiGraphics.itemDecorations(this.font, itemStack, this.leftPos - recipeWidth + (i * 16) + 1, this.topPos + (displayIndex * 18) + 31);
                if (pMouseX >= this.leftPos - recipeWidth + (i * 16) + 1 && pMouseX <= this.leftPos - recipeWidth + (i * 16) + 17 && pMouseY >= this.topPos + (displayIndex * 18) + 31 && pMouseY <= this.topPos + (displayIndex * 18) + 31 + 16) {
                    pGuiGraphics.setTooltipForNextFrame(this.font, itemStack, pMouseX, pMouseY);
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
                ClientPacketDistributor.sendToServer(new UncraftingRecipeSelectionPayload(this.menu.blockEntity.getBlockPos(), recipe));
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollDelta) {
        if (mouseX >= this.leftPos - 152 && mouseX <= this.leftPos && mouseY >= this.topPos && mouseY <= this.topPos + 184){
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
            ClientPacketDistributor.sendToServer(new UncraftingPageChangePayload(page, this.menu.blockEntity.getBlockPos()));
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollDelta);
    }

    public void drawCenteredWordWrapWithoutShadow(GuiGraphicsExtractor context, Font textRenderer, Component text, int centerX, int y, int color) {
        List<FormattedCharSequence> lines = textRenderer.split(text, 140);

        int lineHeight = textRenderer.lineHeight + 2;

        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            int lineWidth = textRenderer.width(line);
            int lineX = centerX - lineWidth / 2;
            int lineY = y + (i * lineHeight);

            context.text(textRenderer, line, lineX, lineY, color, false);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        this.hasShift = input.hasShiftDown();
        return super.keyPressed(input);
    }

    @Override
    public boolean keyReleased(@NotNull KeyEvent keyInput) {
        this.hasShift = false;
        return super.keyReleased(keyInput);
    }

    public boolean hasShift(){
        return hasShift;
    }
}
