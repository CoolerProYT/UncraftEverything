package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ClientConfigSyncPayload(boolean autoMoveToInventory) implements CustomPayload {
    public static final Id<ClientConfigSyncPayload> TYPE = new Id<>(Identifier.of(UncraftEverything.MODID, "client_config_sync"));

    public static final PacketCodec<RegistryByteBuf, ClientConfigSyncPayload> STREAM_CODEC = PacketCodec.tuple(
            PacketCodecs.BOOLEAN,
            ClientConfigSyncPayload::autoMoveToInventory,
            ClientConfigSyncPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}