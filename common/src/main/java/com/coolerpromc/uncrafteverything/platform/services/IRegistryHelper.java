package com.coolerpromc.uncrafteverything.platform.services;

import com.coolerpromc.uncrafteverything.blockentity.custom.*;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public interface IRegistryHelper {
    <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> factory);

    <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(
            String name, BlockEntityFactory<T> factory, Supplier<? extends Block> block);

    <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(
            String name, MenuFactory<T> factory);

    void registerCreativeTab(String name, Supplier<CreativeModeTab> factory);

    CreativeModeTab.Builder creativeTabBuilder();

    default UncraftingTableBlockEntity createManualTable(BlockPos pos, BlockState state) {
        return new UncraftingTableBlockEntity(pos, state);
    }

    default AutoUncraftingTableBlockEntity createAutomaticTable(BlockPos pos, BlockState state) {
        return new AutoUncraftingTableBlockEntity(pos, state);
    }

    @FunctionalInterface
    interface BlockEntityFactory<T extends BlockEntity> {
        T create(BlockPos pos, BlockState state);
    }

    @FunctionalInterface
    interface MenuFactory<T extends AbstractContainerMenu> {
        T create(int id, Inventory inventory, FriendlyByteBuf data);
    }
}
