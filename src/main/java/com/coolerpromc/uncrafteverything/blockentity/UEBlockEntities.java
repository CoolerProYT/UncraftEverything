package com.coolerpromc.uncrafteverything.blockentity;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import java.util.Set;
import java.util.function.Supplier;

public class UEBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, UncraftEverything.MODID);

    public static final Supplier<BlockEntityType<UncraftingTableBlockEntity>> UNCRAFTING_TABLE_BE = BLOCK_ENTITIES.register("uncrafting_table_be", () -> new BlockEntityType<>(UncraftingTableBlockEntity::new, Set.of(UEBlocks.UNCRAFTING_TABLE.get())));

    public static void register(IEventBus eventBus){
        BLOCK_ENTITIES.register(eventBus);
    }
}