package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

import java.util.ArrayList;
import java.util.List;

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

    public static void encode(UncraftingRecipeSelectionPayload payload, RegistryFriendlyByteBuf byteBuf){
        byteBuf.writeBlockPos(payload.blockPos());

        ItemStack.STREAM_CODEC.encode(byteBuf, payload.recipe().getInput());

        byteBuf.writeInt(payload.recipe().getOutputs().size());

        for (ItemStack output : payload.recipe().getOutputs()) {
            byteBuf.writeBoolean(!output.isEmpty());
            if (!output.isEmpty()) {
                ItemStack.STREAM_CODEC.encode(byteBuf, output);
            }
        }
    }

    public static UncraftingRecipeSelectionPayload decode(RegistryFriendlyByteBuf byteBuf){
        BlockPos blockPos = byteBuf.readBlockPos();
        ItemStack input = ItemStack.STREAM_CODEC.decode(byteBuf);
        int outputCount = byteBuf.readInt();
        List<ItemStack> outputs = new ArrayList<>(outputCount);

        for (int i = 0; i < outputCount; i++) {
            if (byteBuf.readBoolean()) {
                outputs.add(ItemStack.STREAM_CODEC.decode(byteBuf));
            } else {
                outputs.add(ItemStack.EMPTY);
            }
        }

        return new UncraftingRecipeSelectionPayload(blockPos, new UncraftingTableRecipe(input, outputs));
    }

    public static void register(BusGroup bus) {
        // nothing special on setup, channel is built statically
        FMLCommonSetupEvent.getBus(bus).addListener(fmlCommonSetupEvent -> {});
    }
}
