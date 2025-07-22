package com.coolerpromc.uncrafteverything.compat.jei;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen;
import com.coolerpromc.uncrafteverything.util.JEIUncraftingTableRecipe;
import com.coolerpromc.uncrafteverything.util.UETags;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings({"removal", "deprecation"})
@JeiPlugin
public class UEJEIPlugin implements IModPlugin {
    public static final RecipeType<JEIUncraftingTableRecipe> UNCRAFTING_TYPE = RecipeType.create(UncraftEverything.MODID, "uncrafting_table", JEIUncraftingTableRecipe.class);

    @Override
    public @NotNull ResourceLocation getPluginUid() {
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
            if (!itemStack.is(Items.SHULKER_BOX)){
                ItemStack dyeStack = new ItemStack(DyeItem.byColor(Objects.requireNonNull(((ShulkerBoxBlock) ((BlockItem) itemStack.getItem()).getBlock()).getColor())));
                entries.add(new JEIUncraftingTableRecipe(itemStack, List.of(Ingredient.of(Blocks.SHULKER_BOX), Ingredient.of(dyeStack))));
            }
        });

        // Add Tipped Arrows
        Registry.POTION.stream().forEach(potion -> {
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
        Registry.ENCHANTMENT.stream().forEach(enchantment -> {
            if (enchantment != null) {
                ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
                ItemStack dirt = new ItemStack(Items.DIAMOND_SWORD);
                EnchantedBookItem.addEnchantment(enchantedBook, new EnchantmentInstance(enchantment, enchantment.getMaxLevel()));
                EnchantmentHelper.setEnchantments(Map.of(enchantment, enchantment.getMaxLevel()), dirt);
                List<Ingredient> output = new ArrayList<>();
                output.add(Ingredient.of(new ItemStack(Items.DIAMOND_SWORD)));
                output.add(Ingredient.of(enchantedBook));

                entries.add(new JEIUncraftingTableRecipe(dirt, output));
            }
        });

        // Add all items that can be uncrafted
        recipeManager.getRecipes().forEach(recipeHolder -> {
            if (recipeHolder instanceof ShapedRecipe shapedRecipe){
                boolean isVanillaInput = Registry.ITEM.getKey(shapedRecipe.result.getItem()).getNamespace().equals("minecraft");

                if (!isVanillaInput || !UncraftEverythingConfig.CONFIG.preventModdedIngredientRecipes() || UncraftingTableBlockEntity.isVanillaIngredientRecipe(shapedRecipe)) {
                    entries.add(new JEIUncraftingTableRecipe(shapedRecipe.result, shapedRecipe.getIngredients()));
                }
            }

            if (recipeHolder instanceof ShapelessRecipe shapelessRecipe){
                boolean isVanillaInput = Registry.ITEM.getKey(shapelessRecipe.result.getItem()).getNamespace().equals("minecraft");

                if (!isVanillaInput || !UncraftEverythingConfig.CONFIG.preventModdedIngredientRecipes() || UncraftingTableBlockEntity.isVanillaIngredientRecipe(shapelessRecipe)) {
                    entries.add(new JEIUncraftingTableRecipe(shapelessRecipe.result, shapelessRecipe.ingredients));
                }
            }

            if (recipeHolder instanceof UpgradeRecipe smithingTransformRecipe){
                boolean isVanillaInput = Registry.ITEM.getKey(smithingTransformRecipe.result.getItem()).getNamespace().equals("minecraft");

                if (!isVanillaInput || !UncraftEverythingConfig.CONFIG.preventModdedIngredientRecipes() || UncraftingTableBlockEntity.isVanillaIngredientRecipe(smithingTransformRecipe)) {
                    NonNullList<Ingredient> ingredients = NonNullList.create();

                    ingredients.add(smithingTransformRecipe.base);
                    ingredients.add(smithingTransformRecipe.addition);
                    entries.add(new JEIUncraftingTableRecipe(smithingTransformRecipe.result, ingredients));
                }
            }

            if (ModList.get().isLoaded("recipestages")) {
                try {
                    Class<?> shapedRecipeStageClass = Class.forName("com.blamejared.recipestages.recipes.ShapedRecipeStage");
                    Class<?> recipeStageClass = Class.forName("com.blamejared.recipestages.recipes.RecipeStage");

                    if (shapedRecipeStageClass.isInstance(recipeHolder)) {
                        Object innerRecipe = shapedRecipeStageClass.getMethod("getRecipe").invoke(recipeHolder);
                        if (innerRecipe instanceof ShapedRecipe shapedRecipe) {
                            entries.add(new JEIUncraftingTableRecipe(shapedRecipe.result, shapedRecipe.getIngredients()));
                        }
                    }

                    if (recipeStageClass.isInstance(recipeHolder)) {
                        Object innerRecipe = recipeStageClass.getMethod("getRecipe").invoke(recipeHolder);
                        if (innerRecipe instanceof ShapelessRecipe shapelessRecipe) {
                            entries.add(new JEIUncraftingTableRecipe(shapelessRecipe.result, shapelessRecipe.getIngredients()));
                        }
                    }
                } catch (Exception e) {
                    System.out.println("[UncraftEverything] Failed to get recipe from RecipeStages: " + e.getMessage());
                }
            }
        });
        registration.addRecipes(UNCRAFTING_TYPE, entries);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(UncraftingTableScreen.class, 59, 35, 22, 15, UNCRAFTING_TYPE);
        registration.addGuiContainerHandler(UncraftingTableScreen.class, new IGuiContainerHandler<>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(UncraftingTableScreen containerScreen) {
                return Collections.singletonList(new Rect2i(containerScreen.getGuiLeft() - 152, containerScreen.getGuiTop(),125, 184));
            }
        });
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(UEBlocks.UNCRAFTING_TABLE.get()), UNCRAFTING_TYPE);
    }
}
