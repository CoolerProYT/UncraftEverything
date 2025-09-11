package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.util.BufferUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.Map;

public class UEProgressionPayload {
    public static final Identifier TYPE = new Identifier(UncraftEverything.MODID, "ue_progression_payload");

    private final Map<String, String> progressionMap;

    public UEProgressionPayload(Map<String, String> progressionMap){
        this.progressionMap = progressionMap;
    }

    public static PacketByteBuf encode(PacketByteBuf buf, UEProgressionPayload payload) {
        BufferUtil.writeStringMap(buf, payload.progressionMap);
        return buf;
    }

    public static UEProgressionPayload decode(PacketByteBuf buf) {
        return new UEProgressionPayload(BufferUtil.readStringMap(buf));
    }

    public Map<String, String> progressionMap(){
        return progressionMap;
    }
}