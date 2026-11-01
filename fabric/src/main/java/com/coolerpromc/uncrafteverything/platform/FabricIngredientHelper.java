package com.coolerpromc.uncrafteverything.platform;

import com.coolerpromc.uncrafteverything.platform.services.IIngredientHelper;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public final class FabricIngredientHelper implements IIngredientHelper {
    public Ingredient strict(ItemStack stack) {
        return net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients.nbt(
                stack, true);
    }

    public ItemStack remaining(ItemStack stack) {
        return ((net.fabricmc.fabric.api.item.v1.FabricItem) stack.getItem())
                .getRecipeRemainder(stack);
    }
}
