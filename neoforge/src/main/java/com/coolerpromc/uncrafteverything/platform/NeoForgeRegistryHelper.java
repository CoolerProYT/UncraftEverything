package com.coolerpromc.uncrafteverything.platform;

import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.platform.services.IRegistryHelper;
import com.coolerpromc.uncrafteverything.platform.util.BlockEntityTypeFactory;
import com.coolerpromc.uncrafteverything.platform.util.BlockRegistryHandler;
import com.coolerpromc.uncrafteverything.platform.util.MenuFactory;
import com.coolerpromc.uncrafteverything.platform.util.RegistryHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

public class NeoForgeRegistryHelper implements IRegistryHelper {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Constants.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Constants.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Constants.MODID);

    @Override
    public <T extends Block> BlockRegistryHandler<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> func, BlockBehaviour.Properties p) {
        DeferredBlock<T> block = BLOCKS.registerBlock(name, func, () -> p);
        registerItem(name, properties -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix()));

        return new BlockRegistryHandler<>() {
            @Override
            public Identifier id() {
                return block.getId();
            }

            @Override
            public Holder<T> holder() {
                return (Holder<T>) block.getDelegate();
            }

            @Override
            public T get() {
                return block.get();
            }

            @Override
            public Item asItem() {
                return block.asItem();
            }
        };
    }

    @Override
    public <T extends Item> RegistryHandler<T> registerItem(String name, Function<Item.Properties, T> func) {
        DeferredItem<T> item = ITEMS.registerItem(name, func);

        return new RegistryHandler<T>() {
            @Override
            public Identifier id() {
                return item.getId();
            }

            @Override
            public Holder<T> holder() {
                return (Holder<T>) item.getDelegate();
            }

            @Override
            public T get() {
                return item.get();
            }
        };
    }

    @Override
    public <T extends BlockEntity> RegistryHandler<BlockEntityType<T>> registerBlockEntity(String name, BlockEntityTypeFactory<T> factory, Supplier<? extends Block>... blocks) {
        DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> blockEntity = BLOCK_ENTITIES.register(name, () -> new BlockEntityType<>(factory::create, Arrays.stream(blocks).map(Supplier::get).toArray(Block[]::new)));

        return new RegistryHandler<>() {
            @Override
            public Identifier id() {
                return blockEntity.getId();
            }

            @Override
            public Holder<BlockEntityType<T>> holder() {
                return (Holder<BlockEntityType<T>>) (Holder<?>) blockEntity.getDelegate();
            }

            @Override
            public BlockEntityType<T> get() {
                return blockEntity.get();
            }
        };
    }

    @Override
    public RegistryHandler<CreativeModeTab> registerCreativeTab(String name, Supplier<ItemStack> icon, Component title, Function<CreativeModeTab.ItemDisplayParameters, ItemStack[]> func) {
        DeferredHolder<CreativeModeTab, CreativeModeTab> tab = CREATIVE_TABS.register(name, () -> CreativeModeTab.builder().icon(icon).title(title).displayItems(((param, output) -> Arrays.stream(func.apply(param)).forEach(output::accept))).build());

        return new RegistryHandler<>() {
            @Override
            public Identifier id() {
                return tab.getId();
            }

            @Override
            public Holder<CreativeModeTab> holder() {
                return tab.getDelegate();
            }

            @Override
            public CreativeModeTab get() {
                return tab.value();
            }
        };
    }

    @Override
    public <T extends AbstractContainerMenu, D> RegistryHandler<MenuType<T>> registerMenu(String name, MenuFactory<T, D> factory, StreamCodec<? super RegistryFriendlyByteBuf, D> data) {
        DeferredHolder<MenuType<?>, MenuType<T>> menu = MENUS.register(name, () -> IMenuTypeExtension.create((id, inv, buf) -> factory.create(id, inv, data.decode(buf))));

        return new RegistryHandler<>() {
            @Override
            public Identifier id() {
                return menu.getId();
            }

            @Override
            public Holder<MenuType<T>> holder() {
                return (Holder<MenuType<T>>) (Holder<?>) menu.getDelegate();
            }

            @Override
            public MenuType<T> get() {
                return menu.get();
            }
        };
    }

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        CREATIVE_TABS.register(eventBus);
        MENUS.register(eventBus);
    }
}
