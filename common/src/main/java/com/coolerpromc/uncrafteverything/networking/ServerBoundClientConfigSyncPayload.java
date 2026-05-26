package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.platform.util.PayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ServerBoundClientConfigSyncPayload(boolean autoMoveToInventory) implements CustomPacketPayload {
    public static final Type<ServerBoundClientConfigSyncPayload> TYPE = new Type<>(Constants.id("client_config_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundClientConfigSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ServerBoundClientConfigSyncPayload::autoMoveToInventory,
            ServerBoundClientConfigSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(PayloadContext context){
        context.execute(() -> {
            UncraftEverything.AUTO_MOVE = this.autoMoveToInventory();
        });
    }
}