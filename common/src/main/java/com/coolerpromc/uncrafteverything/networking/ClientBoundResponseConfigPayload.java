package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverythingClient;
import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.platform.util.PayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ClientBoundResponseConfigPayload(
        UncraftEverythingConfig.RestrictionType restrictionType,
        List<String> restrictedItems,
        boolean allowEnchantedItem,
        UncraftEverythingConfig.ExperienceType experienceType,
        int experience,
        boolean allowUnsmithing,
        boolean allowDamaged,
        boolean preventModdedIngredientsFromVanillaItems,
        Map<String, Integer> perItemExp,
        List<String> restrictedModIngredients,
        Map<String, String> ftbQuestProgression,
        boolean enableProgression,
        boolean onlyAllowDefinedProgression,
        boolean outputEnchantedBook,
        boolean prioritizeVanillaIngredientRecipe,
        boolean restrictAmbiguouslyCraftedItems
) implements CustomPacketPayload {

    public static final Type<ClientBoundResponseConfigPayload> TYPE = new Type<>(Constants.id("response_config"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundResponseConfigPayload> STREAM_CODEC = StreamCodec.of(ClientBoundResponseConfigPayload::encode, ClientBoundResponseConfigPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, ClientBoundResponseConfigPayload payload) {
        UncraftEverythingConfig.RestrictionType.STREAM_CODEC.encode(buf, payload.restrictionType);
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).encode(buf, payload.restrictedItems);
        ByteBufCodecs.BOOL.encode(buf, payload.allowEnchantedItem);
        UncraftEverythingConfig.ExperienceType.STREAM_CODEC.encode(buf, payload.experienceType);
        ByteBufCodecs.INT.encode(buf, payload.experience);
        ByteBufCodecs.BOOL.encode(buf, payload.allowUnsmithing);
        ByteBufCodecs.BOOL.encode(buf, payload.allowDamaged);
        ByteBufCodecs.BOOL.encode(buf, payload.preventModdedIngredientsFromVanillaItems);
        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT).encode(buf, new HashMap<>(payload.perItemExp));
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).encode(buf, payload.restrictedModIngredients);
        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8).encode(buf, new HashMap<>(payload.ftbQuestProgression));
        ByteBufCodecs.BOOL.encode(buf, payload.enableProgression);
        ByteBufCodecs.BOOL.encode(buf, payload.onlyAllowDefinedProgression);
        ByteBufCodecs.BOOL.encode(buf, payload.outputEnchantedBook);
        ByteBufCodecs.BOOL.encode(buf, payload.prioritizeVanillaIngredientRecipe);
        ByteBufCodecs.BOOL.encode(buf, payload.restrictAmbiguouslyCraftedItems);
    }

    private static ClientBoundResponseConfigPayload decode(RegistryFriendlyByteBuf buf){
        UncraftEverythingConfig.RestrictionType restrictionType = UncraftEverythingConfig.RestrictionType.STREAM_CODEC.decode(buf);
        List<String> restrictedItems = ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).decode(buf);
        boolean allowEnchantedItem = ByteBufCodecs.BOOL.decode(buf);
        UncraftEverythingConfig.ExperienceType experienceType = UncraftEverythingConfig.ExperienceType.STREAM_CODEC.decode(buf);
        int experience = ByteBufCodecs.INT.decode(buf);
        boolean allowUnsmithing = ByteBufCodecs.BOOL.decode(buf);
        boolean allowDamaged = ByteBufCodecs.BOOL.decode(buf);
        boolean preventModdedIngredientsFromVanillaItems = ByteBufCodecs.BOOL.decode(buf);
        Map<String, Integer> perItemExp = ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT).decode(buf);
        List<String> restrictedModIngredients = ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).decode(buf);
        Map<String, String> ftbQuestProgression = ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8).decode(buf);
        boolean enableProgression = ByteBufCodecs.BOOL.decode(buf);
        boolean onlyAllowDefinedProgression = ByteBufCodecs.BOOL.decode(buf);
        boolean outputEnchantedBook = ByteBufCodecs.BOOL.decode(buf);
        boolean prioritizeVanillaIngredientRecipe = ByteBufCodecs.BOOL.decode(buf);
        boolean restrictAmbiguouslyCraftedItems = ByteBufCodecs.BOOL.decode(buf);

        return new ClientBoundResponseConfigPayload(restrictionType, restrictedItems, allowEnchantedItem, experienceType, experience, allowUnsmithing, allowDamaged, preventModdedIngredientsFromVanillaItems, perItemExp, restrictedModIngredients, ftbQuestProgression, enableProgression, onlyAllowDefinedProgression, outputEnchantedBook, prioritizeVanillaIngredientRecipe, restrictAmbiguouslyCraftedItems);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class ClientHandler{
        public static void handle(ClientBoundResponseConfigPayload payload, PayloadContext context){
            context.execute(() -> UncraftEverythingClient.payloadFromServer = payload);
        }
    }
}