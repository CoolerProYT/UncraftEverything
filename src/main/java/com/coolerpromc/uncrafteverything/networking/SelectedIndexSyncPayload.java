package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record SelectedIndexSyncPayload(BlockPos blockPos, int index) implements CustomPayload {
    public static final Id<SelectedIndexSyncPayload> TYPE = new Id<>(Identifier.of(UncraftEverything.MODID, "selected_index_sync_payload"));

    public static final PacketCodec<RegistryByteBuf, SelectedIndexSyncPayload> STREAM_CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC,
                    SelectedIndexSyncPayload::blockPos,
                    PacketCodecs.INTEGER,
                    SelectedIndexSyncPayload::index,
                    SelectedIndexSyncPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}