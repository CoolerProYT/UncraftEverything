package com.coolerpromc.uncrafteverything.datagen;

import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.block.UEBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;

import java.util.concurrent.CompletableFuture;

public class UEBlockTagGenerator extends net.neoforged.neoforge.common.data.BlockTagsProvider {
    public UEBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Constants.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(BlockTags.MINEABLE_WITH_AXE)
                .add(UEBlocks.UNCRAFTING_TABLE.key());

        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(UEBlocks.AUTO_UNCRAFTING_TABLE.key());
    }
}
