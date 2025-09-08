package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.util.BufferUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Map;

public class UEProgressionPayload{
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
    private final Map<String, String> progressionMap;

    public UEProgressionPayload(Map<String, String> progressionMap){
        this.progressionMap = progressionMap;
    }

    public static void encode(UEProgressionPayload payload, PacketBuffer byteBuf){
        BufferUtil.writeStringMap(byteBuf, payload.progressionMap);
    }

    public static UEProgressionPayload decode(PacketBuffer byteBuf){
        Map<String, String> progressionMap = BufferUtil.readStringMap(byteBuf);
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

    public Map<String, String> progressionMap(){
        return progressionMap;
    }
}