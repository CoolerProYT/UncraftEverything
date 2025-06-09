package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record ResponseConfigPayload(UncraftEverythingConfig.RestrictionType restrictionType, List<String> restrictedItems, boolean allowEnchantedItem, UncraftEverythingConfig.ExperienceType experienceType, int experience, boolean allowUnsmithing, boolean allowDamaged, Map<String, Integer> perItemExp) {
    public static final Identifier TYPE = new Identifier(UncraftEverything.MODID, "response_config");

    public static PacketByteBuf encode(PacketByteBuf buf, ResponseConfigPayload payload) {
        buf.writeEnumConstant(payload.restrictionType);
        buf.writeCollection(payload.restrictedItems, PacketByteBuf::writeString);
        buf.writeBoolean(payload.allowEnchantedItem);
        buf.writeEnumConstant(payload.experienceType);
        buf.writeInt(payload.experience);
        buf.writeBoolean(payload.allowUnsmithing);
        buf.writeBoolean(payload.allowDamaged);
        buf.writeMap(payload.perItemExp, PacketByteBuf::writeString, PacketByteBuf::writeVarInt);

        return buf;
    }

    public static ResponseConfigPayload decode(PacketByteBuf buf){
        UncraftEverythingConfig.RestrictionType restrictionType = buf.readEnumConstant(UncraftEverythingConfig.RestrictionType.class);
        List<String> restrictedItems = buf.readCollection(ArrayList::new, PacketByteBuf::readString);
        boolean allowEnchantedItem = buf.readBoolean();
        UncraftEverythingConfig.ExperienceType experienceType = buf.readEnumConstant(UncraftEverythingConfig.ExperienceType.class);
        int experience = buf.readInt();
        boolean allowUnsmithing = buf.readBoolean();
        boolean allowDamaged = buf.readBoolean();
        Map<String, Integer> perItemExp = buf.readMap(PacketByteBuf::readString, PacketByteBuf::readVarInt);

        return new ResponseConfigPayload(restrictionType, restrictedItems, allowEnchantedItem, experienceType, experience, allowUnsmithing, allowDamaged, perItemExp);
    }
}