package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record UncraftingRecipeSelectionPayload(BlockPos blockPos, UncraftingTableRecipe recipe) implements CustomPayload {
    public static final CustomPayload.Id<UncraftingRecipeSelectionPayload> TYPE = new Id<>(Identifier.of(UncraftEverything.MODID, "uncrafting_table_recipe_selection"));

    public static final PacketCodec<RegistryByteBuf, UncraftingRecipeSelectionPayload> STREAM_CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC,
                    UncraftingRecipeSelectionPayload::blockPos,
                    UncraftingTableRecipe.STREAM_CODEC,
                    UncraftingRecipeSelectionPayload::recipe,
                    UncraftingRecipeSelectionPayload::new
            );


    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}