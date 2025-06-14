package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.util.BufferUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.List;

public class UEConfigPayload {
    public static final Identifier TYPE = new Identifier(UncraftEverything.MODID, "ue_config");

    public final UncraftEverythingConfig.RestrictionType restrictionType;
    public final List<String> restrictedItems;
    public final boolean allowEnchantedItem;
    public final UncraftEverythingConfig.ExperienceType experienceType;
    public final int experience;
    public final boolean allowUnsmithing;
    public final boolean allowDamaged;

    public UEConfigPayload(UncraftEverythingConfig.RestrictionType restrictionType, List<String> restrictedItems, boolean allowEnchantedItem, UncraftEverythingConfig.ExperienceType experienceType, int experience, boolean allowUnsmithing, boolean allowDamaged) {
        this.restrictionType = restrictionType;
        this.restrictedItems = restrictedItems;
        this.allowEnchantedItem = allowEnchantedItem;
        this.experienceType = experienceType;
        this.experience = experience;
        this.allowUnsmithing = allowUnsmithing;
        this.allowDamaged = allowDamaged;
    }

    public static PacketByteBuf encode(PacketByteBuf buf, UEConfigPayload payload) {
        buf.writeEnumConstant(payload.restrictionType);
        BufferUtil.writeStringList(buf, payload.restrictedItems);
        buf.writeBoolean(payload.allowEnchantedItem);
        buf.writeEnumConstant(payload.experienceType);
        buf.writeInt(payload.experience);
        buf.writeBoolean(payload.allowUnsmithing);
        buf.writeBoolean(payload.allowDamaged);

        return buf;
    }

    public static UEConfigPayload decode(PacketByteBuf buf){
        UncraftEverythingConfig.RestrictionType restrictionType = buf.readEnumConstant(UncraftEverythingConfig.RestrictionType.class);
        List<String> restrictedItems = BufferUtil.readStringList(buf);
        boolean allowEnchantedItem = buf.readBoolean();
        UncraftEverythingConfig.ExperienceType experienceType = buf.readEnumConstant(UncraftEverythingConfig.ExperienceType.class);
        int experience = buf.readInt();
        boolean allowUnsmithing = buf.readBoolean();
        boolean allowDamaged = buf.readBoolean();

        return new UEConfigPayload(restrictionType, restrictedItems, allowEnchantedItem, experienceType, experience, allowUnsmithing, allowDamaged);
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
}