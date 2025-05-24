package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RequestConfigPayload() implements CustomPacketPayload {
    public static final Type<RequestConfigPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "request_config"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestConfigPayload> STREAM_CODEC = StreamCodec.of((buffer, value) -> {}, buffer -> new RequestConfigPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
