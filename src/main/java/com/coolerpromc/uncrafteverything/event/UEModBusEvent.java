package com.coolerpromc.uncrafteverything.event;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.networking.ServerPayloadHandler;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableCraftButtonClickPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = UncraftEverything.MODID, bus = EventBusSubscriber.Bus.MOD)
public class UEModBusEvent {
    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                UncraftingTableCraftButtonClickPayload.TYPE,
                UncraftingTableCraftButtonClickPayload.STREAM_CODEC,
                ServerPayloadHandler::handleButtonClick
        );
    }
}
