package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record UncraftingRecipeSelectionPayload(BlockPos blockPos, UncraftingTableRecipe recipe) {
    public static final Identifier ID = new Identifier(UncraftEverything.MODID, "uncrafting_table_recipe_selection");

    public static final Codec<UncraftingRecipeSelectionPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("blockPos").forGetter(UncraftingRecipeSelectionPayload::blockPos),
            UncraftingTableRecipe.CODEC.fieldOf("recipe").forGetter(UncraftingRecipeSelectionPayload::recipe)
    ).apply(instance, UncraftingRecipeSelectionPayload::new));
}