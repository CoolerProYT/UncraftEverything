package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.command.ModCommands;
import com.coolerpromc.uncrafteverything.platform.*;

import net.minecraft.commands.Commands;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(UncraftEverything.MODID)
public final class ForgeUncraftEverything {
    public ForgeUncraftEverything() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        UncraftEverything.init();
        ((ForgeRegistryHelper) Services.REGISTRY).attach(bus);
        bus.addListener(this::setup);
        MinecraftForge.EVENT_BUS.addListener(this::starting);
        MinecraftForge.EVENT_BUS.addListener(this::stopping);
        MinecraftForge.EVENT_BUS.addListener(this::sync);
        if (Services.PLATFORM.isClient()) Client.register(bus);
    }

    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(UncraftEverything::registerPackets);
    }

    private void starting(ServerStartingEvent event) {
        UncraftEverything.startWatchers();
    }

    private void stopping(ServerStoppingEvent event) {
        UncraftEverything.stopWatchers();
    }

    private void sync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) UncraftEverything.syncConfig(event.getPlayer());
    }

    private static final class Client {
        static void register(net.minecraftforge.eventbus.api.IEventBus bus) {
            bus.addListener(Client::setup);
            MinecraftForge.EVENT_BUS.addListener(Client::commands);
            MinecraftForge.EVENT_BUS.addListener(Client::logout);
        }

        static void setup(FMLClientSetupEvent event) {
            event.enqueueWork(UncraftEverythingClient::init);
        }

        static void commands(RegisterClientCommandsEvent event) {
            ModCommands.register(event.getDispatcher(), Commands::literal);
        }

        static void logout(net.minecraftforge.event.GameShuttingDownEvent event) {
            UncraftEverything.close();
        }
    }
}
