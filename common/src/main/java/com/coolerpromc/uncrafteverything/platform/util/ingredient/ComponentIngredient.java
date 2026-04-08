package com.coolerpromc.uncrafteverything.platform.util.ingredient;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.crafting.Ingredient;

public record ComponentIngredient(Ingredient ingredient, DataComponentPatch patch) {
}
