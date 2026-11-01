package com.coolerpromc.uncrafteverything.platform.services;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public interface IIngredientHelper {
    Ingredient strict(ItemStack stack);

    ItemStack remaining(ItemStack stack);
}
