package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UncraftingPageChangePayload(int page, BlockPos blockPos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UncraftingPageChangePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "uncrafting_recipe_selection_data_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UncraftingPageChangePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            UncraftingPageChangePayload::page,
            BlockPos.STREAM_CODEC,
            UncraftingPageChangePayload::blockPos,
            UncraftingPageChangePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
