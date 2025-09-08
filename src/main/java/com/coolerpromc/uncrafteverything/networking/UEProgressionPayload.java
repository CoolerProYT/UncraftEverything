package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Map;

@SuppressWarnings("removal")
public record UEProgressionPayload(Map<String, String> progressionMap){
    public static final ResourceLocation TYPE = new ResourceLocation(UncraftEverything.MODID, "ue_progression_payload");
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(TYPE,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(UEProgressionPayload payload, FriendlyByteBuf byteBuf){
        byteBuf.writeMap(payload.progressionMap, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeUtf);
    }

    public static UEProgressionPayload decode(FriendlyByteBuf byteBuf){
        Map<String, String> progressionMap = byteBuf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readUtf);
        return new UEProgressionPayload(progressionMap);
    }

    public static void register() {
        INSTANCE.registerMessage(
                nextId(),
                UEProgressionPayload.class,
                UEProgressionPayload::encode,
                UEProgressionPayload::decode,
                ServerPayloadHandler::handleProgression
        );
    }
}