package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

import java.util.List;

public record UncraftingTableDataPayload(BlockPos blockPos, List<UncraftingTableRecipe> recipes) {
    private static final int PROTOCOL_VERSION = 0;
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "uncrafting_table_data");
    public static final SimpleChannel INSTANCE = ChannelBuilder
            .named(TYPE)
            .networkProtocolVersion(PROTOCOL_VERSION)
            .clientAcceptedVersions((status, i) -> i == PROTOCOL_VERSION)
            .serverAcceptedVersions((status, i) -> i == PROTOCOL_VERSION)
            .simpleChannel()
            .messageBuilder(UncraftingTableDataPayload.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
            .encoder(UncraftingTableDataPayload::encode)
            .decoder(UncraftingTableDataPayload::decode)
            .consumer(FMLEnvironment.dist.isClient() ? ClientPayloadHandler::handleBlockEntityData : (payload, context) -> {})
            .add();

    public static final Codec<UncraftingTableDataPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("blockPos").forGetter(UncraftingTableDataPayload::blockPos),
            UncraftingTableRecipe.CODEC.listOf().fieldOf("recipes").forGetter(UncraftingTableDataPayload::recipes)
    ).apply(instance, UncraftingTableDataPayload::new));

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(UncraftingTableDataPayload payload, FriendlyByteBuf byteBuf){
        byteBuf.writeJsonWithCodec(CODEC, payload);
    }

    public static UncraftingTableDataPayload decode(FriendlyByteBuf byteBuf){
        return byteBuf.readJsonWithCodec(CODEC);
    }

    public static void register(IEventBus bus) {
        // nothing special on setup, channel is built statically
        bus.addListener((FMLCommonSetupEvent e) -> { /* no-op */ });
    }
}
