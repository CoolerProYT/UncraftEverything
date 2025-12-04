package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record UncraftingPageChangePayload(int page, BlockPos blockPos) implements CustomPayload {
    public static final Id<UncraftingPageChangePayload> TYPE = new Id<>(Identifier.of(UncraftEverything.MODID, "uncrafting_recipe_selection_data_payload"));

    public static final PacketCodec<RegistryByteBuf, UncraftingPageChangePayload> STREAM_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            UncraftingPageChangePayload::page,
            BlockPos.PACKET_CODEC,
            UncraftingPageChangePayload::blockPos,
            UncraftingPageChangePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}