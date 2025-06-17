package com.coolerpromc.uncrafteverything.compat.jei;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen;
import com.coolerpromc.uncrafteverything.util.JEIUncraftingTableRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.armortrim.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@JeiPlugin
public class UEJEIPlugin implements IModPlugin {
    public static final RecipeType<JEIUncraftingTableRecipe> UNCRAFTING_TYPE = RecipeType.create(UncraftEverything.MODID, "uncrafting_table", JEIUncraftingTableRecipe.class);

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "jei_plugin");
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
        registration.addRecipes(UNCRAFTING_TYPE, entries);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(UncraftingTableScreen.class, 59, 35, 22, 15, UNCRAFTING_TYPE);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(UEBlocks.UNCRAFTING_TABLE.get()), UNCRAFTING_TYPE);
    }
}
