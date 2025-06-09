
package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ResponseConfigPayload(UncraftEverythingConfig.RestrictionType restrictionType, List<String> restrictedItems, boolean allowEnchantedItem, UncraftEverythingConfig.ExperienceType experienceType, int experience, boolean allowUnsmithing, boolean allowDamaged, Map<String, Integer> perItemExp){
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "response_config");
    private static final int PROTOCOL_VERSION = 0;
    public static final StreamCodec<RegistryFriendlyByteBuf, ResponseConfigPayload> STREAM_CODEC = StreamCodec.composite(
            UncraftEverythingConfig.RestrictionType.STREAM_CODEC,
            ResponseConfigPayload::restrictionType,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
            ResponseConfigPayload::restrictedItems,
            ByteBufCodecs.BOOL,
            ResponseConfigPayload::allowEnchantedItem,
            UncraftEverythingConfig.ExperienceType.STREAM_CODEC,
            ResponseConfigPayload::experienceType,
            ByteBufCodecs.INT,
            ResponseConfigPayload::experience,
            ByteBufCodecs.BOOL,
            ResponseConfigPayload::allowUnsmithing,
            ByteBufCodecs.BOOL,
            ResponseConfigPayload::allowDamaged,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT),
            ResponseConfigPayload::perItemExp,
            ResponseConfigPayload::new
    );

    public static final SimpleChannel INSTANCE = ChannelBuilder
            .named(TYPE)
            .networkProtocolVersion(PROTOCOL_VERSION)
            .clientAcceptedVersions((status, i) -> i == PROTOCOL_VERSION)
            .serverAcceptedVersions((status, i) -> i == PROTOCOL_VERSION)
            .simpleChannel()
            .messageBuilder(ResponseConfigPayload.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
            .codec(STREAM_CODEC)
            .consumer(FMLEnvironment.dist.isClient() ? ClientPayloadHandler::handleConfigSync : (payload, context) -> {})
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