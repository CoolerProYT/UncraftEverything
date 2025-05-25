package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ResponseConfigPayload(UncraftEverythingConfig.RestrictionType restrictionType, List<String> restrictedItems, boolean allowEnchantedItem, UncraftEverythingConfig.ExperienceType experienceType, int experience, boolean allowUnsmithing, Map<String, Integer> perItemExp) implements CustomPayload {
    public static final Id<ResponseConfigPayload> TYPE = new Id<>(Identifier.of(UncraftEverything.MODID, "response_config"));

    public static final PacketCodec<RegistryByteBuf, ResponseConfigPayload> STREAM_CODEC = PacketCodec.tuple(
            UncraftEverythingConfig.RestrictionType.STREAM_CODEC,
            ResponseConfigPayload::restrictionType,
            PacketCodecs.STRING.collect(PacketCodecs.toList()),
            ResponseConfigPayload::restrictedItems,
            PacketCodecs.BOOLEAN,
            ResponseConfigPayload::allowEnchantedItem,
            UncraftEverythingConfig.ExperienceType.STREAM_CODEC,
            ResponseConfigPayload::experienceType,
            PacketCodecs.INTEGER,
            ResponseConfigPayload::experience,
            PacketCodecs.BOOLEAN,
            ResponseConfigPayload::allowUnsmithing,
            PacketCodecs.map(HashMap::new, PacketCodecs.STRING, PacketCodecs.VAR_INT),
            ResponseConfigPayload::perItemExp,
            ResponseConfigPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}