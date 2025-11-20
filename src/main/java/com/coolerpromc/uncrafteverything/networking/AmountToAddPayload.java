package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AmountToAddPayload(BlockPos blockPos, int index) implements CustomPacketPayload {
    public static final Type<AmountToAddPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(UncraftEverything.MODID, "amount_to_add_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AmountToAddPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    AmountToAddPayload::blockPos,
                    ByteBufCodecs.INT,
                    AmountToAddPayload::index,
                    AmountToAddPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
