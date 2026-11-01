package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;


import com.coolerpromc.uncrafteverything.platform.NetworkChannel;

public record UncraftingRecipeSelectionRequestPayload() {
    private static final String PROTOCOL_VERSION = "1";
    public static final ResourceLocation TYPE = new ResourceLocation(UncraftEverything.MODID, "uncrafting_table_recipe_selection_request");
    public static final NetworkChannel INSTANCE = new NetworkChannel(TYPE);

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(UncraftingRecipeSelectionRequestPayload payload, FriendlyByteBuf byteBuf){

    }

    public static UncraftingRecipeSelectionRequestPayload decode(FriendlyByteBuf byteBuf){
        return new UncraftingRecipeSelectionRequestPayload();
    }

    public static void register(){
        INSTANCE.registerMessage(
                nextId(),
                UncraftingRecipeSelectionRequestPayload.class,
                UncraftingRecipeSelectionRequestPayload::encode,
                UncraftingRecipeSelectionRequestPayload::decode,
                com.coolerpromc.uncrafteverything.platform.Services.PLATFORM.isClient() ? ClientPayloadHandler::handleRecipeSelectionRequest : (payload, context) -> {}
        );
    }
}
