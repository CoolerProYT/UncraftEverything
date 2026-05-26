package com.coolerpromc.uncrafteverything.platform.util;

import com.coolerpromc.uncrafteverything.config.FTBQuestProgressionConfig;
import com.coolerpromc.uncrafteverything.config.PerItemExpCostConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.ClientBoundResponseConfigPayload;
import com.coolerpromc.uncrafteverything.platform.Services;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface PayloadContext {
    Player player();
    Level level();
    void execute(Runnable runnable);
    void disconnect(Component reason);
    
    static void syncConfig(ServerPlayer player){
        ClientBoundResponseConfigPayload configPayload = new ClientBoundResponseConfigPayload(
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
        );
        Services.NETWORK.sendToPlayer(player, configPayload);
    }
}