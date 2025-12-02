package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record UncraftingRecipeSelectionRequestPayload() implements CustomPacketPayload {
    public static final Type<UncraftingRecipeSelectionRequestPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(UncraftEverything.MODID, "uncrafting_table_recipe_selection_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UncraftingRecipeSelectionRequestPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, value) -> {

                    },
                    buffer -> new UncraftingRecipeSelectionRequestPayload()
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
