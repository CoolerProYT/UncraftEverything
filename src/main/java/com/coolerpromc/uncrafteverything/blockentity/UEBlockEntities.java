package com.coolerpromc.uncrafteverything.blockentity;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class UEBlockEntities {
    public static final DeferredRegister<TileEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, UncraftEverything.MODID);

    public static final Supplier<TileEntityType<UncraftingTableBlockEntity>> UNCRAFTING_TABLE_BE = BLOCK_ENTITIES.register("uncrafting_table_be", () -> TileEntityType.Builder.of(UncraftingTableBlockEntity::new, UEBlocks.UNCRAFTING_TABLE.get()).build(null));

    public static void register(IEventBus eventBus){
        BLOCK_ENTITIES.register(eventBus);
    }
}
