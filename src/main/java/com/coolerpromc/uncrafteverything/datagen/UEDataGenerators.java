package com.coolerpromc.uncrafteverything.datagen;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UncraftEverything.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class UEDataGenerators {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        UEBlockTagGenerator ueBlockTagGenerator = new UEBlockTagGenerator(generator, existingFileHelper);
        generator.addProvider(event.includeServer(), ueBlockTagGenerator);
        generator.addProvider(event.includeServer(), new UEItemTagGenerator(generator, ueBlockTagGenerator, existingFileHelper));
    }
}
