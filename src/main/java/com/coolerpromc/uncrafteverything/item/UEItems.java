package com.coolerpromc.uncrafteverything.item;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class UEItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(UncraftEverything.MODID);

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
