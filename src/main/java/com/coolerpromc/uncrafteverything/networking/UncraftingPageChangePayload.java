package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public record UncraftingPageChangePayload(int page, BlockPos blockPos){
    private static final int PROTOCOL_VERSION = 0;
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "uncrafting_recipe_selection_data_payload");
    public static final SimpleChannel INSTANCE = ChannelBuilder
            .named(TYPE)
            .networkProtocolVersion(PROTOCOL_VERSION)
            .clientAcceptedVersions((status, i) -> i == PROTOCOL_VERSION)
            .serverAcceptedVersions((status, i) -> i == PROTOCOL_VERSION)
            .simpleChannel()
            .messageBuilder(UncraftingPageChangePayload.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
            .encoder(UncraftingPageChangePayload::encode)
            .decoder(UncraftingPageChangePayload::decode)
            .consumer(ServerPayloadHandler::handleRecipeSelectionData)
            .add();

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, UncraftingPageChangePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            UncraftingPageChangePayload::page,
            BlockPos.STREAM_CODEC,
            UncraftingPageChangePayload::blockPos,
            UncraftingPageChangePayload::new
    );

    public static void encode(UncraftingPageChangePayload payload, FriendlyByteBuf byteBuf){
        byteBuf.writeVarInt(payload.page);
        byteBuf.writeBlockPos(payload.blockPos);
    }

    public static UncraftingPageChangePayload decode(FriendlyByteBuf byteBuf){
        int page = byteBuf.readVarInt();
        BlockPos blockPos = byteBuf.readBlockPos();
        return new UncraftingPageChangePayload(page, blockPos);
    }

    public static void register(BusGroup bus) {
        // nothing special on setup, channel is built statically
        FMLCommonSetupEvent.getBus(bus).addListener(fmlCommonSetupEvent -> {});
    }
}
