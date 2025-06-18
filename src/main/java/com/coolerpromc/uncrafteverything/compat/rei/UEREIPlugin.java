package com.coolerpromc.uncrafteverything.compat.rei;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen;
import com.coolerpromc.uncrafteverything.util.JEIUncraftingTableRecipe;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.*;
import net.minecraft.item.trim.*;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.collection.DefaultedList;

import java.util.*;

public class UEREIPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new UncraftingRecipeCategory(), configuration -> configuration.addWorkstations(EntryStacks.of(UEBlocks.UNCRAFTING_TABLE)));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        RecipeManager recipeManager = MinecraftClient.getInstance().world.getRecipeManager();
        List<JEIUncraftingTableRecipe> entries = new ArrayList<>();

        // Add Shulker Boxes (Prevent duplication, only normal shulker box will be outputted)
        Ingredient shulkerBoxIngredient = Ingredient.fromTag(ConventionalItemTags.SHULKER_BOXES);
        Arrays.stream(shulkerBoxIngredient.getMatchingStacks()).forEach(itemStack -> {
            if (!itemStack.getItem().equals(Items.SHULKER_BOX)){
                entries.add(new JEIUncraftingTableRecipe(itemStack, List.of(Ingredient.ofItems(Blocks.SHULKER_BOX), Ingredient.ofItems(DyeItem.byColor(Objects.requireNonNull(((ShulkerBoxBlock) ((BlockItem) itemStack.getItem()).getBlock()).getColor()))))));
            }
        });

        // Add Tipped Arrows
        BasicDisplay.registryAccess().getOptional(RegistryKeys.POTION).stream()
                .flatMap(Registry::streamEntries)
                .forEach(potion -> {
                    if (potion != Potions.WATER) {
                        ItemStack tippedArrow = PotionContentsComponent.createStack(Items.TIPPED_ARROW, potion);
                        List<Ingredient> output = new ArrayList<>();
                        output.add(Ingredient.ofItems(Items.ARROW));
                        output.add(Ingredient.ofItems(Items.ARROW));
                        output.add(Ingredient.ofItems(Items.ARROW));
                        output.add(Ingredient.ofItems(Items.ARROW));
                        output.add(Ingredient.ofStacks(PotionContentsComponent.createStack(Items.LINGERING_POTION, potion)));
                        output.add(Ingredient.ofItems(Items.ARROW));
                        output.add(Ingredient.ofItems(Items.ARROW));
                        output.add(Ingredient.ofItems(Items.ARROW));
                        output.add(Ingredient.ofItems(Items.ARROW));

                        entries.add(new JEIUncraftingTableRecipe(tippedArrow, output));
                    }
                });

        // Add Enchanted Books
        BasicDisplay.registryAccess().getOptional(RegistryKeys.ENCHANTMENT).stream()
                .flatMap(Registry::streamEntries)
                .forEach(enchantment -> {
                    if (enchantment != null) {
                        ItemStack enchantedBook = EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(enchantment, enchantment.value().getMaxLevel()));
                        ItemStack dirt = new ItemStack(Items.DIAMOND_SWORD);
                        dirt.addEnchantment(enchantment, enchantment.value().getMaxLevel());
                        List<Ingredient> output = new ArrayList<>();
                        output.add(Ingredient.ofStacks(new ItemStack(Items.DIAMOND_SWORD)));
                        output.add(Ingredient.ofStacks(enchantedBook));

                        entries.add(new JEIUncraftingTableRecipe(dirt, output));
                    }
                });

        // Add all items that can be uncrafted
        recipeManager.values().forEach(recipeHolder -> {
            if (recipeHolder.value() instanceof ShapedRecipe shapedRecipe){
                entries.add(new JEIUncraftingTableRecipe(shapedRecipe.result, shapedRecipe.getIngredients()));
            }

            if (recipeHolder.value() instanceof ShapelessRecipe shapelessRecipe){
                entries.add(new JEIUncraftingTableRecipe(shapelessRecipe.result, shapelessRecipe.getIngredients()));
            }

            if (recipeHolder.value() instanceof SmithingTransformRecipe smithingTransformRecipe){
                DefaultedList<Ingredient> ingredients = DefaultedList.of();

                ingredients.add(smithingTransformRecipe.base);
                ingredients.add(smithingTransformRecipe.addition);
                ingredients.add(smithingTransformRecipe.template);
                entries.add(new JEIUncraftingTableRecipe(smithingTransformRecipe.result, ingredients));
            }

            if (recipeHolder.value() instanceof SmithingTrimRecipe smithingTrimRecipe){
                DynamicRegistryManager registryAccess = MinecraftClient.getInstance().world.getRegistryManager();
                List<Ingredient> output = new ArrayList<>();
                output.add(0, smithingTrimRecipe.base);
                output.add(1, smithingTrimRecipe.addition);
                output.add(smithingTrimRecipe.template);
                Arrays.stream(smithingTrimRecipe.base.getMatchingStacks()).forEach(itemStack -> {
                    output.set(0, Ingredient.ofStacks(itemStack));
                    Arrays.stream(smithingTrimRecipe.addition.getMatchingStacks()).forEach(itemStack1 -> {
                        output.set(1, Ingredient.ofStacks(itemStack1));
                        Optional<RegistryEntry.Reference<ArmorTrimMaterial>> trimMaterialReference = ArmorTrimMaterials.get(registryAccess, smithingTrimRecipe.addition.getMatchingStacks()[0]);
                        Optional<RegistryEntry.Reference<ArmorTrimPattern>> trimPatternReference = ArmorTrimPatterns.get(registryAccess, smithingTrimRecipe.template.getMatchingStacks()[0]);
                        if (trimPatternReference.isPresent() && trimMaterialReference.isPresent()){
                            itemStack.set(DataComponentTypes.TRIM, new ArmorTrim(trimMaterialReference.get(), trimPatternReference.get()));
                            entries.add(new JEIUncraftingTableRecipe(itemStack, output));
                        }
                    });
                });
            }
        });

        entries.forEach(jeiUncraftingTableRecipe -> {
            registry.add(new UncraftingRecipeDisplay(List.of(EntryIngredients.of(jeiUncraftingTableRecipe.getInput())), EntryIngredients.ofIngredients(jeiUncraftingTableRecipe.getOutputs())));
        });
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerClickArea(screen -> new Rectangle(((screen.width - 176) / 2) + 59, ((screen.height - 166) / 2) + 27, 22, 15), UncraftingTableScreen.class, UncraftingRecipeDisplay.CATEGORY_IDENTIFIER);
    }

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(UncraftingTableScreen.class, screen -> List.of(
            new Rectangle(0, 0, screen.getX(), screen.height)
        ));
    }
}
