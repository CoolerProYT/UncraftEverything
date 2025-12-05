package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TypeChangePayload(BlockPos blockPos, UncraftEverythingConfig.ExperienceType expType) implements CustomPacketPayload {
    public static final Type<TypeChangePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "type_change_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TypeChangePayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    TypeChangePayload::blockPos,
                    UncraftEverythingConfig.ExperienceType.STREAM_CODEC,
                    TypeChangePayload::expType,
                    TypeChangePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}