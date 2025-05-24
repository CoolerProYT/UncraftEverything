package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.util.BufferUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.List;
import java.util.Map;

public class ResponseConfigPayload {
    public static final ResourceLocation TYPE = new ResourceLocation(UncraftEverything.MODID, "response_config");
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(TYPE,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public final UncraftEverythingConfig.RestrictionType restrictionType;
    public final List<String> restrictedItems;
    public final boolean allowEnchantedItem;
    public final UncraftEverythingConfig.ExperienceType experienceType;
    public final int experience;
    public final boolean allowUnsmithing;
    public final Map<String, Integer> perItemExp;

    public ResponseConfigPayload(UncraftEverythingConfig.RestrictionType restrictionType, List<String> restrictedItems, boolean allowEnchantedItem, UncraftEverythingConfig.ExperienceType experienceType, int experience, boolean allowUnsmithing, Map<String, Integer> perItemExp){
        this.restrictionType = restrictionType;
        this.restrictedItems = restrictedItems;
        this.allowEnchantedItem = allowEnchantedItem;
        this.experienceType = experienceType;
        this.experience = experience;
        this.allowUnsmithing = allowUnsmithing;
        this.perItemExp = perItemExp;
    }

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(ResponseConfigPayload payload, PacketBuffer byteBuf){
        byteBuf.writeEnum(payload.restrictionType);
        BufferUtil.writeStringList(byteBuf, payload.restrictedItems);
        byteBuf.writeBoolean(payload.allowEnchantedItem);
        byteBuf.writeEnum(payload.experienceType);
        byteBuf.writeInt(payload.experience);
        byteBuf.writeBoolean(payload.allowUnsmithing);
        BufferUtil.writeMap(byteBuf, payload.perItemExp);
    }

    public static ResponseConfigPayload decode(PacketBuffer byteBuf){
        UncraftEverythingConfig.RestrictionType restrictionType = byteBuf.readEnum(UncraftEverythingConfig.RestrictionType.class);
        List<String> restrictedItems = BufferUtil.readStringList(byteBuf);
        boolean allowEnchantedItem = byteBuf.readBoolean();
        UncraftEverythingConfig.ExperienceType experienceType = byteBuf.readEnum(UncraftEverythingConfig.ExperienceType.class);
        int experience = byteBuf.readInt();
        boolean allowUnsmithing = byteBuf.readBoolean();
        Map<String, Integer> perItemExp = BufferUtil.readMap(byteBuf);

        return new ResponseConfigPayload(restrictionType, restrictedItems, allowEnchantedItem, experienceType, experience, allowUnsmithing, perItemExp);
    }

    public static void register(){
        INSTANCE.registerMessage(
                nextId(),
                ResponseConfigPayload.class,
                ResponseConfigPayload::encode,
                ResponseConfigPayload::decode,
                FMLEnvironment.dist.isClient() ? ClientPayloadHandler::handleConfigSync : (payload, context) -> {}
        );
    }

    public UncraftEverythingConfig.RestrictionType restrictionType(){
        return restrictionType;
    }

    public List<String> restrictedItems(){
        return restrictedItems;
    }

    public boolean allowEnchantedItem(){
        return allowEnchantedItem;
    }

    public UncraftEverythingConfig.ExperienceType experienceType(){
        return experienceType;
    }

    public int experience(){
        return experience;
    }

    public boolean allowUnsmithing(){
        return allowUnsmithing;
    }

    public Map<String, Integer> perItemExp(){
        return perItemExp;
    }
}