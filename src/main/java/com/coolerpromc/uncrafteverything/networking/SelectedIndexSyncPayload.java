package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SelectedIndexSyncPayload(BlockPos blockPos, int index) implements CustomPacketPayload {
    public static final Type<SelectedIndexSyncPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(UncraftEverything.MODID, "selected_index_sync_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SelectedIndexSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    SelectedIndexSyncPayload::blockPos,
                    ByteBufCodecs.INT,
                    SelectedIndexSyncPayload::index,
                    SelectedIndexSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
