package com.coolerpromc.uncrafteverything.blockentity;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.custom.AutoUncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.platform.Services;
import com.coolerpromc.uncrafteverything.platform.util.RegistryHandler;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class UEBlockEntities {
    public static final RegistryHandler<BlockEntityType<?>, BlockEntityType<UncraftingTableBlockEntity>> UNCRAFTING_TABLE_BE = Services.REGISTRY.registerBlockEntity("uncrafting_table", UncraftingTableBlockEntity::new, UEBlocks.UNCRAFTING_TABLE);
    public static final RegistryHandler<BlockEntityType<?>, BlockEntityType<AutoUncraftingTableBlockEntity>> AUTO_UNCRAFTING_TABLE_BE = Services.REGISTRY.registerBlockEntity("auto_uncrafting_table", AutoUncraftingTableBlockEntity::new, UEBlocks.AUTO_UNCRAFTING_TABLE);

    public static void load() {
    }
}
