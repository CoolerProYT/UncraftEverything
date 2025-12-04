package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record TypeChangePayload(BlockPos blockPos, UncraftEverythingConfig.ExperienceType expType) implements CustomPayload {
    public static final Id<TypeChangePayload> TYPE = new Id<>(Identifier.of(UncraftEverything.MODID, "type_change_payload"));

    public static final PacketCodec<RegistryByteBuf, TypeChangePayload> STREAM_CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC,
                    TypeChangePayload::blockPos,
                    UncraftEverythingConfig.ExperienceType.STREAM_CODEC,
                    TypeChangePayload::expType,
                    TypeChangePayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}