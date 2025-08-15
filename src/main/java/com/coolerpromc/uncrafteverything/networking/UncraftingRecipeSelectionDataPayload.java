package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record UncraftingRecipeSelectionDataPayload(int page, BlockPos blockPos) implements CustomPayload {
    public static final CustomPayload.Id<UncraftingRecipeSelectionDataPayload> TYPE = new Id<>(Identifier.of(UncraftEverything.MODID, "uncrafting_recipe_selection_data_payload"));

    public static final PacketCodec<RegistryByteBuf, UncraftingRecipeSelectionDataPayload> STREAM_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            UncraftingRecipeSelectionDataPayload::page,
            BlockPos.PACKET_CODEC,
            UncraftingRecipeSelectionDataPayload::blockPos,
            UncraftingRecipeSelectionDataPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}