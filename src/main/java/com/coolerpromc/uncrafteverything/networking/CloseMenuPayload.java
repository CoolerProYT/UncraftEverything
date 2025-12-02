package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CloseMenuPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<CloseMenuPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(UncraftEverything.MODID, "close_menu_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CloseMenuPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            CloseMenuPayload::pos,
            CloseMenuPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
