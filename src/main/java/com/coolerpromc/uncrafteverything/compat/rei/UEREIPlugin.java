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
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.armortrim.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.neoforged.neoforge.common.Tags;

import java.util.*;

@REIPluginClient
public class UEREIPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new UncraftingRecipeCategory(), configuration -> configuration.addWorkstations(EntryStacks.of(UEBlocks.UNCRAFTING_TABLE.get())));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        List<JEIUncraftingTableRecipe> entries = new ArrayList<>();

        // Add Shulker Boxes (Prevent duplication, only normal shulker box will be outputted)
        Ingredient shulkerBoxIngredient = Ingredient.of(Tags.Items.SHULKER_BOXES);
        Arrays.stream(shulkerBoxIngredient.getItems()).forEach(itemStack -> {
            if (!itemStack.is(Items.SHULKER_BOX)){
                ItemStack dyeStack = new ItemStack(DyeItem.byColor(Objects.requireNonNull(((ShulkerBoxBlock) ((BlockItem) itemStack.getItem()).getBlock()).getColor())));
                entries.add(new JEIUncraftingTableRecipe(itemStack, List.of(Ingredient.of(Blocks.SHULKER_BOX), Ingredient.of(dyeStack))));
            }
        });

        // Add Tipped Arrows
        BuiltInRegistries.POTION.stream().forEach(potion -> {
            if (potion != Potions.WATER) {
                ItemStack tippedArrow = PotionContents.createItemStack(Items.TIPPED_ARROW, new Holder.Direct<>(potion));
                List<Ingredient> output = new ArrayList<>();
                output.add(Ingredient.of(Items.ARROW));
                output.add(Ingredient.of(Items.ARROW));
                output.add(Ingredient.of(Items.ARROW));
                output.add(Ingredient.of(Items.ARROW));
                output.add(Ingredient.of(PotionContents.createItemStack(Items.LINGERING_POTION, new Holder.Direct<>(potion))));
                output.add(Ingredient.of(Items.ARROW));
                output.add(Ingredient.of(Items.ARROW));
                output.add(Ingredient.of(Items.ARROW));
                output.add(Ingredient.of(Items.ARROW));

                entries.add(new JEIUncraftingTableRecipe(tippedArrow, output));
            }
        });

        // Add Enchanted Books
        Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).holders().forEach(enchantment -> {
            if (enchantment != null) {
                ItemStack enchantedBook = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, enchantment.value().getMaxLevel()));
                ItemStack dirt = new ItemStack(Items.DIAMOND_SWORD);
                dirt.enchant(enchantment, enchantment.value().getMaxLevel());
                List<Ingredient> output = new ArrayList<>();
                output.add(Ingredient.of(new ItemStack(Items.DIAMOND_SWORD)));
                output.add(Ingredient.of(enchantedBook));

                entries.add(new JEIUncraftingTableRecipe(dirt, output));
            }
        });

        // Add all items that can be uncrafted
        recipeManager.getRecipes().forEach(recipeHolder -> {
            if (recipeHolder.value() instanceof ShapedRecipe shapedRecipe){
                entries.add(new JEIUncraftingTableRecipe(shapedRecipe.result, shapedRecipe.getIngredients()));
            }

            if (recipeHolder.value() instanceof ShapelessRecipe shapelessRecipe){
                entries.add(new JEIUncraftingTableRecipe(shapelessRecipe.result, shapelessRecipe.getIngredients()));
            }

            if (recipeHolder.value() instanceof SmithingTransformRecipe smithingTransformRecipe){
                NonNullList<Ingredient> ingredients = NonNullList.create();

                ingredients.add(smithingTransformRecipe.base);
                ingredients.add(smithingTransformRecipe.addition);
                ingredients.add(smithingTransformRecipe.template);
                entries.add(new JEIUncraftingTableRecipe(smithingTransformRecipe.result, ingredients));
            }

            if (recipeHolder.value() instanceof SmithingTrimRecipe smithingTrimRecipe){
                RegistryAccess registryAccess = Minecraft.getInstance().level.registryAccess();
                List<Ingredient> output = new ArrayList<>();
                output.add(0, smithingTrimRecipe.base);
                output.add(1, smithingTrimRecipe.addition);
                output.add(smithingTrimRecipe.template);
                Arrays.stream(smithingTrimRecipe.base.getItems()).forEach(itemStack -> {
                    output.set(0, Ingredient.of(itemStack));
                    Arrays.stream(smithingTrimRecipe.addition.getItems()).forEach(itemStack1 -> {
                        output.set(1, Ingredient.of(itemStack1));
                        Optional<Holder.Reference<TrimMaterial>> trimMaterialReference = TrimMaterials.getFromIngredient(registryAccess, smithingTrimRecipe.addition.getItems()[0]);
                        Optional<Holder.Reference<TrimPattern>> trimPatternReference = TrimPatterns.getFromTemplate(registryAccess, smithingTrimRecipe.template.getItems()[0]);
                        if (trimPatternReference.isPresent() && trimMaterialReference.isPresent()){
                            itemStack.set(DataComponents.TRIM, new ArmorTrim(trimMaterialReference.get(), trimPatternReference.get()));
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
            new Rectangle(0, 0, screen.getGuiLeft(), screen.height)
        ));
    }
}
