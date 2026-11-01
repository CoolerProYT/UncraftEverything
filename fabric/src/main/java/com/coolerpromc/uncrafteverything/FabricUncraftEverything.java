package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.platform.*;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.transfer.v1.item.*;

public final class FabricUncraftEverything implements ModInitializer {
    public void onInitialize() {
        UncraftEverything.init();
        UncraftEverything.registerPackets();
        ((FabricRegistryHelper) Services.REGISTRY).registerLegacyTable();
        ItemStorage.SIDED.registerForBlockEntity(
                (table, side) -> InventoryStorage.of(table.automationInventory(side), side),
                UEBlockEntities.UNCRAFTING_TABLE_BE.get());
        ItemStorage.SIDED.registerForBlockEntity(
                (table, side) -> InventoryStorage.of(table.automationInventory(side), side),
                UEBlockEntities.AUTO_UNCRAFTING_TABLE_BE.get());
        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) ->
                        server.execute(() -> UncraftEverything.syncConfig(handler.player)));
        ServerLifecycleEvents.SERVER_STARTING.register(server -> UncraftEverything.startWatchers());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> UncraftEverything.stopWatchers());
    }
}
