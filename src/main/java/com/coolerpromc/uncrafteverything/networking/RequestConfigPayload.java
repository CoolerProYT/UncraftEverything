
package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RequestConfigPayload() implements CustomPayload {
    public static final Id<RequestConfigPayload> TYPE = new Id<>(Identifier.of(UncraftEverything.MODID, "request_config"));

    public static final PacketCodec<RegistryByteBuf, RequestConfigPayload> STREAM_CODEC = PacketCodec.of((buffer, value) -> {}, buffer -> new RequestConfigPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}