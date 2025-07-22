package com.coolerpromc.uncrafteverything.datagen;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = UncraftEverything.MODID)
public class UEDataGenerators {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        PackOutput output = event.getGenerator().getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        event.addProvider(new UEBlockTagGenerator(output, lookupProvider));
    }
}
