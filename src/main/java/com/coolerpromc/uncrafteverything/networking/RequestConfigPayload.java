
package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public record RequestConfigPayload() {
    private static final int PROTOCOL_VERSION = 0;
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "request_config");
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestConfigPayload> STREAM_CODEC = StreamCodec.of((buffer, value) -> {}, buffer -> new RequestConfigPayload());
    public static final SimpleChannel INSTANCE = ChannelBuilder
            .named(TYPE)
            .networkProtocolVersion(PROTOCOL_VERSION)
            .clientAcceptedVersions((status, i) -> i == PROTOCOL_VERSION)
            .serverAcceptedVersions((status, i) -> i == PROTOCOL_VERSION)
            .simpleChannel()
            .messageBuilder(RequestConfigPayload.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
            .codec(STREAM_CODEC)
            .consumer(ServerPayloadHandler::handleRequestConfig)
            .add();

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void register(IEventBus bus) {
        // nothing special on setup, channel is built statically
        bus.addListener((FMLCommonSetupEvent e) -> { /* no-op */ });
    }
}