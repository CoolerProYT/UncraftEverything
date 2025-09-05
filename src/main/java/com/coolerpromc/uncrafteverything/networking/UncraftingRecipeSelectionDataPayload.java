package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public record UncraftingRecipeSelectionDataPayload(int page, BlockPos blockPos){
    private static final int PROTOCOL_VERSION = 0;
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "uncrafting_recipe_selection_data_payload");
    public static final SimpleChannel INSTANCE = ChannelBuilder
            .named(TYPE)
            .networkProtocolVersion(PROTOCOL_VERSION)
            .clientAcceptedVersions((status, i) -> i == PROTOCOL_VERSION)
            .serverAcceptedVersions((status, i) -> i == PROTOCOL_VERSION)
            .simpleChannel()
            .messageBuilder(UncraftingRecipeSelectionDataPayload.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
            .encoder(UncraftingRecipeSelectionDataPayload::encode)
            .decoder(UncraftingRecipeSelectionDataPayload::decode)
            .consumer(ServerPayloadHandler::handleRecipeSelectionData)
            .add();

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, UncraftingRecipeSelectionDataPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            UncraftingRecipeSelectionDataPayload::page,
            BlockPos.STREAM_CODEC,
            UncraftingRecipeSelectionDataPayload::blockPos,
            UncraftingRecipeSelectionDataPayload::new
    );

    public static void encode(UncraftingRecipeSelectionDataPayload payload, FriendlyByteBuf byteBuf){
        byteBuf.writeVarInt(payload.page);
        byteBuf.writeBlockPos(payload.blockPos);
    }

    public static UncraftingRecipeSelectionDataPayload decode(FriendlyByteBuf byteBuf){
        int page = byteBuf.readVarInt();
        BlockPos blockPos = byteBuf.readBlockPos();
        return new UncraftingRecipeSelectionDataPayload(page, blockPos);
    }

    public static void register(IEventBus bus) {
        // nothing special on setup, channel is built statically
        bus.addListener((FMLCommonSetupEvent e) -> { /* no-op */ });
    }
}
