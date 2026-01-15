package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

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
        boolean onlyAllowDefinedProgression,
        boolean outputEnchantedBook,
        boolean prioritizeVanillaIngredientRecipe
) implements CustomPayload {

    public static final Id<UEConfigPayload> TYPE = new Id<>(Identifier.of(UncraftEverything.MODID, "ue_config"));

    public static final PacketCodec<RegistryByteBuf, UEConfigPayload> STREAM_CODEC = PacketCodec.ofStatic(UEConfigPayload::encode, UEConfigPayload::decode);

    private static void encode(RegistryByteBuf buf, UEConfigPayload payload) {
        UncraftEverythingConfig.RestrictionType.STREAM_CODEC.encode(buf, payload.restrictionType);
        PacketCodecs.STRING.collect(PacketCodecs.toList()).encode(buf, payload.restrictedItems);
        PacketCodecs.BOOLEAN.encode(buf, payload.allowEnchantedItem);
        UncraftEverythingConfig.ExperienceType.STREAM_CODEC.encode(buf, payload.experienceType);
        PacketCodecs.INTEGER.encode(buf, payload.experience);
        PacketCodecs.BOOLEAN.encode(buf, payload.allowUnsmithing);
        PacketCodecs.BOOLEAN.encode(buf, payload.allowDamaged);
        PacketCodecs.BOOLEAN.encode(buf, payload.preventModdedIngredientsFromVanillaItems);
        PacketCodecs.STRING.collect(PacketCodecs.toList()).encode(buf, payload.restrictedModIngredients);
        PacketCodecs.BOOLEAN.encode(buf, payload.enableProgression);
        PacketCodecs.BOOLEAN.encode(buf, payload.onlyAllowDefinedProgression);
        PacketCodecs.BOOLEAN.encode(buf, payload.outputEnchantedBook);
        PacketCodecs.BOOLEAN.encode(buf, payload.prioritizeVanillaIngredientRecipe);
    }

    private static UEConfigPayload decode(RegistryByteBuf buf){
        UncraftEverythingConfig.RestrictionType restrictionType = UncraftEverythingConfig.RestrictionType.STREAM_CODEC.decode(buf);
        List<String> restrictedItems = PacketCodecs.STRING.collect(PacketCodecs.toList()).decode(buf);
        boolean allowEnchantedItem = PacketCodecs.BOOLEAN.decode(buf);
        UncraftEverythingConfig.ExperienceType experienceType = UncraftEverythingConfig.ExperienceType.STREAM_CODEC.decode(buf);
        int experience = PacketCodecs.INTEGER.decode(buf);
        boolean allowUnsmithing = PacketCodecs.BOOLEAN.decode(buf);
        boolean allowDamaged = PacketCodecs.BOOLEAN.decode(buf);
        boolean preventModdedIngredientsFromVanillaItems = PacketCodecs.BOOLEAN.decode(buf);
        List<String> restrictedModIngredients = PacketCodecs.STRING.collect(PacketCodecs.toList()).decode(buf);
        boolean enableProgression = PacketCodecs.BOOLEAN.decode(buf);
        boolean onlyAllowDefinedProgression = PacketCodecs.BOOLEAN.decode(buf);
        boolean outputEnchantedBook = PacketCodecs.BOOLEAN.decode(buf);
        boolean prioritizeVanillaIngredientRecipe = PacketCodecs.BOOLEAN.decode(buf);

        return new UEConfigPayload(restrictionType, restrictedItems, allowEnchantedItem, experienceType, experience, allowUnsmithing, allowDamaged, preventModdedIngredientsFromVanillaItems, restrictedModIngredients, enableProgression, onlyAllowDefinedProgression, outputEnchantedBook, prioritizeVanillaIngredientRecipe);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}