package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.Map;

public record UEProgressionPayload(Map<String, String> progressionMap) {
    public static final Identifier TYPE = new Identifier(UncraftEverything.MODID, "ue_progression_payload");

    public static PacketByteBuf encode(PacketByteBuf buf, UEProgressionPayload payload) {
        buf.writeMap(payload.progressionMap, PacketByteBuf::writeString, PacketByteBuf::writeString);
        return buf;
    }

    public static UEProgressionPayload decode(PacketByteBuf buf) {
        return new UEProgressionPayload(buf.readMap(PacketByteBuf::readString, PacketByteBuf::readString));
    }
}