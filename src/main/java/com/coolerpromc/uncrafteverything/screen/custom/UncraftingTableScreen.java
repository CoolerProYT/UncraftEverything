package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableCraftButtonClickPayload;
import com.coolerpromc.uncrafteverything.screen.widget.RecipeSelectionButton;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UncraftingTableScreen extends AbstractContainerScreen<UncraftingTableMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "textures/gui/uncrafting_table_gui.png");
    private static final ResourceLocation RECIPE_PANEL_TEXTURE = ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "textures/gui/recipe_selection_panel.png");
    private List<UncraftingTableRecipe> recipes = List.of();
    private int selectedRecipe = 0;

    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_PADDING = 2;
    private int page = 0;
    private final int MAX_PAGE_SIZE = 7;

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

        super.init();

        this.leftPos = Math.max((width - imageWidth) / 2, (16 * 9) + SCROLLBAR_PADDING + SCROLLBAR_WIDTH);

        int buttonX = leftPos + (imageWidth - 64) - 20;
        int buttonY = topPos + 72;

        this.addRenderableWidget(Button
                .builder(Component.translatable("screen.uncrafteverything.uncraft"), this::onPressed).pos(buttonX, buttonY).size(64, 16)
                .build());

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
        }
    }

    private void onPressed(Button button) {
        UncraftingTableCraftButtonClickPayload payload = new UncraftingTableCraftButtonClickPayload(this.menu.blockEntity.getBlockPos(), hasShiftDown());
        PacketDistributor.sendToServer(payload);
    }

    private void openConfigScreen(Button button){
        this.getMinecraft().setScreen(new UEConfigScreen(Component.translatable("screen.uncrafteverything.uncraft_everything_config"), this));
    }

    private void openExpScreen(Button button){
        this.getMinecraft().setScreen(new PerItemExpConfigScreen(this));
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = this.leftPos;
        int y = this.topPos;

        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        this.clearWidgets();
        this.init();

        Component exp = Component.translatable("screen.uncrafteverything.exp_" + this.menu.getExpType().toLowerCase() + "_required",this.menu.getExpAmount());
        int expX = leftPos + (imageWidth - 64) - 20 + 32;
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(0.75f, 0.75f, 0.75f);
        pGuiGraphics.pose().translate(expX * 1.3334f, this.topPos * 1.3334f + 121, 0);
        this.drawCenteredWordWrapWithoutShadow(pGuiGraphics, this.font, exp, 0, 0, 0xFF00AA00);
        pGuiGraphics.pose().popPose();


        int x = this.leftPos;
        int y = this.topPos;
        int maxPageCount = (int) Math.ceil((double) recipes.size() / MAX_PAGE_SIZE);
        int pageToDisplay = recipes.isEmpty() ? 0 : page + 1;

        if (page > maxPageCount - 1) {
            page = 0;
        }

        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate(0, 0, 600);
        pGuiGraphics.blit(RECIPE_PANEL_TEXTURE, x - 152, y, 0, 0, 152, 184, 152, 184);
        this.drawCenteredWordWrapWithoutShadow(pGuiGraphics, font, Component.translatable("screen.uncrafteverything.uncraft_recipe_selection"), x - 75, y + 7, 0xFF404040);
        this.drawCenteredWordWrapWithoutShadow(pGuiGraphics, font, Component.translatable("screen.uncrafteverything.page", pageToDisplay, maxPageCount), x - 75, y + imageHeight - 18, 0xFF404040);

        Button prevButton = Button.builder(Component.translatable("screen.uncrafteverything.prev_button"), button -> {
            if (this.page > 0) {
                this.page--;
            }
            else{
                this.page = Math.max(maxPageCount - 1, 0);
            }
        }).pos(x - 152 + 5, y + imageHeight - 23).size(16, 16).build();
        this.addRenderableWidget(prevButton).render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        Button nextButton = Button.builder(Component.translatable("screen.uncrafteverything.next_button"), button -> {
            if (this.page < maxPageCount - 1) {
                this.page++;
            }
            else{
                this.page = 0;
            }
        }).pos(x - 21, y + imageHeight - 23).size(16, 16).build();
        this.addRenderableWidget(nextButton).render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        // Render visible recipes
        int visibleCount = 0;
        for (int j = page * MAX_PAGE_SIZE; j < recipes.size() && visibleCount < MAX_PAGE_SIZE; j++) {
            UncraftingTableRecipe recipe = recipes.get(j);
            int displayIndex = visibleCount;

            int recipeWidth = 9 * 16 + 5;
            Rectangle2D bounds = new Rectangle2D.Double(x - recipeWidth, y + (displayIndex * 18) + 30, recipeWidth - 3, 18);

            int finalJ = j;
            RecipeSelectionButton button = new RecipeSelectionButton((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight(), Component.translatable("screen.uncrafteverything.blank"), ignored -> selectedRecipe = finalJ);
            if (selectedRecipe == j) {
                button.setFocused(true);
            }
            this.addRenderableWidget(button).render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

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
                pGuiGraphics.renderFakeItem(itemStack, x - recipeWidth + (i * 16) + 1, y + (displayIndex * 18) + 31);
                pGuiGraphics.renderItemDecorations(this.font, itemStack, x - recipeWidth + (i * 16) + 1, y + (displayIndex * 18) + 31);
                if (pMouseX >= x - recipeWidth + (i * 16) + 1 && pMouseX <= x - recipeWidth + (i * 16) + 17 && pMouseY >= y + (displayIndex * 18) + 31 && pMouseY <= y + (displayIndex * 18) + 31 + 16) {
                    pGuiGraphics.renderTooltip(this.font, itemStack, pMouseX, pMouseY);
                }
                i++;
            }

            visibleCount++;
        }
        pGuiGraphics.pose().popPose();

        if (selectedRecipe >= recipes.size()) {
            selectedRecipe = 0;
        }

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

            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().translate(0, 0, 390);
            pGuiGraphics.fill(x + 97, y + 16, x + 151, y + 70, 0xAA8B8B8B);
            pGuiGraphics.pose().popPose();
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
                pGuiGraphics.pose().pushPose();
                pGuiGraphics.pose().translate(centeredX, textY, 390);
                pGuiGraphics.drawString(font, formattedcharsequence, 0, 0, 0xAA0000, false);
                pGuiGraphics.pose().popPose();
                textY += 9;
            }
        }

        if (this.menu.player.hasPermissions(4) || this.menu.player.isCreative()){
            if (pMouseX >= leftPos + imageWidth - 16 && pMouseX <= leftPos + imageWidth - 4 && pMouseY >= topPos + 3 && pMouseY <= topPos + 15) {
                pGuiGraphics.renderTooltip(this.font, Component.translatable("screen.uncrafteverything.uncraft_everything_config"), pMouseX, pMouseY);
            }

            if (pMouseX >= leftPos + imageWidth - 30 && pMouseX <= leftPos + imageWidth - 18 && pMouseY >= topPos + 3 && pMouseY <= topPos + 15) {
                pGuiGraphics.renderTooltip(this.font, Component.translatable("screen.uncrafteverything.per_item_xp_config"), pMouseX, pMouseY);
            }
        }

        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollDelta) {
        if (scrollDelta == 1.0d && this.page > 0) {
            this.page--;
        } else if (scrollDelta == -1.0d && (this.page + 1) * MAX_PAGE_SIZE < recipes.size()) {
            this.page++;
        }
        else if (scrollDelta == 1.0d && this.page == 0 && !recipes.isEmpty()) {
            this.page = (int) Math.ceil((double) recipes.size() / MAX_PAGE_SIZE) - 1;
        } else if (scrollDelta == -1.0d && (this.page + 1) * MAX_PAGE_SIZE >= recipes.size()) {
            this.page = 0;
        }
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
                PacketDistributor.sendToServer(new UncraftingRecipeSelectionPayload(this.menu.blockEntity.getBlockPos(), recipe));
            }
        }
    }
}
