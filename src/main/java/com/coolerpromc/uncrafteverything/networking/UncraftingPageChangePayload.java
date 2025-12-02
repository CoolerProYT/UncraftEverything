package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;

public record UncraftingPageChangePayload(int page, BlockPos blockPos){
    private static final String PROTOCOL_VERSION = "1";
    public static final ResourceLocation TYPE = new ResourceLocation(UncraftEverything.MODID, "uncrafting_recipe_selection_data_payload");
    public static final net.minecraftforge.network.simple.SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(TYPE,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(UncraftingPageChangePayload payload, FriendlyByteBuf byteBuf){
        byteBuf.writeVarInt(payload.page);
        byteBuf.writeBlockPos(payload.blockPos);
    }

    public static UncraftingPageChangePayload decode(FriendlyByteBuf byteBuf){
        int page = byteBuf.readVarInt();
        BlockPos blockPos = byteBuf.readBlockPos();
        return new UncraftingPageChangePayload(page, blockPos);
    }

    public static void register() {
        INSTANCE.registerMessage(
                nextId(),
                UncraftingPageChangePayload.class,
                UncraftingPageChangePayload::encode,
                UncraftingPageChangePayload::decode,
                ServerPayloadHandler::handleRecipeSelectionData
        );
    }
}
