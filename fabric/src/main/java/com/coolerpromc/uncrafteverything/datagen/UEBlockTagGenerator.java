package com.coolerpromc.uncrafteverything.datagen;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;

public final class UEBlockTagGenerator extends FabricTagProvider.BlockTagProvider {
  public UEBlockTagGenerator(
      FabricDataOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
    super(output, lookup);
  }

  @Override
  protected void addTags(HolderLookup.Provider lookup) {
    getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_AXE)
        .add(UEBlocks.UNCRAFTING_TABLE.get())
        .add(UEBlocks.AUTO_UNCRAFTING_TABLE.get());
  }
}
