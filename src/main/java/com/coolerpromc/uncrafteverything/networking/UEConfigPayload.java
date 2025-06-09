package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

import java.util.List;

public record UEConfigPayload(UncraftEverythingConfig.RestrictionType restrictionType, List<String> restrictedItems, boolean allowEnchantedItem, UncraftEverythingConfig.ExperienceType experienceType, int experience, boolean allowUnsmithing, boolean allowDamaged){
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "ue_config");
    private static final int PROTOCOL_VERSION = 0;
    public static final StreamCodec<RegistryFriendlyByteBuf, UEConfigPayload> STREAM_CODEC = StreamCodec.composite(
            UncraftEverythingConfig.RestrictionType.STREAM_CODEC,
            UEConfigPayload::restrictionType,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
            UEConfigPayload::restrictedItems,
            ByteBufCodecs.BOOL,
            UEConfigPayload::allowEnchantedItem,
            UncraftEverythingConfig.ExperienceType.STREAM_CODEC,
            UEConfigPayload::experienceType,
            ByteBufCodecs.INT,
            UEConfigPayload::experience,
            ByteBufCodecs.BOOL,
            UEConfigPayload::allowUnsmithing,
            ByteBufCodecs.BOOL,
            UEConfigPayload::allowDamaged,
            UEConfigPayload::new
    );
    public static final SimpleChannel INSTANCE = ChannelBuilder
            .named(TYPE)
            .networkProtocolVersion(PROTOCOL_VERSION)
            .clientAcceptedVersions((status, i) -> i == PROTOCOL_VERSION)
            .serverAcceptedVersions((status, i) -> i == PROTOCOL_VERSION)
            .simpleChannel()
            .messageBuilder(UEConfigPayload.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
            .codec(STREAM_CODEC)
            .consumer(ServerPayloadHandler::handleConfig)
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