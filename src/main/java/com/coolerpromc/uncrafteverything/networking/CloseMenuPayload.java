package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record CloseMenuPayload(BlockPos pos) implements CustomPayload {
    public static final Id<CloseMenuPayload> TYPE = new Id<>(Identifier.of(UncraftEverything.MODID, "close_menu_payload"));

    public static final PacketCodec<RegistryByteBuf, CloseMenuPayload> STREAM_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC,
            CloseMenuPayload::pos,
            CloseMenuPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}