package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.compat.ftbquests.QuestHelper;
import com.coolerpromc.uncrafteverything.networking.UncraftingPageChangePayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableCraftButtonClickPayload;
import com.coolerpromc.uncrafteverything.screen.widget.RecipeSelectionButton;
import com.coolerpromc.uncrafteverything.util.Status;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Rectangle2D;
import java.util.*;

public class UncraftingTableScreen extends AbstractContainerScreen<UncraftingTableMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "textures/gui/uncrafting_table_gui.png");
    private static final ResourceLocation RECIPE_PANEL_TEXTURE = ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "textures/gui/recipe_selection_panel.png");
    private List<UncraftingTableRecipe> recipes = List.of();
    private int selectedRecipe = 0;
    private boolean hasShift = false;

    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_PADDING = 2;
    private int page = 0;
    private final int MAX_PAGE_SIZE = 7;
    private int recipeSize = 0;

    public UncraftingTableScreen(UncraftingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    public void updateFromBlockEntity(List<UncraftingTableRecipe> recipes, int size) {
        this.recipes = recipes;
        this.recipeSize = size;

        if (size < 7 && this.page != 0){
            this.page = 0;
            ClientPacketDistributor.sendToServer(new UncraftingPageChangePayload(page, this.menu.blockEntity.getBlockPos()));
        }

        if (!recipes.isEmpty()){
            ClientPacketDistributor.sendToServer(new UncraftingRecipeSelectionPayload(this.menu.blockEntity.getBlockPos(), this.recipes.get(selectedRecipe)));
        }
    }

    @Override
    protected void init() {
        this.imageHeight = 184;
        this.inventoryLabelY = this.imageHeight - 94;

        super.init();

        this.leftPos = Math.max((width - imageWidth) / 2, (16 * 9) + SCROLLBAR_PADDING + SCROLLBAR_WIDTH);

        int buttonX = leftPos + (imageWidth - 64) - 20;
        int buttonY = topPos + 72;

        this.addRenderableWidget(Button.builder(Component.translatable("screen.uncrafteverything.uncraft"), this::onPressed).pos(buttonX, buttonY).size(64, 16).build());

        addConfigButtons();
    }

    private void addConfigButtons(){
        if (this.menu.player.isCreative() || this.menu.player.hasPermissions(4)){
            SpriteIconButton configButton = SpriteIconButton
                    .builder(Component.translatable("screen.uncrafteverything.blank"), this::openConfigScreen, true).size(12, 12).sprite(ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "config"), 8, 8)
                    .build();
            configButton.setX(leftPos + imageWidth - 16);
            configButton.setY(topPos + 3);
            this.addRenderableWidget(configButton);

            SpriteIconButton expButton = SpriteIconButton
                    .builder(Component.translatable("screen.uncrafteverything.blank"), this::openExpScreen, true).size(12, 12).sprite(ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "exp"), 8, 8)
                    .build();
            expButton.setX(leftPos + imageWidth - 30);
            expButton.setY(topPos + 3);
            this.addRenderableWidget(expButton);

            if (QuestHelper.FTBQUESTS_LOADED){
                SpriteIconButton progressionButton = SpriteIconButton
                        .builder(Component.translatable("screen.uncrafteverything.blank"), this::openProgressionScreen, true).size(12, 12).sprite(ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "book"), 8, 8)
                        .build();
                progressionButton.setX(leftPos + imageWidth - 44);
                progressionButton.setY(topPos + 3);
                this.addRenderableWidget(progressionButton);
            }
        }
    }

    private void onPressed(Button button) {
        UncraftingTableCraftButtonClickPayload payload = new UncraftingTableCraftButtonClickPayload(this.menu.blockEntity.getBlockPos(), hasShift());
        ClientPacketDistributor.sendToServer(payload);
    }

    private void openConfigScreen(Button button){
        this.getMinecraft().setScreen(new UEConfigScreen(Component.translatable("screen.uncrafteverything.uncraft_everything_config"), this));
    }

    private void openExpScreen(Button button){
        this.getMinecraft().setScreen(new PerItemExpConfigScreen(this));
    }

    private void openProgressionScreen(Button button){
        this.getMinecraft().setScreen(new FTBQuestsProgressionConfigScreen(this));
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float partialTick, int mouseX, int mouseY) {
        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.clearWidgets();
        this.init();

        this.renderExpRequired(pGuiGraphics, pMouseX, pMouseY);
        this.renderRecipeSelection(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderOutputPreview(pGuiGraphics);
        super.renderContents(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderInputSlotOverlay(pGuiGraphics);
        super.renderCarriedItem(pGuiGraphics, pMouseX, pMouseY);
        super.renderSnapbackItem(pGuiGraphics);
        this.renderConfigButtonTooltip(pGuiGraphics, pMouseX, pMouseY);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    private void renderExpRequired(GuiGraphics guiGraphics, int mouseX, int mouseY){
        ResourceLocation icon = ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "textures/gui/sprites/exp.png");
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(0.5f, 0.5f);
        guiGraphics.pose().translate((this.leftPos + imageWidth - 17) * 2, (this.topPos + 72) * 2);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, icon, 0, 0, 0, 0, 16, 16, 16, 16);
        guiGraphics.pose().popMatrix();

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(0.75f, 0.75f);
        guiGraphics.pose().translate((this.leftPos + imageWidth - 12) * 1.3334f, (this.topPos + 82) * 1.3334f);
        this.drawCenteredWordWrapWithoutShadow(guiGraphics, this.font, Component.literal(this.menu.getExpAmount() + ""), 0, 0, 0xFF00AA00);
        guiGraphics.pose().popMatrix();

        if (mouseX >= (this.leftPos + imageWidth - 19) && mouseX <= (this.leftPos + imageWidth - 7) && mouseY >= this.topPos + 72 && mouseY <= this.topPos + 87){
            Component exp = Component.translatable("screen.uncrafteverything.exp_" + this.menu.getExpType().toLowerCase() + "_required",this.menu.getExpAmount());
            guiGraphics.setTooltipForNextFrame(this.font, exp, mouseX, mouseY);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        Status status = Status.byIndex(this.menu.getStatus());

        if (this.getSlotUnderMouse() instanceof ResourceHandlerSlot slot && slot.getResourceHandler().size() == 1 && status != Status.BLANK){
            List<Component> tooltip = new ArrayList<>();
            if (slot.hasItem()){
                tooltip.addAll(this.getTooltipFromContainerItem(slot.getItem()));
            }
            tooltip.add(Component.translatable(status.getTranslationKey()).withColor(status.getOverlay()));
            guiGraphics.setTooltipForNextFrame(this.font, tooltip, Optional.empty(), x, y);
        }
        else{
            super.renderTooltip(guiGraphics, x, y);
        }
    }

    private void renderInputSlotOverlay(GuiGraphics guiGraphics){
        Status status = Status.byIndex(this.menu.getStatus());

        if (status != Status.BLANK){
            guiGraphics.fill(this.leftPos + 25, this.topPos + 34, this.leftPos + 43, this.topPos + 52, ARGB.color(70, status.getOverlay()));
        }
    }

    private void renderRecipeSelection(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick){
        int maxPageCount = (int) Math.ceil((double) recipeSize / MAX_PAGE_SIZE);
        int pageToDisplay = recipes.isEmpty() ? 0 : page + 1;

        if (page > maxPageCount - 1) {
            page = 0;
        }

        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, RECIPE_PANEL_TEXTURE, this.leftPos - 152, this.topPos, 0, 0, 152, 184, 152, 184);
        this.drawCenteredWordWrapWithoutShadow(pGuiGraphics, font, Component.translatable("screen.uncrafteverything.uncraft_recipe_selection"), this.leftPos - 75, this.topPos + 7, 0xFF404040);
        this.drawCenteredWordWrapWithoutShadow(pGuiGraphics, font, Component.translatable("screen.uncrafteverything.page",pageToDisplay, maxPageCount), this.leftPos - 75, this.topPos + imageHeight - 18, 0xFF404040);

        Button prevButton = Button.builder(Component.translatable("screen.uncrafteverything.prev_button"), button -> {
            if (this.page > 0) {
                this.page--;
            }
            else{
                this.page = Math.max(maxPageCount - 1, 0);
            }
            ClientPacketDistributor.sendToServer(new UncraftingPageChangePayload(page, this.menu.blockEntity.getBlockPos()));
        }).pos(this.leftPos - 152 + 5, this.topPos + imageHeight - 23).size(16, 16).build();
        this.addRenderableWidget(prevButton).render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        Button nextButton = Button.builder(Component.translatable("screen.uncrafteverything.next_button"), button -> {
            if (this.page < maxPageCount - 1) {
                this.page++;
            }
            else{
                this.page = 0;
            }
            ClientPacketDistributor.sendToServer(new UncraftingPageChangePayload(page, this.menu.blockEntity.getBlockPos()));
        }).pos(this.leftPos - 21, this.topPos + imageHeight - 23).size(16, 16).build();
        this.addRenderableWidget(nextButton).render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        // Render visible recipes
        int visibleCount = 0;
        for (int j = 0; j < recipes.size() && visibleCount < MAX_PAGE_SIZE; j++) {
            UncraftingTableRecipe recipe = recipes.get(j);
            int displayIndex = visibleCount;

            int recipeWidth = 9 * 16 + 5;
            Rectangle2D bounds = new Rectangle2D.Double(this.leftPos - recipeWidth, this.topPos + (displayIndex * 18) + 30, recipeWidth - 3, 18);

            int finalJ = j;
            RecipeSelectionButton button = new RecipeSelectionButton((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight(), Component.translatable("screen.uncrafteverything.blank"), ignored -> {
                selectedRecipe = finalJ;
                ClientPacketDistributor.sendToServer(new UncraftingRecipeSelectionPayload(this.menu.blockEntity.getBlockPos(), this.recipes.get(selectedRecipe)));
            });
            if (selectedRecipe == j) {
                button.setFocused(true);
            }
            this.addWidget(button).render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

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
                pGuiGraphics.renderFakeItem(itemStack, this.leftPos - recipeWidth + (i * 16) + 1, this.topPos + (displayIndex * 18) + 31);
                pGuiGraphics.renderItemDecorations(this.font, itemStack, this.leftPos - recipeWidth + (i * 16) + 1, this.topPos + (displayIndex * 18) + 31);
                if (pMouseX >= this.leftPos - recipeWidth + (i * 16) + 1 && pMouseX <= this.leftPos - recipeWidth + (i * 16) + 17 && pMouseY >= this.topPos + (displayIndex * 18) + 31 && pMouseY <= this.topPos + (displayIndex * 18) + 31 + 16) {
                    pGuiGraphics.setTooltipForNextFrame(this.font, itemStack, pMouseX, pMouseY);
                }
                i++;
            }

            visibleCount++;
        }

        if (selectedRecipe >= recipes.size()) {
            selectedRecipe = 0;
        }
    }

    private void renderOutputPreview(GuiGraphics guiGraphics){
        if (!recipes.isEmpty()) {
            List<ItemStack> outputs = this.recipes.get(selectedRecipe).getOutputs();
            for (int i = 0; i < outputs.size(); i++) {
                ItemStack itemStack = outputs.get(i);
                guiGraphics.renderFakeItem(
                        itemStack,
                        this.leftPos + 98 + 18 * (i % 3),
                        this.topPos + 17 + (i / 3) * 18);
                guiGraphics.fill(
                        this.leftPos + 98 + 18 * (i % 3),
                        this.topPos + 17 + (i / 3) * 18,
                        this.leftPos + 98 + 18 * (i % 3) + 16,
                        this.topPos + 17 + (i / 3) * 18 + 16,
                        0xAA8B8B8B);
            }
        }
    }

    private void renderConfigButtonTooltip(GuiGraphics guiGraphics, int pMouseX, int pMouseY){
        if (this.menu.player.hasPermissions(4) || this.menu.player.isCreative()){
            if (pMouseX >= leftPos + imageWidth - 16 && pMouseX <= leftPos + imageWidth - 4 && pMouseY >= topPos + 3 && pMouseY <= topPos + 15) {
                guiGraphics.setTooltipForNextFrame(this.font, Component.translatable("screen.uncrafteverything.uncraft_everything_config"), pMouseX, pMouseY);
            }

            if (pMouseX >= leftPos + imageWidth - 30 && pMouseX <= leftPos + imageWidth - 18 && pMouseY >= topPos + 3 && pMouseY <= topPos + 15) {
                guiGraphics.setTooltipForNextFrame(this.font, Component.translatable("screen.uncrafteverything.per_item_xp_config"), pMouseX, pMouseY);
            }

            if (pMouseX >= leftPos + imageWidth - 44 && pMouseX <= leftPos + imageWidth - 32 && pMouseY >= topPos + 3 && pMouseY <= topPos + 15 && QuestHelper.FTBQUESTS_LOADED) {
                guiGraphics.setTooltipForNextFrame(this.font, Component.translatable("screen.uncrafteverything.ftb_quest_progression_config"), pMouseX, pMouseY);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollDelta) {
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
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollDelta);
    }

    public void drawCenteredWordWrapWithoutShadow(GuiGraphics context, Font textRenderer, Component text, int centerX, int y, int color) {
        List<FormattedCharSequence> lines = textRenderer.split(text, 140);

        int lineHeight = textRenderer.lineHeight + 2;

        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            int lineWidth = textRenderer.width(line);
            int lineX = centerX - lineWidth / 2;
            int lineY = y + (i * lineHeight);

            context.drawString(textRenderer, line, lineX, lineY, color, false);
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
    public boolean keyPressed(KeyEvent input) {
        this.hasShift = input.hasShiftDown();
        return super.keyPressed(input);
    }

    @Override
    public boolean keyReleased(KeyEvent keyInput) {
        this.hasShift = false;
        return super.keyReleased(keyInput);
    }

    public boolean hasShift(){
        return hasShift;
    }
}
