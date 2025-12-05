package com.coolerpromc.uncrafteverything.event;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingClientConfig;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = UncraftEverything.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class UEModBusEvent {
    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == UncraftEverythingClientConfig.CONFIG_SPEC) {
            UncraftEverythingClientConfig.CONFIG.onChanged();
        }
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == UncraftEverythingClientConfig.CONFIG_SPEC) {
            UncraftEverythingClientConfig.CONFIG.onChanged();
        }
    }
}
