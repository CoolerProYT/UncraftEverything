package com.coolerpromc.uncrafteverything.networking;
import com.coolerpromc.uncrafteverything.platform.NetworkChannel;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;


public record UncraftingPageChangePayload(int page, BlockPos blockPos){
    private static final String PROTOCOL_VERSION = "1";
    public static final ResourceLocation TYPE = new ResourceLocation(UncraftEverything.MODID, "uncrafting_recipe_selection_data_payload");
    public static final NetworkChannel INSTANCE = new NetworkChannel(TYPE);

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
