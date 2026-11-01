package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import com.coolerpromc.uncrafteverything.platform.NetworkChannel;

public record RequestConfigPayload(){
    private static final String PROTOCOL_VERSION = "1";
    public static final ResourceLocation TYPE = new ResourceLocation(UncraftEverything.MODID, "request_config");
    public static final NetworkChannel INSTANCE = new NetworkChannel(TYPE);

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(RequestConfigPayload payload, FriendlyByteBuf byteBuf){

    }

    public static RequestConfigPayload decode(FriendlyByteBuf byteBuf){
        return new RequestConfigPayload();
    }

    public static void register(){
        INSTANCE.registerMessage(
                nextId(),
                RequestConfigPayload.class,
                RequestConfigPayload::encode,
                RequestConfigPayload::decode,
                ServerPayloadHandler::handleRequestConfig
        );
    }
}