package com.coolerpromc.uncrafteverything.datagen;

import com.coolerpromc.uncrafteverything.Constants;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Constants.MODID)
public class UEDataGenerators {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        event.createProvider(UEBlockTagGenerator::new);
    }
}
