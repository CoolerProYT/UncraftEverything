package com.coolerpromc.uncrafteverything.datagen;

import net.fabricmc.fabric.api.datagen.v1.*;

public final class UncraftEverythingDataGenerator implements DataGeneratorEntrypoint {
  @Override
  public void onInitializeDataGenerator(FabricDataGenerator generator) {
    generator.createPack().addProvider(UEBlockTagGenerator::new);
  }
}
