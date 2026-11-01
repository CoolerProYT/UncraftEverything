package com.coolerpromc.uncrafteverything.block;

import com.coolerpromc.uncrafteverything.block.custom.*;
import com.coolerpromc.uncrafteverything.platform.Services;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Supplier;

public final class UEBlocks {
    public static final Supplier<Block> UNCRAFTING_TABLE =
            Services.REGISTRY.registerBlock(
                    "uncrafting_table",
                    () ->
                            new UncraftingTableBlock(
                                    BlockBehaviour.Properties.copy(Blocks.CRAFTING_TABLE)));
    public static final Supplier<Block> AUTO_UNCRAFTING_TABLE =
            Services.REGISTRY.registerBlock(
                    "auto_uncrafting_table",
                    () ->
                            new AutoUncraftingTableBlock(
                                    BlockBehaviour.Properties.of()
                                            .mapColor(MapColor.STONE)
                                            .strength(1.5F, 3.5F)));

    public static void register() {}
}
