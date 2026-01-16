package com.coolerpromc.uncrafteverything.datagen;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;

import java.util.concurrent.CompletableFuture;

public class UEBlockTagGenerator extends FabricTagsProvider.BlockTagsProvider {
    public UEBlockTagGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        this.valueLookupBuilder(BlockTags.MINEABLE_WITH_AXE)
                .add(UEBlocks.UNCRAFTING_TABLE);
    }
}
