package com.coolerpromc.uncrafteverything.datagen;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class UEBlockTagGenerator extends BlockTagsProvider {

    public UEBlockTagGenerator(DataGenerator output, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, UncraftEverything.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {

    }
}