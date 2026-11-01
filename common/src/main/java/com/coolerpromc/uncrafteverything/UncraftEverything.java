package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.config.*;
import com.coolerpromc.uncrafteverything.item.UECreativeTab;
import com.coolerpromc.uncrafteverything.networking.*;
import com.coolerpromc.uncrafteverything.platform.Services;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;

import net.minecraft.server.level.ServerPlayer;

public final class UncraftEverything {
    public static final String MODID = "uncrafteverything";

    public static void init() {
        UncraftEverythingConfig.CONFIG_SPEC.load(
                Services.PLATFORM.configPath().resolve(Services.PLATFORM.commonConfigName()),
                () -> {});
        UEBlocks.register();
        UEBlockEntities.register();
        UEMenuTypes.register();
        UECreativeTab.register();
        PerItemExpCostConfig.load();
        FTBQuestProgressionConfig.load();
        startWatchers();
    }

    public static void registerPackets() {
        UncraftingTableDataPayload.register();
        UncraftingRecipeSelectionPayload.register();
        UncraftingTableCraftButtonClickPayload.register();
        RequestConfigPayload.register();
        ResponseConfigPayload.register();
        UEConfigPayload.register();
        UEExpPayload.register();
        UncraftingRecipeSelectionRequestPayload.register();
        UncraftingPageChangePayload.register();
        UEProgressionPayload.register();
        AmountToAddPayload.register();
        ClientConfigSyncPayload.register();
        CloseMenuPayload.register();
        ExpTransferPayload.register();
        SelectedIndexSyncPayload.register();
        TypeChangePayload.register();
    }

    public static void syncConfig(ServerPlayer player) {
        ServerPayloadHandler.handleRequestConfig(
                new RequestConfigPayload(),
                () ->
                        new com.coolerpromc.uncrafteverything.platform.PacketContext(
                                player, player.server, player.connection::disconnect));
    }

    public static void startWatchers() {
        PerItemExpCostConfig.startWatcher();
        FTBQuestProgressionConfig.startWatcher();
    }

    public static void stopWatchers() {
        PerItemExpCostConfig.stopWatcher();
        FTBQuestProgressionConfig.stopWatcher();
    }

    public static void close() {
        stopWatchers();
        UncraftEverythingConfig.CONFIG_SPEC.close();
        UncraftEverythingClientConfig.CONFIG_SPEC.close();
    }
}
