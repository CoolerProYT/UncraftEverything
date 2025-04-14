package com.coolerpromc.uncrafteverything.event;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = UncraftEverything.MODID, bus = EventBusSubscriber.Bus.MOD)
public class UEScreenEvents {
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(UEMenuTypes.UNCRAFTING_TABLE_MENU.get(), UncraftingTableScreen::new);
    }
}
