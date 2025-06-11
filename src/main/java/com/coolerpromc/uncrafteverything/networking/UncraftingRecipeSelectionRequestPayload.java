package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class UncraftingRecipeSelectionRequestPayload {
    private static final String PROTOCOL_VERSION = "1";
    public static final ResourceLocation TYPE = new ResourceLocation(UncraftEverything.MODID, "uncrafting_table_recipe_selection_request");
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(TYPE,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(UncraftingRecipeSelectionRequestPayload payload, PacketBuffer byteBuf){

    }

    public static UncraftingRecipeSelectionRequestPayload decode(PacketBuffer byteBuf){
        return new UncraftingRecipeSelectionRequestPayload();
    }

    public static void register(){
        INSTANCE.registerMessage(
                nextId(),
                UncraftingRecipeSelectionRequestPayload.class,
                UncraftingRecipeSelectionRequestPayload::encode,
                UncraftingRecipeSelectionRequestPayload::decode,
                FMLEnvironment.dist.isClient() ? ClientPayloadHandler::handleRecipeSelectionRequest : (payload, context) -> {}
        );
    }
}
