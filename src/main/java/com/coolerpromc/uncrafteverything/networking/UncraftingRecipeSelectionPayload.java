package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class UncraftingRecipeSelectionPayload {
    public static final Identifier ID = new Identifier(UncraftEverything.MODID, "uncrafting_table_recipe_selection");
    private final BlockPos blockPos;
    private final UncraftingTableRecipe recipe;

    public UncraftingRecipeSelectionPayload(BlockPos blockPos, UncraftingTableRecipe recipe){
        this.blockPos = blockPos;
        this.recipe = recipe;
    }

    public static final Codec<UncraftingRecipeSelectionPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("blockPos").forGetter(UncraftingRecipeSelectionPayload::blockPos),
            UncraftingTableRecipe.CODEC.fieldOf("recipe").forGetter(UncraftingRecipeSelectionPayload::recipe)
    ).apply(instance, UncraftingRecipeSelectionPayload::new));

    public BlockPos blockPos() {
        return blockPos;
    }

    public UncraftingTableRecipe recipe() {
        return recipe;
    }
}