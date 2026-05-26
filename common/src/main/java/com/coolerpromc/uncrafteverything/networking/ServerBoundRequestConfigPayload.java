package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.config.FTBQuestProgressionConfig;
import com.coolerpromc.uncrafteverything.config.PerItemExpCostConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.platform.Services;
import com.coolerpromc.uncrafteverything.platform.util.PayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record ServerBoundRequestConfigPayload() implements CustomPacketPayload {
    public static final Type<ServerBoundRequestConfigPayload> TYPE = new Type<>(Constants.id("request_config"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundRequestConfigPayload> STREAM_CODEC = StreamCodec.ofMember((buffer, value) -> {}, buffer -> new ServerBoundRequestConfigPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void handle(PayloadContext context){
        context.execute(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Services.NETWORK.sendToPlayer(player, new ClientBoundResponseConfigPayload(
                        UncraftEverythingConfig.restrictionType,
                        UncraftEverythingConfig.restrictions,
                        UncraftEverythingConfig.allowEnchantedItems,
                        UncraftEverythingConfig.experienceType,
                        UncraftEverythingConfig.experience,
                        UncraftEverythingConfig.allowUnSmithing,
                        UncraftEverythingConfig.allowDamaged,
                        UncraftEverythingConfig.preventModdedIngredientsFromVanillaItems,
                        PerItemExpCostConfig.getPerItemExp(),
                        UncraftEverythingConfig.restrictedModIngredients,
                        FTBQuestProgressionConfig.getProgressionMap(),
                        UncraftEverythingConfig.enableProgression,
                        UncraftEverythingConfig.onlyAllowDefinedProgression,
                        UncraftEverythingConfig.outputEnchantedBook,
                        UncraftEverythingConfig.prioritizeVanillaIngredientRecipe,
                        UncraftEverythingConfig.restrictAmbiguouslyCraftedItems
                ));
            }
        });
    }
}