package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public record UncraftingRecipeSelectionRequestPayload() {
    private static final int PROTOCOL_VERSION = 0;
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "uncrafting_table_recipe_selection_request");
    public static final SimpleChannel INSTANCE = ChannelBuilder
            .named(TYPE)
            .networkProtocolVersion(PROTOCOL_VERSION)
            .clientAcceptedVersions((status, i) -> i == PROTOCOL_VERSION)
            .serverAcceptedVersions((status, i) -> i == PROTOCOL_VERSION)
            .simpleChannel()
            .messageBuilder(UncraftingRecipeSelectionRequestPayload.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
            .encoder(UncraftingRecipeSelectionRequestPayload::encode)
            .decoder(UncraftingRecipeSelectionRequestPayload::decode)
            .consumer(FMLEnvironment.dist.isClient() ? ClientPayloadHandler::handleRecipeSelectionRequest : (payload, context) -> {})
            .add();


    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(UncraftingRecipeSelectionRequestPayload payload, RegistryFriendlyByteBuf byteBuf){

    }

    public static UncraftingRecipeSelectionRequestPayload decode(RegistryFriendlyByteBuf byteBuf){
        return new UncraftingRecipeSelectionRequestPayload();
    }

    public static void register(IEventBus bus) {
        // nothing special on setup, channel is built statically
        bus.addListener((FMLCommonSetupEvent e) -> { /* no-op */ });
    }
}
