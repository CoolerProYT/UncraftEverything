package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record UncraftingRecipeSelectionRequestPayload() implements CustomPayload {
    public static final Id<UncraftingRecipeSelectionRequestPayload> TYPE = new Id<>(Identifier.of(UncraftEverything.MODID, "uncrafting_table_recipe_selection_request"));

    public static final PacketCodec<RegistryByteBuf, UncraftingRecipeSelectionRequestPayload> STREAM_CODEC =
            PacketCodec.ofStatic(
                    (buf, value) -> {

                    },
                    buf -> new UncraftingRecipeSelectionRequestPayload()
            );


    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}