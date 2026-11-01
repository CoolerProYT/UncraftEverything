package com.coolerpromc.uncrafteverything.blockentity;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.custom.*;
import com.coolerpromc.uncrafteverything.platform.Services;

import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public final class UEBlockEntities {
    public static final Supplier<BlockEntityType<UncraftingTableBlockEntity>> UNCRAFTING_TABLE_BE =
            Services.REGISTRY.registerBlockEntity(
                    "uncrafting_table_be",
                    Services.REGISTRY::createManualTable,
                    UEBlocks.UNCRAFTING_TABLE);
    public static final Supplier<BlockEntityType<AutoUncraftingTableBlockEntity>>
            AUTO_UNCRAFTING_TABLE_BE =
                    Services.REGISTRY.registerBlockEntity(
                            "auto_uncrafting_table_be",
                            Services.REGISTRY::createAutomaticTable,
                            UEBlocks.AUTO_UNCRAFTING_TABLE);

    public static void register() {}
}
