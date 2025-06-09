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

    public static final StreamCodec<RegistryFriendlyByteBuf, ResponseConfigPayload> STREAM_CODEC = StreamCodec.of(ResponseConfigPayload::encode, ResponseConfigPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, ResponseConfigPayload payload) {
        UncraftEverythingConfig.RestrictionType.STREAM_CODEC.encode(buf, payload.restrictionType);
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).encode(buf, payload.restrictedItems);
        ByteBufCodecs.BOOL.encode(buf, payload.allowEnchantedItem);
        UncraftEverythingConfig.ExperienceType.STREAM_CODEC.encode(buf, payload.experienceType);
        ByteBufCodecs.INT.encode(buf, payload.experience);
        ByteBufCodecs.BOOL.encode(buf, payload.allowUnsmithing);
        ByteBufCodecs.BOOL.encode(buf, payload.allowDamaged);
        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT).encode(buf, new HashMap<>(payload.perItemExp));
    }

    private static ResponseConfigPayload decode(RegistryFriendlyByteBuf buf){
        UncraftEverythingConfig.RestrictionType restrictionType = UncraftEverythingConfig.RestrictionType.STREAM_CODEC.decode(buf);
        List<String> restrictedItems = ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).decode(buf);
        boolean allowEnchantedItem = ByteBufCodecs.BOOL.decode(buf);
        UncraftEverythingConfig.ExperienceType experienceType = UncraftEverythingConfig.ExperienceType.STREAM_CODEC.decode(buf);
        int experience = ByteBufCodecs.INT.decode(buf);
        boolean allowUnsmithing = ByteBufCodecs.BOOL.decode(buf);
        boolean allowDamaged = ByteBufCodecs.BOOL.decode(buf);
        Map<String, Integer> perItemExp = ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT).decode(buf);

        return new ResponseConfigPayload(restrictionType, restrictedItems, allowEnchantedItem, experienceType, experience, allowUnsmithing, allowDamaged, perItemExp);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}