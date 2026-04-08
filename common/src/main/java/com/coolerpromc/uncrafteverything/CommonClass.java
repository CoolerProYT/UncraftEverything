package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.config.FTBQuestProgressionConfig;
import com.coolerpromc.uncrafteverything.config.PerItemExpCostConfig;
import com.coolerpromc.uncrafteverything.item.UECreativeTab;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;

public class CommonClass {
    public static boolean AUTO_MOVE = true;

    public static void init() {
        UEBlocks.load();
        UEBlockEntities.load();
        UECreativeTab.load();
        UEMenuTypes.load();

        PerItemExpCostConfig.load();
        FTBQuestProgressionConfig.load();
        PerItemExpCostConfig.startWatcher();
        FTBQuestProgressionConfig.startWatcher();
    }
}