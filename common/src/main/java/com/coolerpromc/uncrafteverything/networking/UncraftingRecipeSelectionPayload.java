package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import com.coolerpromc.uncrafteverything.platform.NetworkChannel;

public record UncraftingRecipeSelectionPayload(BlockPos blockPos, UncraftingTableRecipe recipe) {
    private static final String PROTOCOL_VERSION = "1";
    public static final ResourceLocation TYPE = new ResourceLocation(UncraftEverything.MODID, "uncrafting_table_recipe_selection");
    public static final NetworkChannel INSTANCE = new NetworkChannel(TYPE);

    public static final Codec<UncraftingRecipeSelectionPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("blockPos").forGetter(UncraftingRecipeSelectionPayload::blockPos),
            UncraftingTableRecipe.CODEC.fieldOf("recipe").forGetter(UncraftingRecipeSelectionPayload::recipe)
    ).apply(instance, UncraftingRecipeSelectionPayload::new));

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(UncraftingRecipeSelectionPayload payload, FriendlyByteBuf byteBuf){
        byteBuf.writeBlockPos(payload.blockPos());
        payload.recipe().writeToBuf(byteBuf);
    }

    public static UncraftingRecipeSelectionPayload decode(FriendlyByteBuf byteBuf){
        return new UncraftingRecipeSelectionPayload(byteBuf.readBlockPos(), UncraftingTableRecipe.readFromBuf(byteBuf));
    }

    public static void register(){
        INSTANCE.registerMessage(
                nextId(),
                UncraftingRecipeSelectionPayload.class,
                UncraftingRecipeSelectionPayload::encode,
                UncraftingRecipeSelectionPayload::decode,
                ServerPayloadHandler::handleRecipeSelection
        );
    }
}
