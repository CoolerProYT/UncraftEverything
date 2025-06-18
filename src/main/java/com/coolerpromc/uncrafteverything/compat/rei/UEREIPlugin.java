package com.coolerpromc.uncrafteverything.compat.rei;

import com.coolerpromc.uncrafteverything.UncraftEverythingClient;
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
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.trim.*;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        Ingredient shulkerBoxIngredient = Ingredient.fromTag(Registries.ITEM.getOrThrow(ConventionalItemTags.SHULKER_BOXES));
        shulkerBoxIngredient.getMatchingItems().forEach(itemStack -> {
            if (!itemStack.value().equals(Items.SHULKER_BOX)){
                entries.add(new JEIUncraftingTableRecipe(itemStack.value().getDefaultStack(), List.of(Ingredient.ofItems(Blocks.SHULKER_BOX), Ingredient.ofItems(DyeItem.byColor(Objects.requireNonNull(((ShulkerBoxBlock) ((BlockItem) itemStack.value()).getBlock()).getColor()))))));
            }
        });

        // Add Tipped Arrows
        BasicDisplay.registryAccess().getOptional(RegistryKeys.POTION).stream()
                .flatMap(Registry::streamEntries)
                .forEach(potion -> {
                    if (potion != Potions.WATER) {
                        ItemStack tippedArrow = PotionContentsComponent.createStack(Items.TIPPED_ARROW, potion);
                        List<ItemStack> output = new ArrayList<>();
                        output.add(Items.ARROW.getDefaultStack());
                        output.add(Items.ARROW.getDefaultStack());
                        output.add(Items.ARROW.getDefaultStack());
                        output.add(Items.ARROW.getDefaultStack());
                        output.add(PotionContentsComponent.createStack(Items.LINGERING_POTION, potion));
                        output.add(Items.ARROW.getDefaultStack());
                        output.add(Items.ARROW.getDefaultStack());
                        output.add(Items.ARROW.getDefaultStack());
                        output.add(Items.ARROW.getDefaultStack());

                        entries.add(new JEIUncraftingTableRecipe(tippedArrow, output, true));
                    }
                });

        // Add Enchanted Books
        BasicDisplay.registryAccess().getOptional(RegistryKeys.ENCHANTMENT).stream()
                .flatMap(Registry::streamEntries)
                .forEach(holder -> {
                    if (!holder.hasKeyAndValue()) return;
                    Enchantment enchantment = holder.value();

                    ItemStack diamondSword = new ItemStack(Items.DIAMOND_SWORD);
                    diamondSword.addEnchantment(holder, enchantment.getMaxLevel());

                    List<ItemStack> output = new ArrayList<>();
                    output.add(Items.DIAMOND_SWORD.getDefaultStack());
                    output.add(EnchantmentHelper.getEnchantedBookWith(new EnchantmentLevelEntry(holder, enchantment.getMaxLevel())));

                    entries.add(new JEIUncraftingTableRecipe(diamondSword, output, true));
                });

        // Add all items that can be uncrafted
        UncraftEverythingClient.recipesFromServer.forEach(recipeHolder -> {
            if (recipeHolder.value() instanceof ShapedRecipe shapedRecipe){
                entries.add(new JEIUncraftingTableRecipe(shapedRecipe.result, shapedRecipe.getIngredients().stream().map(ingredient -> ingredient.orElse(null)).toList()));
            }

            if (recipeHolder.value() instanceof ShapelessRecipe shapelessRecipe){
                entries.add(new JEIUncraftingTableRecipe(shapelessRecipe.result, shapelessRecipe.ingredients));
            }

            if (recipeHolder.value() instanceof SmithingTransformRecipe smithingTransformRecipe){
                DefaultedList<Ingredient> ingredients = DefaultedList.of();

                ingredients.add(smithingTransformRecipe.base().get());
                ingredients.add(smithingTransformRecipe.addition().get());
                ingredients.add(smithingTransformRecipe.template().get());
                entries.add(new JEIUncraftingTableRecipe(smithingTransformRecipe.result, ingredients));
            }

            if (recipeHolder.value() instanceof SmithingTrimRecipe smithingTrimRecipe){
                DynamicRegistryManager registryAccess = MinecraftClient.getInstance().world.getRegistryManager();
                List<Ingredient> output = new ArrayList<>();
                output.add(0, smithingTrimRecipe.base().get());
                output.add(1, smithingTrimRecipe.addition().get());
                output.add(smithingTrimRecipe.template().get());
                smithingTrimRecipe.base().get().getMatchingItems().forEach(itemStack -> {
                    output.set(0, Ingredient.ofItem(itemStack.value()));
                    smithingTrimRecipe.addition().get().getMatchingItems().forEach(itemStack1 -> {
                        output.set(1, Ingredient.ofItem(itemStack1.value()));
                        Optional<RegistryEntry.Reference<ArmorTrimMaterial>> trimMaterialReference = ArmorTrimMaterials.get(registryAccess, smithingTrimRecipe.addition().get().getMatchingItems().toList().get(0).value().getDefaultStack());
                        Optional<RegistryEntry.Reference<ArmorTrimPattern>> trimPatternReference = ArmorTrimPatterns.get(registryAccess, smithingTrimRecipe.template().get().getMatchingItems().toList().get(0).value().getDefaultStack());
                        if (trimPatternReference.isPresent() && trimMaterialReference.isPresent()){
                            ItemStack stack = itemStack.value().getDefaultStack();
                            stack.set(DataComponentTypes.TRIM, new ArmorTrim(trimMaterialReference.get(), trimPatternReference.get()));
                            entries.add(new JEIUncraftingTableRecipe(stack, output));
                        }
                    });
                });
            }
        });

        entries.forEach(jeiUncraftingTableRecipe -> {
            registry.add(new UncraftingRecipeDisplay(List.of(EntryIngredients.of(jeiUncraftingTableRecipe.getInput())), jeiUncraftingTableRecipe.getEntryIngredientOutput()));
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
