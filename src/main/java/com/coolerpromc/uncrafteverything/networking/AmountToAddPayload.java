package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record AmountToAddPayload(BlockPos blockPos, int index) implements CustomPayload {
    public static final Id<AmountToAddPayload> TYPE = new Id<>(Identifier.of(UncraftEverything.MODID, "amount_to_add_payload"));

    public static final PacketCodec<RegistryByteBuf, AmountToAddPayload> STREAM_CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC,
                    AmountToAddPayload::blockPos,
                    PacketCodecs.INTEGER,
                    AmountToAddPayload::index,
                    AmountToAddPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}