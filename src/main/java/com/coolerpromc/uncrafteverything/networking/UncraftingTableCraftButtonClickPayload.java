package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UncraftingTableCraftButtonClickPayload(BlockPos blockPos, String data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UncraftingTableCraftButtonClickPayload> TYPE =
            new Type<>(new ResourceLocation(UncraftEverything.MODID, "uncrafting_table_craft_button_click"));

    public static final StreamCodec<ByteBuf, UncraftingTableCraftButtonClickPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    UncraftingTableCraftButtonClickPayload::blockPos,
                    ByteBufCodecs.STRING_UTF8,
                    UncraftingTableCraftButtonClickPayload::data,
                    UncraftingTableCraftButtonClickPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
