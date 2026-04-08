package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.platform.util.PayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public record ServerBoundUEConfigPayload(
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
) implements CustomPacketPayload {

    public static final Type<ServerBoundUEConfigPayload> TYPE = new Type<>(Constants.id("ue_config"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundUEConfigPayload> STREAM_CODEC = StreamCodec.of(ServerBoundUEConfigPayload::encode, ServerBoundUEConfigPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, ServerBoundUEConfigPayload payload) {
        UncraftEverythingConfig.RestrictionType.STREAM_CODEC.encode(buf, payload.restrictionType);
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).encode(buf, payload.restrictedItems);
        ByteBufCodecs.BOOL.encode(buf, payload.allowEnchantedItem);
        UncraftEverythingConfig.ExperienceType.STREAM_CODEC.encode(buf, payload.experienceType);
        ByteBufCodecs.INT.encode(buf, payload.experience);
        ByteBufCodecs.BOOL.encode(buf, payload.allowUnsmithing);
        ByteBufCodecs.BOOL.encode(buf, payload.allowDamaged);
        ByteBufCodecs.BOOL.encode(buf, payload.preventModdedIngredientsFromVanillaItems);
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).encode(buf, payload.restrictedModIngredients);
        ByteBufCodecs.BOOL.encode(buf, payload.enableProgression);
        ByteBufCodecs.BOOL.encode(buf, payload.onlyAllowDefinedProgression);
        ByteBufCodecs.BOOL.encode(buf, payload.outputEnchantedBook);
        ByteBufCodecs.BOOL.encode(buf, payload.prioritizeVanillaIngredientRecipe);
    }

    private static ServerBoundUEConfigPayload decode(RegistryFriendlyByteBuf buf){
        UncraftEverythingConfig.RestrictionType restrictionType = UncraftEverythingConfig.RestrictionType.STREAM_CODEC.decode(buf);
        List<String> restrictedItems = ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).decode(buf);
        boolean allowEnchantedItem = ByteBufCodecs.BOOL.decode(buf);
        UncraftEverythingConfig.ExperienceType experienceType = UncraftEverythingConfig.ExperienceType.STREAM_CODEC.decode(buf);
        int experience = ByteBufCodecs.INT.decode(buf);
        boolean allowUnsmithing = ByteBufCodecs.BOOL.decode(buf);
        boolean allowDamaged = ByteBufCodecs.BOOL.decode(buf);
        boolean preventModdedIngredientsFromVanillaItems = ByteBufCodecs.BOOL.decode(buf);
        List<String> restrictedModIngredients = ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).decode(buf);
        boolean enableProgression = ByteBufCodecs.BOOL.decode(buf);
        boolean onlyAllowDefinedProgression = ByteBufCodecs.BOOL.decode(buf);
        boolean outputEnchantedBook = ByteBufCodecs.BOOL.decode(buf);
        boolean prioritizeVanillaIngredientRecipe = ByteBufCodecs.BOOL.decode(buf);

        return new ServerBoundUEConfigPayload(restrictionType, restrictedItems, allowEnchantedItem, experienceType, experience, allowUnsmithing, allowDamaged, preventModdedIngredientsFromVanillaItems, restrictedModIngredients, enableProgression, onlyAllowDefinedProgression, outputEnchantedBook, prioritizeVanillaIngredientRecipe);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void handle(PayloadContext context){
        context.execute(() -> {
            if (context.player() instanceof ServerPlayer) {
                UncraftEverythingConfig.restrictionType = this.restrictionType();
                UncraftEverythingConfig.restrictions = this.restrictedItems();
                UncraftEverythingConfig.allowEnchantedItems = this.allowEnchantedItem();
                UncraftEverythingConfig.experienceType = this.experienceType();
                UncraftEverythingConfig.experience = this.experience();
                UncraftEverythingConfig.allowUnSmithing = this.allowUnsmithing();
                UncraftEverythingConfig.allowDamaged = this.allowDamaged();
                UncraftEverythingConfig.preventModdedIngredientsFromVanillaItems = this.preventModdedIngredientsFromVanillaItems();
                UncraftEverythingConfig.restrictedModIngredients = this.restrictedModIngredients();
                UncraftEverythingConfig.enableProgression = this.enableProgression();
                UncraftEverythingConfig.onlyAllowDefinedProgression = this.onlyAllowDefinedProgression();
                UncraftEverythingConfig.outputEnchantedBook = this.outputEnchantedBook();
                UncraftEverythingConfig.prioritizeVanillaIngredientRecipe = this.prioritizeVanillaIngredientRecipe();
            }
        });
    }
}