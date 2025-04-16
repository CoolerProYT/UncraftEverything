package com.coolerpromc.uncrafteverything.block;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.block.custom.UncraftingTableBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class UEBlocks {
    public static final Block UNCRAFTING_TABLE = registerBlock("uncrafting_table", new UncraftingTableBlock(AbstractBlock.Settings.copy(Blocks.CRAFTING_TABLE).registryKey(
            RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(UncraftEverything.MODID, "uncrafting_table"))
    )));

    public static <T extends Block> T registerBlock(String name, T block){
        Identifier id = Identifier.of(UncraftEverything.MODID, name);
        registerItem(name, new BlockItem(block, new Item.Settings().useBlockPrefixedTranslationKey().registryKey(RegistryKey.of(RegistryKeys.ITEM, id))));
        return Registry.register(Registries.BLOCK, id, block);
    }

    public static void registerItem(String name, Item item) {
        Identifier itemID = Identifier.of(UncraftEverything.MODID, name);
        Registry.register(Registries.ITEM, itemID, item);
    }

    public static void register() {

    }
}
