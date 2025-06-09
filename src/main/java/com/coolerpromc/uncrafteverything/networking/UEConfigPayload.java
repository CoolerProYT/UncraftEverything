package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record UEConfigPayload(UncraftEverythingConfig.RestrictionType restrictionType, List<String> restrictedItems, boolean allowEnchantedItem, UncraftEverythingConfig.ExperienceType experienceType, int experience, boolean allowUnsmithing, boolean allowDamaged) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UEConfigPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "ue_config"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UEConfigPayload> STREAM_CODEC = StreamCodec.composite(
            UncraftEverythingConfig.RestrictionType.STREAM_CODEC,
            UEConfigPayload::restrictionType,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
            UEConfigPayload::restrictedItems,
            ByteBufCodecs.BOOL,
            UEConfigPayload::allowEnchantedItem,
            UncraftEverythingConfig.ExperienceType.STREAM_CODEC,
            UEConfigPayload::experienceType,
            ByteBufCodecs.INT,
            UEConfigPayload::experience,
            ByteBufCodecs.BOOL,
            UEConfigPayload::allowUnsmithing,
            ByteBufCodecs.BOOL,
            UEConfigPayload::allowDamaged,
            UEConfigPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}