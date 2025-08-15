package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class UncraftingRecipeSelectionDataPayload {
    public final int page;
    public final BlockPos blockPos;

    public UncraftingRecipeSelectionDataPayload(int page, BlockPos blockPos){
        this.page = page;
        this.blockPos = blockPos;
    }

    public static final Identifier TYPE = new Identifier(UncraftEverything.MODID, "uncrafting_recipe_selection_data_payload");

    public static final Codec<UncraftingRecipeSelectionDataPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("page").forGetter(UncraftingRecipeSelectionDataPayload::page),
            BlockPos.CODEC.fieldOf("blockPos").forGetter(UncraftingRecipeSelectionDataPayload::blockPos)
    ).apply(instance, UncraftingRecipeSelectionDataPayload::new));

    public int page(){
        return page;
    }

    public BlockPos blockPos(){
        return blockPos;
    }
}