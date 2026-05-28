package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.config.FTBQuestProgressionConfig;
import com.coolerpromc.uncrafteverything.config.PerItemExpCostConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingClientConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.item.UECreativeTab;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class UncraftEverything {
    public static boolean AUTO_MOVE = true;

    public static void init() {
        UEBlocks.load();
        UEBlockEntities.load();
        UECreativeTab.load();
        UEMenuTypes.load();

        UncraftEverythingConfig.init();
        UncraftEverythingClientConfig.init();
        PerItemExpCostConfig.init();
        FTBQuestProgressionConfig.init();
    }

    public static void onPlayerLogin(Player player){
        if (UncraftEverythingConfig.CONFIG.restrictAmbiguouslyCraftedItems()){
            player.sendSystemMessage(Component.literal("[Uncraft Everything] Restrict Ambiguously Crafted Items config is enabled, recipe that use ItemTags ingredient will not be able uncrafted!"));
        }
    }
}