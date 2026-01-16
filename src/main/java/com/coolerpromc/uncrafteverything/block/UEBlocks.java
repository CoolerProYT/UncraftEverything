package com.coolerpromc.uncrafteverything.block;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.block.custom.AutoUncraftingTableBlock;
import com.coolerpromc.uncrafteverything.block.custom.UncraftingTableBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class UEBlocks {
    public static final Block UNCRAFTING_TABLE = registerBlock("uncrafting_table", new UncraftingTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE).setId(
            ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(UncraftEverything.MODID, "uncrafting_table"))
    )));
    public static final Block AUTO_UNCRAFTING_TABLE = registerBlock("auto_uncrafting_table", new AutoUncraftingTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTER).setId(
            ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(UncraftEverything.MODID, "auto_uncrafting_table"))
    )));

    public static <T extends Block> T registerBlock(String name, T block){
        Identifier id = Identifier.fromNamespaceAndPath(UncraftEverything.MODID, name);
        registerItem(name, new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(ResourceKey.create(Registries.ITEM, id))));
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    public static void registerItem(String name, Item item) {
        Identifier itemID = Identifier.fromNamespaceAndPath(UncraftEverything.MODID, name);
        Registry.register(BuiltInRegistries.ITEM, itemID, item);
    }

    public static void register() {

    }
}
