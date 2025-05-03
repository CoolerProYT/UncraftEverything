package com.coolerpromc.uncrafteverything.blockentity;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

public class UEBlockEntities {
    public static final BlockEntityType<UncraftingTableBlockEntity> UNCRAFTING_TABLE_BE = Registry.register(Registry.BLOCK_ENTITY_TYPE,  "uncrafting_table_be",
            BlockEntityType.Builder.create(UncraftingTableBlockEntity::new, UEBlocks.UNCRAFTING_TABLE).build(null));

    public static void register() {

    }
}
