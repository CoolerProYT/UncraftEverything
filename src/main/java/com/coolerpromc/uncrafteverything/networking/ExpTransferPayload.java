package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ExpTransferPayload(BlockPos pos, int amount, UncraftEverythingConfig.ExperienceType experienceType) implements CustomPacketPayload {
    public static Type<ExpTransferPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "exp_transfer_payload"));

    public static Codec<ExpTransferPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(ExpTransferPayload::pos),
            Codec.INT.fieldOf("amount").forGetter(ExpTransferPayload::amount),
            UncraftEverythingConfig.ExperienceType.CODEC.fieldOf("experienceType").forGetter(ExpTransferPayload::experienceType)
    ).apply(instance, ExpTransferPayload::new));

    public static StreamCodec<RegistryFriendlyByteBuf, ExpTransferPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ExpTransferPayload::pos,
            ByteBufCodecs.INT,
            ExpTransferPayload::amount,
            UncraftEverythingConfig.ExperienceType.STREAM_CODEC,
            ExpTransferPayload::experienceType,
            ExpTransferPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
