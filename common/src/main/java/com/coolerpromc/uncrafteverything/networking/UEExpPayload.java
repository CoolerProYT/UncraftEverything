package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import com.coolerpromc.uncrafteverything.platform.NetworkChannel;

import java.util.Map;

public record UEExpPayload(Map<String, Integer> perItemExp) {
    public static final ResourceLocation TYPE = new ResourceLocation(UncraftEverything.MODID, "ue_exp_payload");
    private static final String PROTOCOL_VERSION = "1";

    public static final NetworkChannel INSTANCE = new NetworkChannel(TYPE);

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(UEExpPayload payload, FriendlyByteBuf byteBuf){
        byteBuf.writeMap(payload.perItemExp, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeVarInt);
    }

    public static UEExpPayload decode(FriendlyByteBuf byteBuf){
        Map<String, Integer> perItemExp = byteBuf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readVarInt);
        return new UEExpPayload(perItemExp);
    }

    public static void register(){
        INSTANCE.registerMessage(
                nextId(),
                UEExpPayload.class,
                UEExpPayload::encode,
                UEExpPayload::decode,
                ServerPayloadHandler::handleExpCost
        );
    }
}