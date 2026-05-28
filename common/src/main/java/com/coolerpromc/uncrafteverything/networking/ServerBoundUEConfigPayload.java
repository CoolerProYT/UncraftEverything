package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.UncraftEverything;
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
        boolean prioritizeVanillaIngredientRecipe,
        boolean restrictAmbiguouslyCraftedItems,
        boolean allowDamagedNonRepairable,
        double minimumDurability
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
        ByteBufCodecs.BOOL.encode(buf, payload.restrictAmbiguouslyCraftedItems);
        ByteBufCodecs.BOOL.encode(buf, payload.allowDamagedNonRepairable);
        ByteBufCodecs.DOUBLE.encode(buf, payload.minimumDurability);
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
        boolean restrictAmbiguouslyCraftedItems = ByteBufCodecs.BOOL.decode(buf);
        boolean allowDamagedNonRepairable = ByteBufCodecs.BOOL.decode(buf);
        double minimumDurability = ByteBufCodecs.DOUBLE.decode(buf);

        return new ServerBoundUEConfigPayload(restrictionType, restrictedItems, allowEnchantedItem, experienceType, experience, allowUnsmithing, allowDamaged, preventModdedIngredientsFromVanillaItems, restrictedModIngredients, enableProgression, onlyAllowDefinedProgression, outputEnchantedBook, prioritizeVanillaIngredientRecipe, restrictAmbiguouslyCraftedItems, allowDamagedNonRepairable, minimumDurability);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void handle(PayloadContext context){
        context.execute(() -> {
            if (context.player() instanceof ServerPlayer) {
                UncraftEverythingConfig.CONFIG.restrictionType.set(this.restrictionType());
                UncraftEverythingConfig.CONFIG.restrictions.set(this.restrictedItems());
                UncraftEverythingConfig.CONFIG.allowEnchantedItems.set(this.allowEnchantedItem());
                UncraftEverythingConfig.CONFIG.experienceType.set(this.experienceType());
                UncraftEverythingConfig.CONFIG.experience.set(this.experience());
                UncraftEverythingConfig.CONFIG.allowUnSmithing.set(this.allowUnsmithing());
                UncraftEverythingConfig.CONFIG.allowDamaged.set(this.allowDamaged());
                UncraftEverythingConfig.CONFIG.preventModdedIngredientsFromVanillaItems.set(this.preventModdedIngredientsFromVanillaItems());
                UncraftEverythingConfig.CONFIG.restrictedModIngredients.set(this.restrictedModIngredients());
                UncraftEverythingConfig.CONFIG.enableProgression.set(this.enableProgression());
                UncraftEverythingConfig.CONFIG.onlyAllowDefinedProgression.set(this.onlyAllowDefinedProgression());
                UncraftEverythingConfig.CONFIG.outputEnchantedBook.set(this.outputEnchantedBook());
                UncraftEverythingConfig.CONFIG.prioritizeVanillaIngredientRecipe.set(this.prioritizeVanillaIngredientRecipe());
                UncraftEverythingConfig.CONFIG.restrictAmbiguouslyCraftedItems.set(this.restrictAmbiguouslyCraftedItems());
                UncraftEverythingConfig.CONFIG.allowDamagedNonRepairable.set(this.allowDamagedNonRepairable());
                UncraftEverythingConfig.CONFIG.minimumDurability.set(this.minimumDurability());
                UncraftEverythingConfig.CONFIG.save();
            }
        });
    }
}