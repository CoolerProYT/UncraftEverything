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

    public static final StreamCodec<RegistryFriendlyByteBuf, UEConfigPayload> STREAM_CODEC = StreamCodec.of(UEConfigPayload::encode, UEConfigPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, UEConfigPayload payload) {
        UncraftEverythingConfig.RestrictionType.STREAM_CODEC.encode(buf, payload.restrictionType);
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).encode(buf, payload.restrictedItems);
        ByteBufCodecs.BOOL.encode(buf, payload.allowEnchantedItem);
        UncraftEverythingConfig.ExperienceType.STREAM_CODEC.encode(buf, payload.experienceType);
        ByteBufCodecs.INT.encode(buf, payload.experience);
        ByteBufCodecs.BOOL.encode(buf, payload.allowUnsmithing);
        ByteBufCodecs.BOOL.encode(buf, payload.allowDamaged);
    }

    private static UEConfigPayload decode(RegistryFriendlyByteBuf buf){
        UncraftEverythingConfig.RestrictionType restrictionType = UncraftEverythingConfig.RestrictionType.STREAM_CODEC.decode(buf);
        List<String> restrictedItems = ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).decode(buf);
        boolean allowEnchantedItem = ByteBufCodecs.BOOL.decode(buf);
        UncraftEverythingConfig.ExperienceType experienceType = UncraftEverythingConfig.ExperienceType.STREAM_CODEC.decode(buf);
        int experience = ByteBufCodecs.INT.decode(buf);
        boolean allowUnsmithing = ByteBufCodecs.BOOL.decode(buf);
        boolean allowDamaged = ByteBufCodecs.BOOL.decode(buf);

        return new UEConfigPayload(restrictionType, restrictedItems, allowEnchantedItem, experienceType, experience, allowUnsmithing, allowDamaged);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}