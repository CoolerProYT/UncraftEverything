package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.List;
import java.util.Map;

public record ResponseConfigPayload(UncraftEverythingConfig.RestrictionType restrictionType, List<String> restrictedItems, boolean allowEnchantedItem, UncraftEverythingConfig.ExperienceType experienceType, int experience, boolean allowUnsmithing, boolean allowDamaged, Map<String, Integer> perItemExp) {
    public static final ResourceLocation TYPE = new ResourceLocation(UncraftEverything.MODID, "response_config");
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(TYPE,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(ResponseConfigPayload payload, FriendlyByteBuf byteBuf){
        byteBuf.writeEnum(payload.restrictionType);
        byteBuf.writeCollection(payload.restrictedItems, FriendlyByteBuf::writeUtf);
        byteBuf.writeBoolean(payload.allowEnchantedItem);
        byteBuf.writeEnum(payload.experienceType);
        byteBuf.writeInt(payload.experience);
        byteBuf.writeBoolean(payload.allowUnsmithing);
        byteBuf.writeBoolean(payload.allowDamaged);
        byteBuf.writeMap(payload.perItemExp, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeVarInt);
    }

    public static ResponseConfigPayload decode(FriendlyByteBuf byteBuf){
        UncraftEverythingConfig.RestrictionType restrictionType = byteBuf.readEnum(UncraftEverythingConfig.RestrictionType.class);
        List<String> restrictedItems = byteBuf.readList(FriendlyByteBuf::readUtf);
        boolean allowEnchantedItem = byteBuf.readBoolean();
        UncraftEverythingConfig.ExperienceType experienceType = byteBuf.readEnum(UncraftEverythingConfig.ExperienceType.class);
        int experience = byteBuf.readInt();
        boolean allowUnsmithing = byteBuf.readBoolean();
        boolean allowDamaged = byteBuf.readBoolean();
        Map<String, Integer> perItemExp = byteBuf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readVarInt);

        return new ResponseConfigPayload(restrictionType, restrictedItems, allowEnchantedItem, experienceType, experience, allowUnsmithing, allowDamaged, perItemExp);
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
}