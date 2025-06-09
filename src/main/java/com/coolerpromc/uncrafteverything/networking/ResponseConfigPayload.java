package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ResponseConfigPayload(UncraftEverythingConfig.RestrictionType restrictionType, List<String> restrictedItems, boolean allowEnchantedItem, UncraftEverythingConfig.ExperienceType experienceType, int experience, boolean allowUnsmithing, boolean allowDamaged, Map<String, Integer> perItemExp) implements CustomPacketPayload {
    public static final Type<ResponseConfigPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "response_config"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResponseConfigPayload> STREAM_CODEC = StreamCodec.composite(
            UncraftEverythingConfig.RestrictionType.STREAM_CODEC,
            ResponseConfigPayload::restrictionType,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
            ResponseConfigPayload::restrictedItems,
            ByteBufCodecs.BOOL,
            ResponseConfigPayload::allowEnchantedItem,
            UncraftEverythingConfig.ExperienceType.STREAM_CODEC,
            ResponseConfigPayload::experienceType,
            ByteBufCodecs.INT,
            ResponseConfigPayload::experience,
            ByteBufCodecs.BOOL,
            ResponseConfigPayload::allowUnsmithing,
            ByteBufCodecs.BOOL,
            ResponseConfigPayload::allowDamaged,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT),
            ResponseConfigPayload::perItemExp,
            ResponseConfigPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}