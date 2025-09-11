package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record UEConfigPayload(
        UncraftEverythingConfig.RestrictionType restrictionType,
        List<String> restrictedItems,
        boolean allowEnchantedItem,
        UncraftEverythingConfig.ExperienceType experienceType,
        int experience,
        boolean allowUnsmithing,
        boolean allowDamaged,
        boolean preventModdedIngredientsFromVanillaItems,
        List<String> restrictedModIngredients,
        boolean enableProgression,
        boolean onlyAllowDefinedProgression
) {

    public static final Identifier TYPE = new Identifier(UncraftEverything.MODID, "ue_config");

    public static PacketByteBuf encode(PacketByteBuf buf, UEConfigPayload payload) {
        buf.writeEnumConstant(payload.restrictionType);
        buf.writeCollection(payload.restrictedItems, PacketByteBuf::writeString);
        buf.writeBoolean(payload.allowEnchantedItem);
        buf.writeEnumConstant(payload.experienceType);
        buf.writeInt(payload.experience);
        buf.writeBoolean(payload.allowUnsmithing);
        buf.writeBoolean(payload.allowDamaged);
        buf.writeBoolean(payload.preventModdedIngredientsFromVanillaItems);
        buf.writeCollection(payload.restrictedModIngredients, PacketByteBuf::writeString);
        buf.writeBoolean(payload.enableProgression);
        buf.writeBoolean(payload.onlyAllowDefinedProgression);

        return buf;
    }

    public static UEConfigPayload decode(PacketByteBuf buf){
        UncraftEverythingConfig.RestrictionType restrictionType = buf.readEnumConstant(UncraftEverythingConfig.RestrictionType.class);
        List<String> restrictedItems = buf.readCollection(ArrayList::new, PacketByteBuf::readString);
        boolean allowEnchantedItem = buf.readBoolean();
        UncraftEverythingConfig.ExperienceType experienceType = buf.readEnumConstant(UncraftEverythingConfig.ExperienceType.class);
        int experience = buf.readInt();
        boolean allowUnsmithing = buf.readBoolean();
        boolean allowDamaged = buf.readBoolean();
        boolean preventModdedIngredientsFromVanillaItems = buf.readBoolean();
        List<String> restrictedModIngredients = buf.readCollection(ArrayList::new, PacketByteBuf::readString);
        boolean enableProgression = buf.readBoolean();
        boolean onlyAllowDefinedProgression = buf.readBoolean();

        return new UEConfigPayload(restrictionType, restrictedItems, allowEnchantedItem, experienceType, experience, allowUnsmithing, allowDamaged, preventModdedIngredientsFromVanillaItems, restrictedModIngredients, enableProgression, onlyAllowDefinedProgression);
    }
}