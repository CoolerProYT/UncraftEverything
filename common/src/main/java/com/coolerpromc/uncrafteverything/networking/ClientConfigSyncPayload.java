package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import com.coolerpromc.uncrafteverything.platform.NetworkChannel;

public record ClientConfigSyncPayload(boolean autoMoveToInventory) {
    private static final String PROTOCOL_VERSION = "1";
    public static final ResourceLocation TYPE = new ResourceLocation(UncraftEverything.MODID, "client_config_sync");
    public static final NetworkChannel INSTANCE = new NetworkChannel(TYPE);

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(ClientConfigSyncPayload payload, FriendlyByteBuf byteBuf){
        byteBuf.writeBoolean(payload.autoMoveToInventory);
    }

    public static ClientConfigSyncPayload decode(FriendlyByteBuf byteBuf){
        return new ClientConfigSyncPayload(byteBuf.readBoolean());
    }

    public static void register(){
        INSTANCE.registerMessage(
                nextId(),
                ClientConfigSyncPayload.class,
                ClientConfigSyncPayload::encode,
                ClientConfigSyncPayload::decode,
                ServerPayloadHandler::handleClientConfigSync
        );
    }
}