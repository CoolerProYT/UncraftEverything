package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableCraftButtonClickPayload;
import com.coolerpromc.uncrafteverything.screen.widget.RecipeSelectionButton;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.TranslationTextComponent;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UncraftingTableScreen extends ContainerScreen<UncraftingTableMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(UncraftEverything.MODID, "textures/gui/uncrafting_table_gui.png");
    private static final ResourceLocation RECIPE_PANEL_TEXTURE = new ResourceLocation(UncraftEverything.MODID, "textures/gui/recipe_selection_panel.png");
    private List<UncraftingTableRecipe> recipes = new ArrayList<>();
    private int selectedRecipe = 0;

    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_PADDING = 2;
    private int page = 0;
    private final int MAX_PAGE_SIZE = 7;

    private Button configButton;
    private Button expConfigButton;

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

        super.init();

        this.leftPos = Math.max((width - imageWidth) / 2, (16 * 9) + SCROLLBAR_PADDING + SCROLLBAR_WIDTH);

        int buttonX = leftPos + (imageWidth - 64) - 20;
        int buttonY = topPos + 72;

        this.addButton(new Button(buttonX, buttonY, 64, 20, new TranslationTextComponent("screen.uncrafteverything.uncraft"), this::onPressed));

        if (this.menu.player.isCreative() || this.menu.player.hasPermissions(4)) {
            configButton = new Button(leftPos + imageWidth - 16, topPos + 3, 12, 12, new TranslationTextComponent("screen.uncrafteverything.blank"), this::openConfigScreen);
            this.addWidget(configButton);
            expConfigButton = new Button(leftPos + imageWidth - 30, topPos + 3, 12, 12, new TranslationTextComponent("screen.uncrafteverything.blank"), this::openExpScreen);
            this.addWidget(expConfigButton);
        }
    }

    private void onPressed(Button button) {
        UncraftingTableCraftButtonClickPayload payload = new UncraftingTableCraftButtonClickPayload(this.menu.blockEntity.getBlockPos(), hasShiftDown());
        UncraftingTableCraftButtonClickPayload.INSTANCE.sendToServer(payload);
    }

    private void openConfigScreen(Button button){
        this.getMinecraft().setScreen(new UEConfigScreen(new TranslationTextComponent("screen.uncrafteverything.uncraft_everything_config"), this));
    }

    private void openExpScreen(Button button){
        this.getMinecraft().setScreen(new PerItemExpConfigScreen(this));
    }

    @Override
    protected void renderBg(MatrixStack pGuiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.getMinecraft().getTextureManager().bind(TEXTURE);
        int x = this.leftPos;
        int y = this.topPos;
        blit(pGuiGraphics, x, y, 0, 0, imageWidth, imageHeight);

        configButton.render(pGuiGraphics, mouseX, mouseY, partialTick);
        expConfigButton.render(pGuiGraphics, mouseX, mouseY, partialTick);

        fill(pGuiGraphics, leftPos + imageWidth - 15, topPos + 3 + 11, leftPos + imageWidth - 17 + 12, topPos + 3 + 12, configButton.isHovered() || configButton.isFocused() ? 0xFFFFFFFF : 0xFF000000);
        fill(pGuiGraphics, leftPos + imageWidth - 29, topPos + 3 + 11, leftPos + imageWidth - 31 + 12, topPos + 3 + 12, expConfigButton.isHovered() || expConfigButton.isFocused() ? 0xFFFFFFFF : 0xFF000000);

        this.getMinecraft().getTextureManager().bind(new ResourceLocation(UncraftEverything.MODID, "textures/gui/sprites/config.png"));
        pGuiGraphics.pushPose();
        pGuiGraphics.translate(leftPos + imageWidth - 16 + 2, topPos + 5, 400);
        blit(pGuiGraphics, 0, 0, 0, 0,8, 8, 8, 8);
        pGuiGraphics.popPose();

        this.getMinecraft().getTextureManager().bind(new ResourceLocation(UncraftEverything.MODID, "textures/gui/sprites/exp.png"));
        pGuiGraphics.pushPose();
        pGuiGraphics.translate(leftPos + imageWidth - 30 + 2, topPos + 5, 400);
        blit(pGuiGraphics, 0, 0, 0, 0,8, 8, 8, 8);
        pGuiGraphics.popPose();
    }

    @Override
    public void render(MatrixStack pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);

        this.children.clear();
        this.init();

        int scale = (int) Minecraft.getInstance().getWindow().getGuiScale();

        TranslationTextComponent exp = new TranslationTextComponent("screen.uncrafteverything.exp_" + this.menu.getExpType().toLowerCase() + "_required",this.menu.getExpAmount());
        int expX = leftPos + (imageWidth - 64) - 20 + 32;
        pGuiGraphics.pushPose();
        pGuiGraphics.scale(0.75f, 0.75f, 0.75f);
        pGuiGraphics.translate(expX * 1.3334f, this.topPos * 1.3334f + 124, 1);
        this.drawCenteredWordWrapWithoutShadow(pGuiGraphics, this.font, exp, 0, 0, 0x00AA00);
        pGuiGraphics.popPose();


        int x = this.leftPos;
        int y = this.topPos;
        int maxPageCount = (int) Math.ceil((double) recipes.size() / MAX_PAGE_SIZE);
        int pageToDisplay = recipes.isEmpty() ? 0 : page + 1;

        if (page > maxPageCount - 1) {
            page = 0;
        }

        this.getMinecraft().getTextureManager().bind(RECIPE_PANEL_TEXTURE);
        blit(pGuiGraphics,x - 152, y, 0, 0, 152, 184, 152, 184);
        this.drawCenteredWordWrapWithoutShadow(pGuiGraphics, font, new TranslationTextComponent("screen.uncrafteverything.uncraft_recipe_selection"), x - 75, y + 7, 4210752);
        this.drawCenteredWordWrapWithoutShadow(pGuiGraphics, font, new TranslationTextComponent("screen.uncrafteverything.page", pageToDisplay, maxPageCount), x - 75, y + imageHeight - 18, 4210752);

        Button prevButton = new Button(x - 152 + 5, y + imageHeight - 23, 16, 16, new TranslationTextComponent("screen.uncrafteverything.prev_button"), button -> {
            if (this.page > 0) {
                this.page--;
            }
            else{
                this.page = Math.max(maxPageCount - 1, 0);
            }
        });
        this.addWidget(prevButton).render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        fill(pGuiGraphics, x - 152 + 5, y + imageHeight - 23 + 15, x - 152 + 5 + 16, y + imageHeight - 23 + 16, prevButton.isHovered() || prevButton.isFocused() ? 0xFFFFFFFF : 0xFF000000);

        Button nextButton = new Button(x - 21, y + imageHeight - 23, 16, 16, new TranslationTextComponent("screen.uncrafteverything.next_button"), button -> {
            if (this.page < maxPageCount - 1) {
                this.page++;
            }
            else{
                this.page = 0;
            }
        });
        this.addWidget(nextButton).render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        fill(pGuiGraphics, x - 21, y + imageHeight - 23 + 15, x - 21 + 16, y + imageHeight - 23 + 16, nextButton.isHovered() || nextButton.isFocused() ? 0xFFFFFFFF : 0xFF000000);

        // Render visible recipes
        int visibleCount = 0;
        for (int j = page * MAX_PAGE_SIZE; j < recipes.size() && visibleCount < MAX_PAGE_SIZE; j++) {
            UncraftingTableRecipe recipe = recipes.get(j);
            int displayIndex = visibleCount;

            int recipeWidth = 9 * 16 + 5;
            Rectangle2D bounds = new Rectangle2D.Double(x - recipeWidth, y + (displayIndex * 18) + 30, recipeWidth - 3, 18);

            int finalJ = j;
            RecipeSelectionButton button = new RecipeSelectionButton((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight(), new TranslationTextComponent("screen.uncrafteverything.blank"), ignored -> selectedRecipe = finalJ);
            if (selectedRecipe == j) {
                button.setFocused(true);
            }
            this.addWidget(button).render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            fill(pGuiGraphics, (int) bounds.getX() + 1, (int) (bounds.getY() + bounds.getHeight() - 1), (int) (bounds.getX() + bounds.getWidth()), (int) (bounds.getY() + bounds.getHeight()),0xFFFFFFFF);
            fill(pGuiGraphics, (int) bounds.getX() + 1, (int) (bounds.getY() + bounds.getHeight() - 1), (int) (bounds.getX()), (int) (bounds.getY() + bounds.getHeight()),0xFF8B8B8B);

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
                pGuiGraphics.pushPose();
                itemRenderer.renderAndDecorateFakeItem(itemStack, x - recipeWidth + (i * 16) + 1, y + (displayIndex * 18) + 31);
                itemRenderer.renderGuiItemDecorations(this.font, itemStack, x - recipeWidth + (i * 16) + 1, y + (displayIndex * 18) + 31);
                if (pMouseX >= x - recipeWidth + (i * 16) + 1 && pMouseX <= x - recipeWidth + (i * 16) + 17 && pMouseY >= y + (displayIndex * 18) + 31 && pMouseY <= y + (displayIndex * 18) + 31 + 16) {
                    renderTooltip(pGuiGraphics, itemStack, pMouseX, pMouseY);
                }
                i++;
            }

            visibleCount++;
        }

        if (selectedRecipe >= recipes.size()) {
            selectedRecipe = 0;
        }

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
                case 0 : statusText = "screen.uncrafteverything.no_recipe_found"; break;
                case 1 : statusText = "screen.uncrafteverything.no_suitable_output_slot"; break;
                case 2 : statusText = "screen.uncrafteverything.not_enough_exp"; break;
                case 3 : statusText = "screen.uncrafteverything.not_enough_input"; break;
                case 4 : statusText = "screen.uncrafteverything.not_empty_shulker"; break;
                case 5 : statusText = "screen.uncrafteverything.restricted_by_config"; break;
                case 6 : statusText = "screen.uncrafteverything.damaged_item"; break;
                case 7 : statusText = "screen.uncrafteverything.enchanted_item"; break;
                default : statusText = "screen.uncrafteverything.blank";
            }

            int textY = y;

            pGuiGraphics.pushPose();
            pGuiGraphics.translate(0, 0, 400);
            fill(pGuiGraphics, x + 97, y + 16, x + 151, y + 70, 0xAA8B8B8B);
            pGuiGraphics.popPose();
            List<IReorderingProcessor> formattedText = font.split(ITextProperties.of(new TranslationTextComponent(statusText).getString()), 54);

            switch (formattedText.size()){
                case 1 : textY += 38; break;
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

        if (this.menu.player.hasPermissions(4) || this.menu.player.isCreative()){
            if (pMouseX >= leftPos + imageWidth - 16 && pMouseX <= leftPos + imageWidth - 4 && pMouseY >= topPos + 3 && pMouseY <= topPos + 15) {
                renderTooltip(pGuiGraphics, new TranslationTextComponent("screen.uncrafteverything.uncraft_everything_config"), pMouseX, pMouseY);
            }

            if (pMouseX >= leftPos + imageWidth - 30 && pMouseX <= leftPos + imageWidth - 18 && pMouseY >= topPos + 3 && pMouseY <= topPos + 15) {
                renderTooltip(pGuiGraphics, new TranslationTextComponent("screen.uncrafteverything.per_item_xp_config"), pMouseX, pMouseY);
            }
        }

        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if (scrollDelta == 1.0d && this.page > 0) {
            this.page--;
        } else if (scrollDelta == -1.0d && (this.page + 1) * MAX_PAGE_SIZE < recipes.size()) {
            this.page++;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollDelta);
    }

    public void drawCenteredWordWrapWithoutShadow(MatrixStack context, FontRenderer textRenderer, TranslationTextComponent text, int centerX, int y, int color) {
        List<IReorderingProcessor> lines = textRenderer.split(text, 140);

        int lineHeight = textRenderer.lineHeight + 2;

        for (int i = 0; i < lines.size(); i++) {
            IReorderingProcessor line = lines.get(i);
            int lineWidth = textRenderer.width(line);
            int lineX = centerX - lineWidth / 2;
            int lineY = y + (i * lineHeight);

            textRenderer.draw(context, line, lineX, lineY, color);
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
                UncraftingRecipeSelectionPayload.INSTANCE.sendToServer(new UncraftingRecipeSelectionPayload(this.menu.blockEntity.getBlockPos(), recipe));
            }
        }
    }
}
