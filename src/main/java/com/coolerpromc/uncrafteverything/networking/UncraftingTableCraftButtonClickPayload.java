package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public record UncraftingTableCraftButtonClickPayload(BlockPos blockPos, boolean hasShiftDown) {
    private static final int PROTOCOL_VERSION = 0;
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "uncrafting_table_craft_button_click");
    public static final SimpleChannel INSTANCE = ChannelBuilder
            .named(TYPE)
            .networkProtocolVersion(PROTOCOL_VERSION)
            .clientAcceptedVersions((status, i) -> i == PROTOCOL_VERSION)
            .serverAcceptedVersions((status, i) -> i == PROTOCOL_VERSION)
            .simpleChannel()
            .messageBuilder(UncraftingTableCraftButtonClickPayload.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
            .encoder(UncraftingTableCraftButtonClickPayload::encode)
            .decoder(UncraftingTableCraftButtonClickPayload::decode)
            .consumer(ServerPayloadHandler::handleButtonClick)
            .add();

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(UncraftingTableCraftButtonClickPayload payload, FriendlyByteBuf byteBuf){
        byteBuf.writeBlockPos(payload.blockPos);
        byteBuf.writeBoolean(payload.hasShiftDown);
    }

    public static UncraftingTableCraftButtonClickPayload decode(FriendlyByteBuf byteBuf){
        BlockPos blockPos = byteBuf.readBlockPos();
        boolean hasShiftDown = byteBuf.readBoolean();
        return new UncraftingTableCraftButtonClickPayload(blockPos, hasShiftDown);
    }

    public static void register(BusGroup bus) {
        // nothing special on setup, channel is built statically
        FMLCommonSetupEvent.getBus(bus).addListener(fmlCommonSetupEvent -> {});
    }
}
