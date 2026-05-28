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
    ClientBoundResponseConfigPayload SYNC_CONFIG = new ClientBoundResponseConfigPayload(
            UncraftEverythingConfig.CONFIG.restrictionType(),
            UncraftEverythingConfig.CONFIG.restrictions(),
            UncraftEverythingConfig.CONFIG.allowEnchantedItems(),
            UncraftEverythingConfig.CONFIG.experienceType(),
            UncraftEverythingConfig.CONFIG.experience(),
            UncraftEverythingConfig.CONFIG.allowUnSmithing(),
            UncraftEverythingConfig.CONFIG.allowDamaged(),
            UncraftEverythingConfig.CONFIG.preventModdedIngredientsFromVanillaItems(),
            PerItemExpCostConfig.CONFIG.getPerItemExp(),
            UncraftEverythingConfig.CONFIG.restrictedModIngredients(),
            FTBQuestProgressionConfig.CONFIG.getProgressionMap(),
            UncraftEverythingConfig.CONFIG.enableProgression(),
            UncraftEverythingConfig.CONFIG.onlyAllowDefinedProgression(),
            UncraftEverythingConfig.CONFIG.outputEnchantedBook(),
            UncraftEverythingConfig.CONFIG.prioritizeVanillaIngredientRecipe(),
            UncraftEverythingConfig.CONFIG.restrictAmbiguouslyCraftedItems(),
            UncraftEverythingConfig.CONFIG.allowDamagedNonRepairable(),
            UncraftEverythingConfig.CONFIG.minimumDurability()
    );

    Player player();
    Level level();
    void execute(Runnable runnable);
    void disconnect(Component reason);
    
    static void syncConfig(ServerPlayer player){
        ClientBoundResponseConfigPayload configPayload = new ClientBoundResponseConfigPayload(
                UncraftEverythingConfig.CONFIG.restrictionType(),
                UncraftEverythingConfig.CONFIG.restrictions(),
                UncraftEverythingConfig.CONFIG.allowEnchantedItems(),
                UncraftEverythingConfig.CONFIG.experienceType(),
                UncraftEverythingConfig.CONFIG.experience(),
                UncraftEverythingConfig.CONFIG.allowUnSmithing(),
                UncraftEverythingConfig.CONFIG.allowDamaged(),
                UncraftEverythingConfig.CONFIG.preventModdedIngredientsFromVanillaItems(),
                PerItemExpCostConfig.CONFIG.getPerItemExp(),
                UncraftEverythingConfig.CONFIG.restrictedModIngredients(),
                FTBQuestProgressionConfig.CONFIG.getProgressionMap(),
                UncraftEverythingConfig.CONFIG.enableProgression(),
                UncraftEverythingConfig.CONFIG.onlyAllowDefinedProgression(),
                UncraftEverythingConfig.CONFIG.outputEnchantedBook(),
                UncraftEverythingConfig.CONFIG.prioritizeVanillaIngredientRecipe(),
                UncraftEverythingConfig.CONFIG.restrictAmbiguouslyCraftedItems(),
                UncraftEverythingConfig.CONFIG.allowDamagedNonRepairable(),
                UncraftEverythingConfig.CONFIG.minimumDurability()
        );
        Services.NETWORK.sendToPlayer(player, configPayload);
    }
}