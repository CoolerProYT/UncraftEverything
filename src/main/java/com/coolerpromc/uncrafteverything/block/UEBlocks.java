package com.coolerpromc.uncrafteverything.block;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.block.custom.UncraftingTableBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class UEBlocks {
    public static final Block UNCRAFTING_TABLE = registerBlock("uncrafting_table", new UncraftingTableBlock(AbstractBlock.Settings.copy(Blocks.CRAFTING_TABLE)));

    public static <T extends Block> T registerBlock(String name, T block){
        Identifier id = new Identifier(UncraftEverything.MODID, name);
        registerItem(name, new BlockItem(block, new Item.Settings()));
        return Registry.register(Registry.BLOCK, id, block);
    }

    public static void registerItem(String name, Item item) {
        Identifier itemID = new Identifier(UncraftEverything.MODID, name);
        Registry.register(Registry.ITEM, itemID, item);
    }

    public static void register() {

    }
}
