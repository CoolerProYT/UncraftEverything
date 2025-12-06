package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientConfigSyncPayload(boolean autoMoveToInventory) implements CustomPacketPayload {
    public static final Type<ClientConfigSyncPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(UncraftEverything.MODID, "client_config_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientConfigSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ClientConfigSyncPayload::autoMoveToInventory,
            ClientConfigSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}