package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UncraftingRecipeSelectionPayload(BlockPos blockPos, UncraftingTableRecipe recipe) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UncraftingRecipeSelectionPayload> TYPE = new Type<>(new ResourceLocation(UncraftEverything.MODID, "uncrafting_table_recipe_selection"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UncraftingRecipeSelectionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    UncraftingRecipeSelectionPayload::blockPos,
                    UncraftingTableRecipe.STREAM_CODEC,
                    UncraftingRecipeSelectionPayload::recipe,
                    UncraftingRecipeSelectionPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
