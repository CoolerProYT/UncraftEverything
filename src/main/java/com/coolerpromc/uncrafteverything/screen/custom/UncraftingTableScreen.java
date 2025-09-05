package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionDataPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableCraftButtonClickPayload;
import com.coolerpromc.uncrafteverything.screen.widget.RecipeSelectionButton;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("removal")
public class UncraftingTableScreen extends AbstractContainerScreen<UncraftingTableMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(UncraftEverything.MODID, "textures/gui/uncrafting_table_gui.png");
    private static final ResourceLocation RECIPE_PANEL_TEXTURE = new ResourceLocation(UncraftEverything.MODID, "textures/gui/recipe_selection_panel.png");
    private List<UncraftingTableRecipe> recipes = List.of();
    private int selectedRecipe = 0;

    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_PADDING = 2;
    private int page = 0;
    private final int MAX_PAGE_SIZE = 7;
    private int recipeSize = 0;

    private Button configButton;
    private Button expConfigButton;

    public UncraftingTableScreen(UncraftingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    public void updateFromBlockEntity(List<UncraftingTableRecipe> recipes, int size) {
        this.recipes = recipes;
        this.recipeSize = size;

        if (size < 7 && this.page != 0){
            this.page = 0;
            UncraftingRecipeSelectionDataPayload.INSTANCE.send(PacketDistributor.SERVER.noArg(), new UncraftingRecipeSelectionDataPayload(page, this.menu.blockEntity.getBlockPos()));
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

        this.addRenderableWidget(new Button(buttonX, buttonY, 64, 20, Component.translatable("screen.uncrafteverything.uncraft"), this::onPressed));

        if (this.menu.player.isCreative() || this.menu.player.hasPermissions(4)) {
            configButton = new Button(leftPos + imageWidth - 16, topPos + 3, 12, 12, Component.empty(), this::openConfigScreen);
            expConfigButton = new Button(leftPos + imageWidth - 30, topPos + 3, 12, 12, Component.empty(), this::openExpScreen);
            this.addWidget(configButton);
            this.addWidget(expConfigButton);
        }
    }

    private void onPressed(Button button) {
        UncraftingTableCraftButtonClickPayload payload = new UncraftingTableCraftButtonClickPayload(this.menu.blockEntity.getBlockPos(), hasShiftDown());
        UncraftingTableCraftButtonClickPayload.INSTANCE.sendToServer(payload);
    }

    private void openConfigScreen(Button button){
        this.getMinecraft().setScreen(new UEConfigScreen(Component.translatable("screen.uncrafteverything.uncraft_everything_config"), this));
    }

    private void openExpScreen(Button button){
        this.getMinecraft().setScreen(new PerItemExpConfigScreen(this));
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = this.leftPos;
        int y = this.topPos;

        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, x, y, 0, 0, imageWidth, imageHeight);

        if (this.menu.player.isCreative() || this.menu.player.hasPermissions(4)){
            configButton.render(poseStack, mouseX, mouseY, partialTick);
            expConfigButton.render(poseStack, mouseX, mouseY, partialTick);

            fill(poseStack, leftPos + imageWidth - 15, topPos + 3 + 11, leftPos + imageWidth - 17 + 12, topPos + 3 + 12, configButton.isHoveredOrFocused() ? 0xFFFFFFFF : 0xFF000000);
            fill(poseStack, leftPos + imageWidth - 29, topPos + 3 + 11, leftPos + imageWidth - 31 + 12, topPos + 3 + 12, expConfigButton.isHoveredOrFocused() ? 0xFFFFFFFF : 0xFF000000);

            poseStack.pushPose();
            poseStack.translate(leftPos + imageWidth - 16 + 2, topPos + 5, 400);
            RenderSystem.setShaderTexture(0, new ResourceLocation(UncraftEverything.MODID, "textures/gui/sprites/config.png"));
            blit(poseStack, 0, 0, 0, 0,8, 8, 8, 8);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(leftPos + imageWidth - 30 + 2, topPos + 5, 400);
            RenderSystem.setShaderTexture(0, new ResourceLocation(UncraftEverything.MODID, "textures/gui/sprites/exp.png"));
            blit(poseStack, 0, 0, 0, 0,8, 8, 8, 8);
            poseStack.popPose();
        }
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(poseStack);
        super.render(poseStack, pMouseX, pMouseY, pPartialTick);

        this.clearWidgets();
        this.init();

        Component exp = Component.translatable("screen.uncrafteverything.exp_" + this.menu.getExpType().toLowerCase() + "_required",this.menu.getExpAmount());
        int expX = leftPos + (imageWidth - 64) - 20 + 32;
        poseStack.pushPose();
        poseStack.scale(0.75f, 0.75f, 0.75f);
        poseStack.translate(expX * 1.3334f, this.topPos * 1.3334f + 124, 1);
        this.drawCenteredWordWrapWithoutShadow(poseStack, this.font, exp, 0, 0, 0xFF00AA00);
        poseStack.popPose();

        int x = this.leftPos;
        int y = this.topPos;
        int maxPageCount = (int) Math.ceil((double) recipeSize / MAX_PAGE_SIZE);
        int pageToDisplay = recipes.isEmpty() ? 0 : page + 1;

        if (page > maxPageCount - 1) {
            page = 0;
        }

        RenderSystem.setShaderTexture(0, RECIPE_PANEL_TEXTURE);
        blit(poseStack, x - 152, y, 0, 0, 152, 184, 152, 184);
        this.drawCenteredWordWrapWithoutShadow(poseStack, font, Component.translatable("screen.uncrafteverything.uncraft_recipe_selection"), x - 75, y + 7, 0xFF404040);
        this.drawCenteredWordWrapWithoutShadow(poseStack, font, Component.translatable("screen.uncrafteverything.page", pageToDisplay, maxPageCount), x - 75, y + imageHeight - 18, 0xFF404040);

        Button prevButton = new Button(x - 152 + 5, y + imageHeight - 23, 16, 16, Component.translatable("screen.uncrafteverything.prev_button"), button -> {
            if (this.page > 0) {
                this.page--;
            }
            else{
                this.page = Math.max(maxPageCount - 1, 0);
            }
            UncraftingRecipeSelectionDataPayload.INSTANCE.send(PacketDistributor.SERVER.noArg(), new UncraftingRecipeSelectionDataPayload(page, this.menu.blockEntity.getBlockPos()));
        });
        this.addRenderableWidget(prevButton).render(poseStack, pMouseX, pMouseY, pPartialTick);
        fill(poseStack, x - 152 + 5, y + imageHeight - 23 + 15, x - 152 + 5 + 16, y + imageHeight - 23 + 16, prevButton.isHoveredOrFocused() ? 0xFFFFFFFF : 0xFF000000);

        Button nextButton = new Button(x - 21, y + imageHeight - 23, 16, 16, Component.translatable("screen.uncrafteverything.next_button"), button -> {
            if (this.page < maxPageCount - 1) {
                this.page++;
            }
            else{
                this.page = 0;
            }
            UncraftingRecipeSelectionDataPayload.INSTANCE.send(PacketDistributor.SERVER.noArg(), new UncraftingRecipeSelectionDataPayload(page, this.menu.blockEntity.getBlockPos()));
        });
        this.addRenderableWidget(nextButton).render(poseStack, pMouseX, pMouseY, pPartialTick);
        fill(poseStack, x - 21, y + imageHeight - 23 + 15, x - 21 + 16, y + imageHeight - 23 + 16, nextButton.isHoveredOrFocused() ? 0xFFFFFFFF : 0xFF000000);

        // Render visible recipes
        int visibleCount = 0;
        for (int j = 0; j < recipes.size() && visibleCount < MAX_PAGE_SIZE; j++) {
            UncraftingTableRecipe recipe = recipes.get(j);
            int displayIndex = visibleCount;

            int recipeWidth = 9 * 16 + 5;
            Rectangle2D bounds = new Rectangle2D.Double(x - recipeWidth, y + (displayIndex * 18) + 30, recipeWidth - 3, 18);

            int finalJ = j;
            RecipeSelectionButton button = new RecipeSelectionButton((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight(), Component.translatable("screen.uncrafteverything.blank"), ignored -> selectedRecipe = finalJ);
            if (selectedRecipe == j) {
                button.setFocused(true);
            }
            this.addRenderableWidget(button).render(poseStack, pMouseX, pMouseY, pPartialTick);
            fill(poseStack, (int) bounds.getX() + 1, (int) (bounds.getY() + bounds.getHeight() - 1), (int) (bounds.getX() + bounds.getWidth()), (int) (bounds.getY() + bounds.getHeight()),0xFFFFFFFF);
            fill(poseStack, (int) bounds.getX() + 1, (int) (bounds.getY() + bounds.getHeight() - 1), (int) (bounds.getX()), (int) (bounds.getY() + bounds.getHeight()),0xFF8B8B8B);

            int i = 0;
            Map<Item, Integer> inputs = new HashMap<>();
            Map<Item, CompoundTag> inputComponents = new HashMap<>();

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
                itemRenderer.renderAndDecorateFakeItem(itemStack, x - recipeWidth + (i * 16) + 1, y + (displayIndex * 18) + 31);
                itemRenderer.renderGuiItemDecorations(this.font, itemStack, x - recipeWidth + (i * 16) + 1, y + (displayIndex * 18) + 31);
                if (pMouseX >= x - recipeWidth + (i * 16) + 1 && pMouseX <= x - recipeWidth + (i * 16) + 17 && pMouseY >= y + (displayIndex * 18) + 31 && pMouseY <= y + (displayIndex * 18) + 31 + 16) {
                    renderTooltip(poseStack, itemStack, pMouseX, pMouseY);
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
                itemRenderer.renderAndDecorateFakeItem(
                        itemStack,
                        x + 98 + 18 * (i % 3),
                        y + 17 + (i / 3) * 18);
                RenderSystem.disableDepthTest();
                fill(
                        poseStack,
                        x + 98 + 18 * (i % 3),
                        y + 17 + (i / 3) * 18,
                        x + 98 + 18 * (i % 3) + 16,
                        y + 17 + (i / 3) * 18 + 16,
                        0x998B8B8B);
                RenderSystem.enableDepthTest();
            }
        }

        int status = this.menu.getStatus();

        if (status != -1){
            Component statusText = Component.translatable(switch (status){
                case 0 -> "screen.uncrafteverything.no_recipe_found";
                case 1 -> "screen.uncrafteverything.no_suitable_output_slot";
                case 2 -> "screen.uncrafteverything.not_enough_exp";
                case 3 -> "screen.uncrafteverything.not_enough_input";
                case 4 -> "not_empty_shulker";
                case 5 -> "screen.uncrafteverything.restricted_by_config";
                case 6 -> "screen.uncrafteverything.damaged_item";
                case 7 -> "screen.uncrafteverything.enchanted_item";
                default -> "screen.uncrafteverything.blank";
            });

            int textY = y;

            poseStack.pushPose();
            poseStack.translate(0, 0, 390);
            fill(poseStack, x + 97, y + 16, x + 151, y + 70, 0xAA8B8B8B);
            poseStack.popPose();
            List<FormattedCharSequence> formattedText = font.split(FormattedText.of(statusText.getString()), 54);

            switch (formattedText.size()){
                case 1 -> textY += 38;
                case 2 -> textY += 34;
                case 3 -> textY += 30;
                case 4 -> textY += 23;
                default -> textY += 27;
            }

            for (FormattedCharSequence formattedcharsequence : formattedText) {
                int textWidth = font.width(formattedcharsequence);
                int centeredX = x + 97 + (54 - textWidth) / 2;
                poseStack.pushPose();
                poseStack.translate(centeredX, textY, 390);
                font.draw(poseStack, formattedcharsequence, 0, 0, 0xAA0000);
                poseStack.popPose();
                textY += 9;
            }
        }

        if (this.menu.player.hasPermissions(4) || this.menu.player.isCreative()){
            if (pMouseX >= leftPos + imageWidth - 16 && pMouseX <= leftPos + imageWidth - 4 && pMouseY >= topPos + 3 && pMouseY <= topPos + 15) {
                renderTooltip(poseStack, Component.translatable("screen.uncrafteverything.uncraft_everything_config"), pMouseX, pMouseY);
            }

            if (pMouseX >= leftPos + imageWidth - 30 && pMouseX <= leftPos + imageWidth - 18 && pMouseY >= topPos + 3 && pMouseY <= topPos + 15) {
                renderTooltip(poseStack, Component.translatable("screen.uncrafteverything.per_item_xp_config"), pMouseX, pMouseY);
            }
        }

        renderTooltip(poseStack, pMouseX, pMouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
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
        UncraftingRecipeSelectionDataPayload.INSTANCE.send(PacketDistributor.SERVER.noArg(), new UncraftingRecipeSelectionDataPayload(page, this.menu.blockEntity.getBlockPos()));
        return super.mouseScrolled(mouseX, mouseY, scrollDelta);
    }

    public void drawCenteredWordWrapWithoutShadow(PoseStack context, Font textRenderer, Component text, int centerX, int y, int color) {
        List<FormattedCharSequence> lines = textRenderer.split(text, 140);

        int lineHeight = textRenderer.lineHeight + 2;

        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
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
