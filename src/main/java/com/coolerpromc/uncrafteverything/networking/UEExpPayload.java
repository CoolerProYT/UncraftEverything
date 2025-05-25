package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.Map;

public record UEExpPayload(Map<String, Integer> perItemExp) {
    public static final Identifier TYPE = new Identifier(UncraftEverything.MODID, "ue_exp_payload");

    public static PacketByteBuf encode(PacketByteBuf buf, UEExpPayload payload) {
        buf.writeMap(payload.perItemExp, PacketByteBuf::writeString, PacketByteBuf::writeInt);
        return buf;
    }

    public static UEExpPayload decode(PacketByteBuf buf) {
        return new UEExpPayload(buf.readMap(PacketByteBuf::readString, PacketByteBuf::readInt));
    }
}