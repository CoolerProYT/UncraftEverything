package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public record UncraftingTableDataPayload(BlockPos blockPos, List<UncraftingTableRecipe> recipes) implements CustomPayload {
    public static final CustomPayload.Id<UncraftingTableDataPayload> TYPE = new CustomPayload.Id<>(Identifier.of(UncraftEverything.MODID, "uncrafting_table_data"));

    public static final PacketCodec<RegistryByteBuf, UncraftingTableDataPayload> STREAM_CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC,
                    UncraftingTableDataPayload::blockPos,
                    UncraftingTableRecipe.STREAM_CODEC.collect(PacketCodecs.toList()),
                    UncraftingTableDataPayload::recipes,
                    UncraftingTableDataPayload::new
            );


    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}