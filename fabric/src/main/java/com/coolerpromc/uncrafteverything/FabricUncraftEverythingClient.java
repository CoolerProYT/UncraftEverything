package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.command.ModCommands;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public final class FabricUncraftEverythingClient implements ClientModInitializer {
    public void onInitializeClient() {
        UncraftEverythingClient.init();
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, access) ->
                        ModCommands.register(dispatcher, ClientCommandManager::literal));
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> UncraftEverything.close());
    }
}
