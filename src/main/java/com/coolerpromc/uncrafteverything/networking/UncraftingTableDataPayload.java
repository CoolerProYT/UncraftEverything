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

    public static void encode(UncraftingTableDataPayload payload, RegistryFriendlyByteBuf byteBuf){
        byteBuf.writeBlockPos(payload.blockPos());

        byteBuf.writeVarInt(payload.recipes().size());

        for (UncraftingTableRecipe recipe : payload.recipes()) {
            if (byteBuf instanceof RegistryFriendlyByteBuf registryBuf) {
                UncraftingTableRecipe.STREAM_CODEC.encode(registryBuf, recipe);
            } else {
                byteBuf.writeBoolean(!recipe.getInput().isEmpty());
                if (!recipe.getInput().isEmpty()) {
                    ItemStack.STREAM_CODEC.encode(byteBuf, recipe.getInput());
                }

                byteBuf.writeVarInt(recipe.getOutputs().size());
                for (var output : recipe.getOutputs()) {
                    byteBuf.writeBoolean(!output.isEmpty());
                    if (!output.isEmpty()) {
                        ItemStack.STREAM_CODEC.encode(byteBuf, output);
                    }
                }
            }
        }
    }

    public static UncraftingTableDataPayload decode(RegistryFriendlyByteBuf byteBuf){
        BlockPos pos = byteBuf.readBlockPos();

        int recipeCount = byteBuf.readVarInt();
        List<UncraftingTableRecipe> recipes = new java.util.ArrayList<>(recipeCount);

        for (int i = 0; i < recipeCount; i++) {
            if (byteBuf instanceof RegistryFriendlyByteBuf registryBuf) {
                recipes.add(UncraftingTableRecipe.STREAM_CODEC.decode(registryBuf));
            } else {
                var input = byteBuf.readBoolean() ? ItemStack.STREAM_CODEC.decode(byteBuf) : net.minecraft.world.item.ItemStack.EMPTY;

                int outputCount = byteBuf.readVarInt();
                List<net.minecraft.world.item.ItemStack> outputs = new java.util.ArrayList<>(outputCount);

                for (int j = 0; j < outputCount; j++) {
                    outputs.add(byteBuf.readBoolean() ? ItemStack.STREAM_CODEC.decode(byteBuf) : net.minecraft.world.item.ItemStack.EMPTY);
                }

                recipes.add(new UncraftingTableRecipe(input, outputs));
            }
        }

        return new UncraftingTableDataPayload(pos, recipes);
    }

    public static void register(BusGroup bus) {
        // nothing special on setup, channel is built statically
        FMLCommonSetupEvent.getBus(bus).addListener(fmlCommonSetupEvent -> {});
    }
}
