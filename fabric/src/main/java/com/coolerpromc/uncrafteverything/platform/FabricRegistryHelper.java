package com.coolerpromc.uncrafteverything.platform;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.platform.services.IRegistryHelper;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.*;

import java.util.function.Supplier;

public final class FabricRegistryHelper implements IRegistryHelper {
    private ResourceLocation id(String name) {
        return new ResourceLocation("uncrafteverything", name);
    }

    public <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> factory) {
        T block = Registry.register(BuiltInRegistries.BLOCK, id(name), factory.get());
        Registry.register(
                BuiltInRegistries.ITEM, id(name), new BlockItem(block, new Item.Properties()));
        return () -> block;
    }

    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(
            String name, BlockEntityFactory<T> factory, Supplier<? extends Block> block) {
        BlockEntityType<T> type =
                Registry.register(
                        BuiltInRegistries.BLOCK_ENTITY_TYPE,
                        id(name),
                        FabricBlockEntityTypeBuilder.create(factory::create, block.get()).build());
        return () -> type;
    }

    public <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(
            String name, MenuFactory<T> factory) {
        MenuType<T> type =
                Registry.register(
                        BuiltInRegistries.MENU,
                        id(name),
                        new ExtendedScreenHandlerType<>(factory::create));
        return () -> type;
    }

    public void registerCreativeTab(String name, Supplier<CreativeModeTab> factory) {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id(name), factory.get());
    }

    public CreativeModeTab.Builder creativeTabBuilder() {
        return net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup.builder();
    }

    public void registerLegacyTable() {
        Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                new ResourceLocation("minecraft", "uncrafting_table_be"),
                FabricBlockEntityTypeBuilder.create(
                                UncraftingTableBlockEntity::new, UEBlocks.UNCRAFTING_TABLE.get())
                        .build());
    }
}
