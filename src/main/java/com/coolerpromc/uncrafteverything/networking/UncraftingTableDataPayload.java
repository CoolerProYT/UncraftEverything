package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class UncraftingTableDataPayload{
    public static final Identifier ID = new Identifier(UncraftEverything.MODID, "uncrafting_table_data");
    public final BlockPos blockPos;
    public final List<UncraftingTableRecipe> recipes;

    public UncraftingTableDataPayload(BlockPos blockPos, List<UncraftingTableRecipe> recipes){
        this.blockPos = blockPos;
        this.recipes = recipes;
    }

    public static final Codec<UncraftingTableDataPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("block_pos").forGetter(UncraftingTableDataPayload::blockPos),
            UncraftingTableRecipe.CODEC.listOf().fieldOf("recipes").forGetter(UncraftingTableDataPayload::recipes)
    ).apply(instance, UncraftingTableDataPayload::new));

    public BlockPos blockPos() {
        return blockPos;
    }

    public List<UncraftingTableRecipe> recipes() {
        return recipes;
    }
}