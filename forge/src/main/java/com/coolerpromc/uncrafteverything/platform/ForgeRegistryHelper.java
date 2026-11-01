package com.coolerpromc.uncrafteverything.platform;

import com.coolerpromc.uncrafteverything.blockentity.custom.*;
import com.coolerpromc.uncrafteverything.platform.services.IRegistryHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

public final class ForgeRegistryHelper implements IRegistryHelper {
    private final DeferredRegister<Block> blocks =
            DeferredRegister.create(ForgeRegistries.BLOCKS, "uncrafteverything");
    private final DeferredRegister<Item> items =
            DeferredRegister.create(ForgeRegistries.ITEMS, "uncrafteverything");
    private final DeferredRegister<BlockEntityType<?>> entities =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "uncrafteverything");
    private final DeferredRegister<MenuType<?>> menus =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, "uncrafteverything");
    private final DeferredRegister<CreativeModeTab> tabs =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "uncrafteverything");

    public void attach(IEventBus bus) {
        blocks.register(bus);
        items.register(bus);
        entities.register(bus);
        menus.register(bus);
        tabs.register(bus);
    }

    public <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> factory) {
        RegistryObject<T> block = blocks.register(name, factory);
        items.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(
            String name, BlockEntityFactory<T> factory, Supplier<? extends Block> block) {
        return entities.register(
                name, () -> BlockEntityType.Builder.of(factory::create, block.get()).build(null));
    }

    public <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(
            String name, MenuFactory<T> factory) {
        return menus.register(name, () -> IForgeMenuType.create(factory::create));
    }

    public void registerCreativeTab(String name, Supplier<CreativeModeTab> factory) {
        tabs.register(name, factory);
    }

    public CreativeModeTab.Builder creativeTabBuilder() {
        return CreativeModeTab.builder();
    }

    public UncraftingTableBlockEntity createManualTable(BlockPos pos, BlockState state) {
        return new ForgeUncraftingTableBlockEntity(pos, state);
    }

    public AutoUncraftingTableBlockEntity createAutomaticTable(BlockPos pos, BlockState state) {
        return new ForgeAutoUncraftingTableBlockEntity(pos, state);
    }
}
