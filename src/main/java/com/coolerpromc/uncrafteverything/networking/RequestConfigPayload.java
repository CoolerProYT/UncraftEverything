
package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public record RequestConfigPayload() implements CustomPacketPayload {
    public static final Type<RequestConfigPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "request_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestConfigPayload> STREAM_CODEC = StreamCodec.of((buffer, value) -> {}, buffer -> new RequestConfigPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}