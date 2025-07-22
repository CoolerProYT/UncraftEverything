package com.coolerpromc.uncrafteverything.datagen;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = UncraftEverything.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class UEDataGenerators {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        generator.addProvider(new UEItemTagGenerator(generator, new UEBlockTagGenerator(generator, existingFileHelper), existingFileHelper));
    }
}
