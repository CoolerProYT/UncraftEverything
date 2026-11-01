package com.coolerpromc.uncrafteverything.platform;

import com.coolerpromc.uncrafteverything.platform.services.IIngredientHelper;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public final class ForgeIngredientHelper implements IIngredientHelper {
    public Ingredient strict(ItemStack stack) {
        return net.minecraftforge.common.crafting.StrictNBTIngredient.of(stack);
    }

    public ItemStack remaining(ItemStack stack) {
        return stack.getCraftingRemainingItem();
    }
}
