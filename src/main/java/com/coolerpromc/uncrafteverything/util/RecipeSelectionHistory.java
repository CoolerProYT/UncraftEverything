package com.coolerpromc.uncrafteverything.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;

public record RecipeSelectionHistory(UncraftingTableRecipe recipe, int page, int index, int totalRecipeCount, DataComponentPatch patch) {
    public static final Codec<RecipeSelectionHistory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UncraftingTableRecipe.CODEC.fieldOf("recipe").forGetter(RecipeSelectionHistory::recipe),
            Codec.INT.fieldOf("page").forGetter(RecipeSelectionHistory::page),
            Codec.INT.fieldOf("index").forGetter(RecipeSelectionHistory::index),
            Codec.INT.fieldOf("totalRecipeCount").forGetter(RecipeSelectionHistory::totalRecipeCount),
            DataComponentPatch.CODEC.fieldOf("patch").forGetter(RecipeSelectionHistory::patch)
    ).apply(instance, RecipeSelectionHistory::new));
}
