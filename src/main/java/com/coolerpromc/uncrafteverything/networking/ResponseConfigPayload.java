package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.util.BufferUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public class ResponseConfigPayload {
    public static final Identifier TYPE = new Identifier(UncraftEverything.MODID, "response_config");

    public final UncraftEverythingConfig.RestrictionType restrictionType;
    public final List<String> restrictedItems;
    public final boolean allowEnchantedItem;
    public final UncraftEverythingConfig.ExperienceType experienceType;
    public final int experience;
    public final boolean allowUnsmithing;
    public final boolean allowDamaged;
    public final boolean preventModdedIngredientsFromVanillaItems;
    public final Map<String, Integer> perItemExp;
    public final List<String> restrictedModIngredients;
    public final Map<String, String> ftbQuestProgression;
    public final boolean enableProgression;
    public final boolean onlyAllowDefinedProgression;

    public ResponseConfigPayload(UncraftEverythingConfig.RestrictionType restrictionType, List<String> restrictedItems, boolean allowEnchantedItem, UncraftEverythingConfig.ExperienceType experienceType, int experience, boolean allowUnsmithing, boolean allowDamaged, boolean preventModdedIngredientsFromVanillaItems, Map<String, Integer> perItemExp, List<String> restrictedModIngredients, Map<String, String> ftbQuestProgression, boolean enableProgression, boolean onlyAllowDefinedProgression){
        this.restrictionType = restrictionType;
        this.restrictedItems = restrictedItems;
        this.allowEnchantedItem = allowEnchantedItem;
        this.experienceType = experienceType;
        this.experience = experience;
        this.allowUnsmithing = allowUnsmithing;
        this.allowDamaged = allowDamaged;
        this.preventModdedIngredientsFromVanillaItems = preventModdedIngredientsFromVanillaItems;
        this.perItemExp = perItemExp;
        this.restrictedModIngredients = restrictedModIngredients;
        this.ftbQuestProgression = ftbQuestProgression;
        this.enableProgression = enableProgression;
        this.onlyAllowDefinedProgression = onlyAllowDefinedProgression;
    }

    public static PacketByteBuf encode(PacketByteBuf buf, ResponseConfigPayload payload) {
        buf.writeEnumConstant(payload.restrictionType);
        BufferUtil.writeStringList(buf, payload.restrictedItems);
        buf.writeBoolean(payload.allowEnchantedItem);
        buf.writeEnumConstant(payload.experienceType);
        buf.writeInt(payload.experience);
        buf.writeBoolean(payload.allowUnsmithing);
        buf.writeBoolean(payload.allowDamaged);
        buf.writeBoolean(payload.preventModdedIngredientsFromVanillaItems);
        BufferUtil.writeMap(buf, payload.perItemExp);
        BufferUtil.writeStringList(buf, payload.restrictedModIngredients);
        BufferUtil.writeStringMap(buf, payload.ftbQuestProgression);
        buf.writeBoolean(payload.enableProgression);
        buf.writeBoolean(payload.onlyAllowDefinedProgression);
        return buf;
    }

    public static ResponseConfigPayload decode(PacketByteBuf buf){
        UncraftEverythingConfig.RestrictionType restrictionType = buf.readEnumConstant(UncraftEverythingConfig.RestrictionType.class);
        List<String> restrictedItems = BufferUtil.readStringList(buf);
        boolean allowEnchantedItem = buf.readBoolean();
        UncraftEverythingConfig.ExperienceType experienceType = buf.readEnumConstant(UncraftEverythingConfig.ExperienceType.class);
        int experience = buf.readInt();
        boolean allowUnsmithing = buf.readBoolean();
        boolean allowDamaged = buf.readBoolean();
        boolean preventModdedIngredientsFromVanillaItems = buf.readBoolean();
        Map<String, Integer> perItemExp = BufferUtil.readMap(buf);
        List<String> restrictedModIngredients = BufferUtil.readStringList(buf);
        Map<String, String> ftbQuestProgression = BufferUtil.readStringMap(buf);
        boolean enableProgression = buf.readBoolean();
        boolean onlyAllowDefinedProgression = buf.readBoolean();

        return new ResponseConfigPayload(restrictionType, restrictedItems, allowEnchantedItem, experienceType, experience, allowUnsmithing, allowDamaged, preventModdedIngredientsFromVanillaItems, perItemExp, restrictedModIngredients, ftbQuestProgression, enableProgression, onlyAllowDefinedProgression);
    }

    public UncraftEverythingConfig.RestrictionType restrictionType() {
        return restrictionType;
    }
    public List<String> restrictedItems() {
        return restrictedItems;
    }
    public boolean allowEnchantedItem() {
        return allowEnchantedItem;
    }
    public UncraftEverythingConfig.ExperienceType experienceType() {
        return experienceType;
    }
    public int experience() {
        return experience;
    }
    public boolean allowUnsmithing() {
        return allowUnsmithing;
    }
    public boolean allowDamaged() {
        return allowDamaged;
    }
    public Map<String, Integer> perItemExp() {
        return perItemExp;
    }
    public boolean preventModdedIngredientsFromVanillaItems() {
        return preventModdedIngredientsFromVanillaItems;
    }
    public List<String> restrictedModIngredients() {
        return restrictedModIngredients;
    }
    public Map<String, String> ftbQuestProgression(){
        return ftbQuestProgression;
    }
    public boolean enableProgression(){
        return enableProgression;
    }
    public boolean onlyAllowDefinedProgression(){
        return onlyAllowDefinedProgression;
    }
}