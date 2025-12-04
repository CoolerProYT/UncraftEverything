package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record ExpTransferPayload(BlockPos pos, int amount, UncraftEverythingConfig.ExperienceType experienceType) implements CustomPayload {
    public static Id<ExpTransferPayload> TYPE = new Id<>(Identifier.of(UncraftEverything.MODID, "exp_transfer_payload"));

    public static Codec<ExpTransferPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(ExpTransferPayload::pos),
            Codec.INT.fieldOf("amount").forGetter(ExpTransferPayload::amount),
            UncraftEverythingConfig.ExperienceType.CODEC.fieldOf("experienceType").forGetter(ExpTransferPayload::experienceType)
    ).apply(instance, ExpTransferPayload::new));

    public static PacketCodec<RegistryByteBuf, ExpTransferPayload> STREAM_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC,
            ExpTransferPayload::pos,
            PacketCodecs.INTEGER,
            ExpTransferPayload::amount,
            UncraftEverythingConfig.ExperienceType.STREAM_CODEC,
            ExpTransferPayload::experienceType,
            ExpTransferPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}