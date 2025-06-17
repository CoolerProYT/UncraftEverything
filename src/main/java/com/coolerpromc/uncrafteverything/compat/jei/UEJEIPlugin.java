package com.coolerpromc.uncrafteverything.compat.jei;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen;
import com.coolerpromc.uncrafteverything.util.JEIUncraftingTableRecipe;
import com.coolerpromc.uncrafteverything.util.UETags;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

@JeiPlugin
public class UEJEIPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(UncraftEverything.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new UncraftingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        List<JEIUncraftingTableRecipe> entries = new ArrayList<>();

        // Add Shulker Boxes (Prevent duplication, only normal shulker box will be outputted)
        Ingredient shulkerBoxIngredient = Ingredient.of(UETags.Items.SHULKER_BOXES);
        Arrays.stream(shulkerBoxIngredient.getItems()).forEach(itemStack -> {
            if (!itemStack.getItem().equals(Items.SHULKER_BOX)){
                ItemStack dyeStack = new ItemStack(DyeItem.byColor(Objects.requireNonNull(((ShulkerBoxBlock) ((BlockItem) itemStack.getItem()).getBlock()).getColor())));
                List<Ingredient> output = new ArrayList<>();
                output.add(Ingredient.of(Blocks.SHULKER_BOX));
                output.add(Ingredient.of(dyeStack));
                entries.add(new JEIUncraftingTableRecipe(itemStack, output));
            }
        });

        // Add Tipped Arrows
        ForgeRegistries.POTION_TYPES.getValues().forEach(potion -> {
            if (potion != Potions.EMPTY && potion != Potions.WATER) {
                ItemStack tippedArrow = PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), potion);
                List<Ingredient> output = new ArrayList<>();
                output.add(Ingredient.of(Items.ARROW));
                output.add(Ingredient.of(Items.ARROW));
                output.add(Ingredient.of(Items.ARROW));
                output.add(Ingredient.of(Items.ARROW));
                output.add(Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), potion)));
                output.add(Ingredient.of(Items.ARROW));
                output.add(Ingredient.of(Items.ARROW));
                output.add(Ingredient.of(Items.ARROW));
                output.add(Ingredient.of(Items.ARROW));

                entries.add(new JEIUncraftingTableRecipe(tippedArrow, output));
            }
        });

        // Add Enchanted Books
        ForgeRegistries.ENCHANTMENTS.getValues().forEach(enchantment -> {
            if (enchantment != null) {
                for (int i = 1; i <= enchantment.getMaxLevel(); i++) {
                    ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
                    ItemStack dirt = new ItemStack(Items.DIAMOND_SWORD);
                    EnchantedBookItem.addEnchantment(enchantedBook, new EnchantmentData(enchantment, i));
                    Map<Enchantment, Integer> enchantments = new HashMap<>();
                    enchantments.put(enchantment, i);
                    EnchantmentHelper.setEnchantments(enchantments, dirt);
                    List<Ingredient> output = new ArrayList<>();
                    output.add(Ingredient.of(new ItemStack(Items.DIAMOND_SWORD)));
                    output.add(Ingredient.of(enchantedBook));

                    entries.add(new JEIUncraftingTableRecipe(dirt, output));
                }
            }
        });

        // Add all items that can be uncrafted
        recipeManager.getRecipes().forEach(recipeHolder -> {
            if (recipeHolder instanceof ShapedRecipe){
                ShapedRecipe shapedRecipe = (ShapedRecipe) recipeHolder;
                entries.add(new JEIUncraftingTableRecipe(shapedRecipe.result, shapedRecipe.getIngredients()));
            }

            if (recipeHolder instanceof ShapelessRecipe){
                ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipeHolder;
                entries.add(new JEIUncraftingTableRecipe(shapelessRecipe.result, shapelessRecipe.getIngredients()));
            }

            if (recipeHolder instanceof SmithingRecipe){
                SmithingRecipe smithingTransformRecipe = (SmithingRecipe) recipeHolder;
                NonNullList<Ingredient> ingredients = NonNullList.create();

                ingredients.add(smithingTransformRecipe.base);
                ingredients.add(smithingTransformRecipe.addition);
                entries.add(new JEIUncraftingTableRecipe(smithingTransformRecipe.result, ingredients));
            }

            if (ModList.get().isLoaded("recipestages")) {
                try {
                    Class<?> shapedRecipeStageClass = Class.forName("com.blamejared.recipestages.recipes.ShapedRecipeStage");
                    Class<?> recipeStageClass = Class.forName("com.blamejared.recipestages.recipes.RecipeStage");

                    if (shapedRecipeStageClass.isInstance(recipeHolder)) {
                        Object innerRecipe = shapedRecipeStageClass.getMethod("getRecipe").invoke(recipeHolder);
                        if (innerRecipe instanceof ShapedRecipe) {
                            ShapedRecipe shapedRecipe = (ShapedRecipe) innerRecipe;
                            entries.add(new JEIUncraftingTableRecipe(shapedRecipe.result, shapedRecipe.getIngredients()));
                        }
                    }

                    if (recipeStageClass.isInstance(recipeHolder)) {
                        Object innerRecipe = recipeStageClass.getMethod("getRecipe").invoke(recipeHolder);
                        if (innerRecipe instanceof ShapelessRecipe) {
                            ShapelessRecipe shapelessRecipe = (ShapelessRecipe) innerRecipe;
                            entries.add(new JEIUncraftingTableRecipe(shapelessRecipe.result, shapelessRecipe.getIngredients()));
                        }
                    }
                } catch (Exception e) {
                    System.out.println("[UncraftEverything] Failed to get recipe from RecipeStages: " + e.getMessage());
                }
            }
        });
        registration.addRecipes(entries, UncraftingRecipeCategory.UID);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(UncraftingTableScreen.class, 59, 35, 22, 15, UncraftingRecipeCategory.UID);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(UEBlocks.UNCRAFTING_TABLE.get()), UncraftingRecipeCategory.UID);
    }
}
