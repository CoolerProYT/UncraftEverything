package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

import java.util.HashMap;
import java.util.Map;

public record UEExpPayload(Map<String, Integer> perItemExp) {
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "ue_exp_payload");
    private static final int PROTOCOL_VERSION = 0;
    public static final StreamCodec<RegistryFriendlyByteBuf, UEExpPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.INT),
            UEExpPayload::perItemExp,
            UEExpPayload::new
    );
    public static final SimpleChannel INSTANCE = ChannelBuilder
            .named(TYPE)
            .networkProtocolVersion(PROTOCOL_VERSION)
            .clientAcceptedVersions((status, i) -> i == PROTOCOL_VERSION)
            .serverAcceptedVersions((status, i) -> i == PROTOCOL_VERSION)
            .simpleChannel()
            .messageBuilder(UEExpPayload.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
            .codec(STREAM_CODEC)
            .consumer(ServerPayloadHandler::handleExpCost)
            .add();

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void register(BusGroup bus) {
        // nothing special on setup, channel is built statically
        FMLCommonSetupEvent.getBus(bus).addListener(fmlCommonSetupEvent -> {});
    }
}