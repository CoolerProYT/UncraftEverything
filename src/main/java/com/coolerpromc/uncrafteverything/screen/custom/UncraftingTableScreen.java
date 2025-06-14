package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableCraftButtonClickPayload;
import com.coolerpromc.uncrafteverything.screen.widget.RecipeSelectionButton;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.TranslatableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("DataFlowIssue")
public class UncraftingTableScreen extends HandledScreen<UncraftingTableMenu> {
    private static final Identifier TEXTURE = new Identifier(UncraftEverything.MODID, "textures/gui/uncrafting_table_gui.png");
    private static final Identifier RECIPE_PANEL_TEXTURE = new Identifier(UncraftEverything.MODID, "textures/gui/recipe_selection_panel.png");
    private List<UncraftingTableRecipe> recipes = new ArrayList<>();
    private int selectedRecipe = 0;

    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_PADDING = 2;
    private int page = 0;
    private final int MAX_PAGE_SIZE = 7;

    private ButtonWidget configButton;
    private ButtonWidget expConfigButton;

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

        this.addButton(new ButtonWidget(buttonX, buttonY, 64, 20, new TranslatableText("screen.uncrafteverything.uncraft"), this::onPressed));

        if (this.handler.player.isCreative() || this.handler.player.hasPermissionLevel(4)) {
            configButton = new ButtonWidget(this.x + backgroundWidth - 16, this.y + 3, 12, 12, new TranslatableText("screen.uncrafteverything.blank"), this::openConfigScreen);
            this.addChild(configButton);
            expConfigButton = new ButtonWidget(this.x + backgroundWidth - 30, this.y + 3, 12, 12, new TranslatableText("screen.uncrafteverything.blank"), this::openExpScreen);
            this.addChild(expConfigButton);
        }
    }

    private void onPressed(ButtonWidget button) {
        PacketByteBuf packetByteBuf = PacketByteBufs.create();
        try{
            packetByteBuf.encode(UncraftingTableCraftButtonClickPayload.CODEC, new UncraftingTableCraftButtonClickPayload(this.handler.blockEntity.getPos(), hasShiftDown()));
        } catch (IOException e) {
            System.out.println("Failed to encode UncraftingTableCraftButtonClickPayload: " + e.getMessage());
        }
        ClientPlayNetworking.send(UncraftingTableCraftButtonClickPayload.ID, packetByteBuf);
    }

    private void openConfigScreen(ButtonWidget button){
        this.client.openScreen(new UEConfigScreen(new TranslatableText("screen.uncrafteverything.uncraft_everything_config"), this));
    }

    private void openExpScreen(ButtonWidget button){
        this.client.openScreen(new PerItemExpConfigScreen(this));
    }

    @Override
    protected void drawBackground(MatrixStack context, float delta, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        MinecraftClient.getInstance().getTextureManager().bindTexture(TEXTURE);
        drawTexture(context, x, y, 0, 0, backgroundWidth, backgroundHeight);

        configButton.render(context, mouseX, mouseY, delta);
        expConfigButton.render(context, mouseX, mouseY, delta);

        fill(context, x + backgroundWidth - 15, y + 3 + 11, x + backgroundWidth - 17 + 12, y + 3 + 12, configButton.isHovered() || configButton.isFocused() ? 0xFFFFFFFF : 0xFF000000);
        fill(context, x + backgroundWidth - 29, y + 3 + 11, x + backgroundWidth - 31 + 12, y + 3 + 12, expConfigButton.isHovered() || expConfigButton.isFocused() ? 0xFFFFFFFF : 0xFF000000);

        this.client.getTextureManager().bindTexture(new Identifier(UncraftEverything.MODID, "textures/gui/sprites/config.png"));
        context.push();
        context.translate(x + backgroundWidth - 16 + 2, y + 5, 400);
        drawTexture(context, 0, 0, 0, 0,8, 8, 8, 8);
        context.pop();

        this.client.getTextureManager().bindTexture(new Identifier(UncraftEverything.MODID, "textures/gui/sprites/exp.png"));
        context.push();
        context.translate(x + backgroundWidth - 30 + 2, y + 5, 400);
        drawTexture(context, 0, 0, 0, 0,8, 8, 8, 8);
        context.pop();
    }

    @Override
    public void render(MatrixStack context, int mouseX, int mouseY, float delta) {
        renderBackground(context);

        this.children.clear();
        this.init();

        Text exp = new TranslatableText("screen.uncrafteverything.exp_" + this.handler.getExpType().toLowerCase() + "_required",this.handler.getExpAmount());
        int expX = x + (backgroundWidth - 64) - 20 + 16;

        context.push();
        context.scale(0.75f, 0.75f, 0.75f);
        context.translate(expX * 1.3334, this.y * 1.3334 + 124, 0);
        drawStringWithShadow(context, this.textRenderer, exp.getString(), 0, 0, 0xFF00AA00);
        context.pop();

        int maxPageCount = (int) Math.ceil((double) recipes.size() / MAX_PAGE_SIZE);
        int pageToDisplay = recipes.isEmpty() ? 0 : page + 1;

        if (page > maxPageCount - 1) {
            page = 0;
        }

        MinecraftClient.getInstance().getTextureManager().bindTexture(RECIPE_PANEL_TEXTURE);
        drawTexture(context, x - 152, y, 0, 0, 152, 184, 152, 184);
        this.drawCenteredWordWrapWithoutShadow(context, textRenderer, new TranslatableText("screen.uncrafteverything.uncraft_recipe_selection"), x - 75, y + 7, 4210752);
        this.drawCenteredWordWrapWithoutShadow(context, textRenderer, new TranslatableText("screen.uncrafteverything.page", pageToDisplay, maxPageCount), x - 75, y + backgroundHeight - 18, 4210752);

        ButtonWidget prevButton = new ButtonWidget(x - 152 + 5, y + backgroundHeight - 23, 16, 16, new TranslatableText("screen.uncrafteverything.prev_button"), button -> {
            if (this.page > 0) {
                this.page--;
            }
            else{
                this.page = Math.max(maxPageCount - 1, 0);
            }
        });
        this.addChild(prevButton).render(context, mouseX, mouseY, delta);
        fill(context, x - 152 + 5, y + backgroundHeight - 23 + 15, x - 152 + 5 + 16, y + backgroundHeight - 23 + 16, prevButton.isHovered() || prevButton.isFocused() ? 0xFFFFFFFF : 0xFF000000);

        ButtonWidget nextButton = new ButtonWidget(x - 21, y + backgroundHeight - 23, 16, 16, new TranslatableText("screen.uncrafteverything.next_button"), button -> {
            if (this.page < maxPageCount - 1) {
                this.page++;
            }
            else{
                this.page = 0;
            }
        });
        this.addChild(nextButton).render(context, mouseX, mouseY, delta);
        fill(context, x - 21, y + backgroundHeight - 23 + 15, x - 21 + 16, y + backgroundHeight - 23 + 16, nextButton.isHovered() || nextButton.isFocused() ? 0xFFFFFFFF : 0xFF000000);

        int visibleCount = 0;
        for (int j = page * MAX_PAGE_SIZE; j < recipes.size() && visibleCount < MAX_PAGE_SIZE; j++) {
            UncraftingTableRecipe recipe = recipes.get(j);
            int displayIndex = visibleCount;

            int recipeWidth = 9 * 16 + 5;
            Rectangle2D bounds = new Rectangle2D.Double(x - recipeWidth, y + (displayIndex * 18) + 30, recipeWidth - 3, 18);

            int finalJ = j;
            RecipeSelectionButton button = new RecipeSelectionButton((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight(), new TranslatableText("screen.uncrafteverything.blank"), ignored -> selectedRecipe = finalJ);
            if (selectedRecipe == j) {
                button.setFocused(true);
            }
            this.addChild(button).render(context, mouseX, mouseY, delta);
            fill(context, (int) bounds.getX() + 1, (int) (bounds.getY() + bounds.getHeight() - 1), (int) (bounds.getX() + bounds.getWidth()), (int) (bounds.getY() + bounds.getHeight()),0xFFFFFFFF);
            fill(context, (int) bounds.getX() + 1, (int) (bounds.getY() + bounds.getHeight() - 1), (int) (bounds.getX()), (int) (bounds.getY() + bounds.getHeight()),0xFF8B8B8B);

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
                itemRenderer.renderInGui(itemStack, x - recipeWidth + (i * 16) + 1, y + (displayIndex * 18) + 31);
                itemRenderer.renderGuiItemOverlay(this.textRenderer, itemStack, x - recipeWidth + (i * 16) + 1, y + (displayIndex * 18) + 31);
                if (mouseX >= x - recipeWidth + (i * 16) + 1 && mouseX <= x - recipeWidth + (i * 16) + 17 && mouseY >= y + (displayIndex * 18) + 31 && mouseY <= y + (displayIndex * 18) + 31 + 16) {
                    renderTooltip(context, itemStack, mouseX, mouseY);
                }
                i++;
            }

            visibleCount++;
        }

        if (selectedRecipe >= recipes.size()) {
            selectedRecipe = 0;
        }

        if (!recipes.isEmpty()) {
            PacketByteBuf packetByteBuf = PacketByteBufs.create();
            try{
                packetByteBuf.encode(UncraftingRecipeSelectionPayload.CODEC, new UncraftingRecipeSelectionPayload(this.handler.blockEntity.getPos(), this.recipes.get(selectedRecipe)));
            } catch (IOException e) {
                System.out.println("Failed to encode UncraftingRecipeSelectionPayload: " + e.getMessage());
            }
            ClientPlayNetworking.send(UncraftingRecipeSelectionPayload.ID, packetByteBuf);

            List<ItemStack> outputs = this.recipes.get(selectedRecipe).getOutputs();
            for (int i = 0; i < outputs.size(); i++) {
                ItemStack itemStack = outputs.get(i);
                itemRenderer.renderInGui(
                        itemStack,
                        x + 98 + 18 * (i % 3),
                        y + 17 + (i / 3) * 18);
                RenderSystem.disableDepthTest();
                fill(
                        context,
                        x + 98 + 18 * (i % 3),
                        y + 17 + (i / 3) * 18,
                        x + 98 + 18 * (i % 3) + 16,
                        y + 17 + (i / 3) * 18 + 16,
                        0xAA8B8B8B);
                RenderSystem.enableDepthTest();
            }
        }

        super.render(context, mouseX, mouseY, delta);

        int status = this.handler.getStatus();

        if (status != -1){
            TranslatableText statusText;
            switch (status){
                case 0 : statusText = new TranslatableText("screen.uncrafteverything.no_recipe_found"); break;
                case 1 : statusText = new TranslatableText("screen.uncrafteverything.no_suitable_output_slot"); break;
                case 2 : statusText = new TranslatableText("screen.uncrafteverything.not_enough_exp"); break;
                case 3 : statusText = new TranslatableText("screen.uncrafteverything.not_enough_input"); break;
                case 4 : statusText = new TranslatableText("screen.uncrafteverything.not_empty_shulker"); break;
                case 5 : statusText = new TranslatableText("screen.uncrafteverything.restricted_by_config"); break;
                case 6 : statusText = new TranslatableText("screen.uncrafteverything.damaged_item"); break;
                case 7 : statusText = new TranslatableText("Enchanted Item Not Allowed!"); break;
                default : statusText = new TranslatableText("screen.uncrafteverything.blank");
            }

            int textY = y;

            context.push();
            context.translate(0,0,400);
            fill(context, x + 97, y + 16, x + 151, y + 70, 0xAA8B8B8B);
            context.pop();
            List<OrderedText> formattedText = textRenderer.wrapLines(StringVisitable.plain(statusText.getString()), 54);

            switch (formattedText.size()){
                case 1 : textY += 38; break;
                case 2 : textY += 34; break;
                case 3 : textY += 30; break;
                case 4 : textY += 23; break;
                default : textY += 27;
            }

            for (OrderedText formattedcharsequence : formattedText) {
                int textWidth = textRenderer.getWidth(formattedcharsequence);
                int centeredX = x + 97 + (54 - textWidth) / 2;
                context.push();
                context.translate(centeredX,textY,400);
                textRenderer.draw(context, formattedcharsequence, 0, 0, 0xFFAA0000);
                context.pop();
                textY += 9;
            }
        }

        if (this.handler.player.hasPermissionLevel(4) || this.handler.player.isCreative()){
            if (mouseX >= x + backgroundWidth - 16 && mouseX <= x + backgroundWidth - 4 && mouseY >= y + 3 && mouseY <= y + 15) {
                renderTooltip(context, new TranslatableText("screen.uncrafteverything.uncraft_everything_config"), mouseX, mouseY);
            }

            if (mouseX >= x + backgroundWidth - 30 && mouseX <= x + backgroundWidth - 18 && mouseY >= y + 3 && mouseY <= y + 15) {
                renderTooltip(context, new TranslatableText("screen.uncrafteverything.per_item_xp_config"), mouseX, mouseY);
            }
        }

        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (amount == 1.0d && this.page > 0) {
            this.page--;
        } else if (amount == -1.0d && (this.page + 1) * MAX_PAGE_SIZE < recipes.size()) {
            this.page++;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    public void drawCenteredWordWrapWithoutShadow(MatrixStack context, TextRenderer textRenderer, Text text, int centerX, int y, int color) {
        List<OrderedText> lines = textRenderer.wrapLines(text, 140);

        int lineHeight = textRenderer.fontHeight + 2;

        for (int i = 0; i < lines.size(); i++) {
            OrderedText line = lines.get(i);
            int lineWidth = textRenderer.getWidth(line);
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
                PacketByteBuf packetByteBuf = PacketByteBufs.create();
                try{
                    packetByteBuf.encode(UncraftingRecipeSelectionPayload.CODEC, new UncraftingRecipeSelectionPayload(this.handler.blockEntity.getPos(), recipe));
                } catch (IOException e) {
                    System.out.println("Failed to encode UncraftingRecipeSelectionPayload: " + e.getMessage());
                }
                ClientPlayNetworking.send(UncraftingRecipeSelectionPayload.ID, packetByteBuf);
            }
        }
    }
}
