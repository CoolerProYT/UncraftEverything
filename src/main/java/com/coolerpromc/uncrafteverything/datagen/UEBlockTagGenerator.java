package com.coolerpromc.uncrafteverything.datagen;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

public class UEBlockTagGenerator extends FabricTagProvider.BlockTagProvider {
    public UEBlockTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        this.getOrCreateTagBuilder(BlockTags.AXE_MINEABLE)
                .add(UEBlocks.UNCRAFTING_TABLE);
    }
}
