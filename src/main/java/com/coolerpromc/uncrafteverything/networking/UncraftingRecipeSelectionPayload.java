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
import net.minecraftforge.network.*;
import net.minecraftforge.network.SimpleChannel;

public record UncraftingRecipeSelectionPayload(BlockPos blockPos, UncraftingTableRecipe recipe) {
    private static final int PROTOCOL_VERSION = 0;
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "uncrafting_table_recipe_selection");
    public static final SimpleChannel INSTANCE = ChannelBuilder
            .named(TYPE)
            .networkProtocolVersion(PROTOCOL_VERSION)
            .clientAcceptedVersions((status, i) -> i == PROTOCOL_VERSION)
            .serverAcceptedVersions((status, i) -> i == PROTOCOL_VERSION)
            .simpleChannel()
            .messageBuilder(UncraftingRecipeSelectionPayload.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
            .encoder(UncraftingRecipeSelectionPayload::encode)
            .decoder(UncraftingRecipeSelectionPayload::decode)
            .consumer(ServerPayloadHandler::handleRecipeSelection)
            .add();

    public static final Codec<UncraftingRecipeSelectionPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("blockPos").forGetter(UncraftingRecipeSelectionPayload::blockPos),
            UncraftingTableRecipe.CODEC.fieldOf("recipe").forGetter(UncraftingRecipeSelectionPayload::recipe)
    ).apply(instance, UncraftingRecipeSelectionPayload::new));

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(UncraftingRecipeSelectionPayload payload, FriendlyByteBuf byteBuf){
        byteBuf.writeJsonWithCodec(CODEC, payload);
    }

    public static UncraftingRecipeSelectionPayload decode(FriendlyByteBuf byteBuf){
        return byteBuf.readJsonWithCodec(CODEC);
    }

    public static void register(IEventBus bus) {
        // nothing special on setup, channel is built statically
        bus.addListener((FMLCommonSetupEvent e) -> { /* no-op */ });
    }
}
