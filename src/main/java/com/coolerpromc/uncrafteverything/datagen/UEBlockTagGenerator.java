package com.coolerpromc.uncrafteverything.datagen;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.block.UEBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class UEBlockTagGenerator extends BlockTagsProvider {

    public UEBlockTagGenerator(DataGenerator output, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, UncraftEverything.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        this.tag(BlockTags.MINEABLE_WITH_AXE)
                .add(UEBlocks.UNCRAFTING_TABLE.get());
    }
}