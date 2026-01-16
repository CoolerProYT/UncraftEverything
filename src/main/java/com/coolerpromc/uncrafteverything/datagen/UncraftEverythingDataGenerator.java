package com.coolerpromc.uncrafteverything.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.HolderLookup;
import java.util.concurrent.CompletableFuture;

public class UncraftEverythingDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		CompletableFuture<HolderLookup.Provider> registriesFuture = fabricDataGenerator.getRegistries();

		pack.addProvider(UEBlockTagGenerator::new);
	}
}
