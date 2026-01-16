package com.coolerpromc.uncrafteverything.blockentity;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.custom.AutoUncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class UEBlockEntities {
    public static final BlockEntityType<UncraftingTableBlockEntity> UNCRAFTING_TABLE_BE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,  "uncrafting_table",
            FabricBlockEntityTypeBuilder.create(UncraftingTableBlockEntity::new, UEBlocks.UNCRAFTING_TABLE).build(null));
    public static final BlockEntityType<AutoUncraftingTableBlockEntity> AUTO_UNCRAFTING_TABLE_BE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,  "auto_uncrafting_table",
            FabricBlockEntityTypeBuilder.create(AutoUncraftingTableBlockEntity::new, UEBlocks.AUTO_UNCRAFTING_TABLE).build(null));


    public static void register() {

    }
}
