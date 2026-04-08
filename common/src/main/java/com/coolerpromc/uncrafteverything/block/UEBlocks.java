package com.coolerpromc.uncrafteverything.block;

import com.coolerpromc.uncrafteverything.block.custom.AutoUncraftingTableBlock;
import com.coolerpromc.uncrafteverything.block.custom.UncraftingTableBlock;
import com.coolerpromc.uncrafteverything.platform.Services;
import com.coolerpromc.uncrafteverything.platform.util.BlockRegistryHandler;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class UEBlocks {
    public static final BlockRegistryHandler<UncraftingTableBlock> UNCRAFTING_TABLE = Services.REGISTRY.registerBlock("uncrafting_table", UncraftingTableBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE));
    public static final BlockRegistryHandler<AutoUncraftingTableBlock> AUTO_UNCRAFTING_TABLE = Services.REGISTRY.registerBlock("auto_uncrafting_table", AutoUncraftingTableBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTER));

    public static void load() {
    }
}
