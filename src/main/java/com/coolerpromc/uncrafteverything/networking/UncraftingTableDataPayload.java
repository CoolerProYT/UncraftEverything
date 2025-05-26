package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public record UncraftingTableDataPayload(BlockPos blockPos, List<UncraftingTableRecipe> recipes){
    public static final Identifier ID = new Identifier(UncraftEverything.MODID, "uncrafting_table_data");

    public static PacketByteBuf encode(UncraftingTableDataPayload payload, PacketByteBuf byteBuf){
        byteBuf.writeBlockPos(payload.blockPos());
        byteBuf.writeVarInt(payload.recipes().size());

        for (UncraftingTableRecipe recipe : payload.recipes()) {
            recipe.writeToBuf(byteBuf);
        }

        return byteBuf;
    }

    public static UncraftingTableDataPayload decode(PacketByteBuf byteBuf){
        BlockPos blockPos = byteBuf.readBlockPos();
        int recipeCount = byteBuf.readVarInt();
        List<UncraftingTableRecipe> recipes = new ArrayList<>();

        for (int i = 0; i < recipeCount; i++) {
            recipes.add(UncraftingTableRecipe.readFromBuf(byteBuf));
        }

        return new UncraftingTableDataPayload(blockPos, recipes);
    }
}