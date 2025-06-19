package com.coolerpromc.uncrafteverything.compat.rei;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen;
import com.coolerpromc.uncrafteverything.util.JEIUncraftingTableRecipe;
import com.coolerpromc.uncrafteverything.util.UETags;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.*;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

import java.util.*;

public class UEREIPlugin implements REIPluginV0 {
    @Override
    public void registerPluginCategories(RecipeHelper recipeHelper) {
        recipeHelper.registerCategories(new UncraftingRecipeCategory());
    }

    @Override
    public void registerOthers(RecipeHelper recipeHelper) {
        recipeHelper.registerClickArea(screen -> new Rectangle(((screen.width - 176) / 2) + 59, ((screen.height - 166) / 2) + 27, 22, 15), UncraftingTableScreen.class, UncraftingRecipeCategory.ID);
        recipeHelper.registerWorkingStations(UncraftingRecipeCategory.ID, EntryStack.create(UEBlocks.UNCRAFTING_TABLE));
    }

    @Override
    public void registerBounds(DisplayHelper displayHelper) {
        BaseBoundsHandler baseBoundsHandler = BaseBoundsHandler.getInstance();
        baseBoundsHandler.registerExclusionZones(UncraftingTableScreen.class, () -> {
            UncraftingTableScreen screen = (UncraftingTableScreen) REIHelper.getInstance().getPreviousContainerScreen();
            return new ArrayList<>(Collections.singleton(new Rectangle(0, 0, screen.getX(), screen.height)));
        });
    }

    @Override
    public void registerRecipeDisplays(RecipeHelper recipeHelper) {
        RecipeManager recipeManager = MinecraftClient.getInstance().getNetworkHandler().getRecipeManager();
        List<JEIUncraftingTableRecipe> entries = new ArrayList<>();

        // Add Shulker Boxes (Prevent duplication, only normal shulker box will be outputted)
        Ingredient shulkerBoxIngredient = Ingredient.fromTag(UETags.Items.SHULKER_BOXES);
        Arrays.stream(shulkerBoxIngredient.getMatchingStacksClient()).forEach(itemStack -> {
            if (!itemStack.getItem().equals(Items.SHULKER_BOX)){
                List<Ingredient> output = new ArrayList<>();
                output.add(Ingredient.ofItems(Blocks.SHULKER_BOX));
                output.add(Ingredient.ofItems(DyeItem.byColor(Objects.requireNonNull(((ShulkerBoxBlock) ((BlockItem) itemStack.getItem()).getBlock()).getColor()))));
                entries.add(new JEIUncraftingTableRecipe(itemStack, output));
            }
        });

        // Add Tipped Arrows
        Registry.POTION.stream()
                .forEach(potion -> {
                    if (potion != Potions.WATER) {
                        ItemStack tippedArrow = PotionUtil.setPotion(Items.TIPPED_ARROW.getDefaultStack(), potion);
                        List<Ingredient> output = new ArrayList<>();
                        output.add(Ingredient.ofItems(Items.ARROW));
                        output.add(Ingredient.ofItems(Items.ARROW));
                        output.add(Ingredient.ofItems(Items.ARROW));
                        output.add(Ingredient.ofItems(Items.ARROW));
                        output.add(Ingredient.ofStacks(PotionUtil.setPotion(new ItemStack(Items.LINGERING_POTION), potion)));
                        output.add(Ingredient.ofItems(Items.ARROW));
                        output.add(Ingredient.ofItems(Items.ARROW));
                        output.add(Ingredient.ofItems(Items.ARROW));
                        output.add(Ingredient.ofItems(Items.ARROW));

                        entries.add(new JEIUncraftingTableRecipe(tippedArrow, output));
                    }
                });

        // Add Enchanted BooksAdd commentMore actions
        Registry.ENCHANTMENT.stream()
                .forEach(enchantment -> {
                    if (enchantment != null) {
                        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
                        ItemStack dirt = new ItemStack(Items.DIAMOND_SWORD);
                        EnchantedBookItem.addEnchantment(enchantedBook, new EnchantmentLevelEntry(enchantment, enchantment.getMaxLevel()));
                        Map<Enchantment, Integer> enchantments = new HashMap<>();
                        enchantments.put(enchantment, enchantment.getMaxLevel());
                        EnchantmentHelper.set(enchantments, dirt);
                        List<Ingredient> output = new ArrayList<>();
                        output.add(Ingredient.ofStacks(new ItemStack(Items.DIAMOND_SWORD)));
                        output.add(Ingredient.ofStacks(enchantedBook));

                        entries.add(new JEIUncraftingTableRecipe(dirt, output));
                    }
                });

        // Add all items that can be uncrafted
        recipeManager.values().forEach(recipeHolder -> {
            if (recipeHolder instanceof ShapedRecipe){
                ShapedRecipe shapedRecipe = (ShapedRecipe) recipeHolder;
                entries.add(new JEIUncraftingTableRecipe(shapedRecipe.output, shapedRecipe.getPreviewInputs()));
            }

            if (recipeHolder instanceof ShapelessRecipe){
                ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipeHolder;
                entries.add(new JEIUncraftingTableRecipe(shapelessRecipe.output, shapelessRecipe.getPreviewInputs()));
            }

            if (recipeHolder instanceof SmithingRecipe){
                SmithingRecipe smithingTransformRecipe = (SmithingRecipe) recipeHolder;
                DefaultedList<Ingredient> ingredients = DefaultedList.of();

                ingredients.add(smithingTransformRecipe.base);
                ingredients.add(smithingTransformRecipe.addition);
                entries.add(new JEIUncraftingTableRecipe(smithingTransformRecipe.getOutput(), ingredients));
            }
        });

        entries.forEach(jeiUncraftingTableRecipe -> {
            recipeHelper.registerDisplay(new UncraftingRecipeDisplay(new ArrayList<>(Collections.singleton(EntryStack.create(jeiUncraftingTableRecipe.getInput()))), EntryStack.ofIngredients(jeiUncraftingTableRecipe.getOutputs())));
        });
    }

    @Override
    public Identifier getPluginIdentifier() {
        return new Identifier(UncraftEverything.MODID, "rei_plugin");
    }
}
