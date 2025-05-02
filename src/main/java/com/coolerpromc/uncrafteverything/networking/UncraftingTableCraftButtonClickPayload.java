package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.*;
import net.minecraftforge.network.SimpleChannel;

public record UncraftingTableCraftButtonClickPayload(BlockPos blockPos) {
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

    public static final Codec<UncraftingTableCraftButtonClickPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("blockPos").forGetter(UncraftingTableCraftButtonClickPayload::blockPos)
    ).apply(instance, UncraftingTableCraftButtonClickPayload::new));

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(UncraftingTableCraftButtonClickPayload payload, FriendlyByteBuf byteBuf){
        byteBuf.writeJsonWithCodec(CODEC, payload);
    }

    public static UncraftingTableCraftButtonClickPayload decode(FriendlyByteBuf byteBuf){
        return byteBuf.readJsonWithCodec(CODEC);
    }

    public static void register(IEventBus bus) {
        // nothing special on setup, channel is built statically
        bus.addListener((FMLCommonSetupEvent e) -> { /* no-op */ });
    }
}
