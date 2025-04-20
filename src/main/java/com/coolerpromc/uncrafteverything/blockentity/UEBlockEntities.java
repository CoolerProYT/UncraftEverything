package com.coolerpromc.uncrafteverything.blockentity;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class UEBlockEntities {
    public static final BlockEntityType<UncraftingTableBlockEntity> UNCRAFTING_TABLE_BE = Registry.register(Registries.BLOCK_ENTITY_TYPE,  "uncrafting_table_be",
            FabricBlockEntityTypeBuilder.create(UncraftingTableBlockEntity::new, UEBlocks.UNCRAFTING_TABLE).build(null));

    public static void register() {

    }
}
