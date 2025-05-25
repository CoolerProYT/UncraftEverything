package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

public record UEConfigPayload(UncraftEverythingConfig.RestrictionType restrictionType, List<String> restrictedItems, boolean allowEnchantedItem, UncraftEverythingConfig.ExperienceType experienceType, int experience, boolean allowUnsmithing) implements CustomPayload {
    public static final Id<UEConfigPayload> TYPE = new Id<>(Identifier.of(UncraftEverything.MODID, "ue_config"));

    public static final PacketCodec<RegistryByteBuf, UEConfigPayload> STREAM_CODEC = PacketCodec.tuple(
            UncraftEverythingConfig.RestrictionType.STREAM_CODEC,
            UEConfigPayload::restrictionType,
            PacketCodecs.STRING.collect(PacketCodecs.toList()),
            UEConfigPayload::restrictedItems,
            PacketCodecs.BOOL,
            UEConfigPayload::allowEnchantedItem,
            UncraftEverythingConfig.ExperienceType.STREAM_CODEC,
            UEConfigPayload::experienceType,
            PacketCodecs.INTEGER,
            UEConfigPayload::experience,
            PacketCodecs.BOOL,
            UEConfigPayload::allowUnsmithing,
            UEConfigPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}