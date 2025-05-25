package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record RequestConfigPayload() {
    public static final Identifier TYPE = new Identifier(UncraftEverything.MODID, "request_config");

    public static PacketByteBuf encode(PacketByteBuf buffer, RequestConfigPayload value) {
        return buffer;
    }

    public static RequestConfigPayload decode(PacketByteBuf buffer) {
        return new RequestConfigPayload();
    }
}