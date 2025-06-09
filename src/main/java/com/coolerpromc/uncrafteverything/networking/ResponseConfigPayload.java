
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

public record ResponseConfigPayload(UncraftEverythingConfig.RestrictionType restrictionType, List<String> restrictedItems, boolean allowEnchantedItem, UncraftEverythingConfig.ExperienceType experienceType, int experience, boolean allowUnsmithing, boolean allowDamaged, Map<String, Integer> perItemExp) implements CustomPayload {
    public static final Id<ResponseConfigPayload> TYPE = new Id<>(Identifier.of(UncraftEverything.MODID, "response_config"));

    public static final PacketCodec<RegistryByteBuf, ResponseConfigPayload> STREAM_CODEC = PacketCodec.ofStatic(ResponseConfigPayload::encode, ResponseConfigPayload::decode);

    private static void encode(RegistryByteBuf buf, ResponseConfigPayload payload) {
        UncraftEverythingConfig.RestrictionType.STREAM_CODEC.encode(buf, payload.restrictionType);
        PacketCodecs.STRING.collect(PacketCodecs.toList()).encode(buf, payload.restrictedItems);
        PacketCodecs.BOOL.encode(buf, payload.allowEnchantedItem);
        UncraftEverythingConfig.ExperienceType.STREAM_CODEC.encode(buf, payload.experienceType);
        PacketCodecs.INTEGER.encode(buf, payload.experience);
        PacketCodecs.BOOL.encode(buf, payload.allowUnsmithing);
        PacketCodecs.BOOL.encode(buf, payload.allowDamaged);
        PacketCodecs.map(HashMap::new, PacketCodecs.STRING, PacketCodecs.VAR_INT).encode(buf, new HashMap<>(payload.perItemExp));
    }

    private static ResponseConfigPayload decode(RegistryByteBuf buf){
        UncraftEverythingConfig.RestrictionType restrictionType = UncraftEverythingConfig.RestrictionType.STREAM_CODEC.decode(buf);
        List<String> restrictedItems = PacketCodecs.STRING.collect(PacketCodecs.toList()).decode(buf);
        boolean allowEnchantedItem = PacketCodecs.BOOL.decode(buf);
        UncraftEverythingConfig.ExperienceType experienceType = UncraftEverythingConfig.ExperienceType.STREAM_CODEC.decode(buf);
        int experience = PacketCodecs.INTEGER.decode(buf);
        boolean allowUnsmithing = PacketCodecs.BOOL.decode(buf);
        boolean allowDamaged = PacketCodecs.BOOL.decode(buf);
        Map<String, Integer> perItemExp = PacketCodecs.map(HashMap::new, PacketCodecs.STRING, PacketCodecs.VAR_INT).decode(buf);

        return new ResponseConfigPayload(restrictionType, restrictedItems, allowEnchantedItem, experienceType, experience, allowUnsmithing, allowDamaged, perItemExp);
    }


    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}