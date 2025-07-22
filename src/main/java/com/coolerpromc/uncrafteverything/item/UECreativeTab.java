package com.coolerpromc.uncrafteverything.item;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.block.UEBlocks;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class UECreativeTab {
    public static void register() {
        FabricItemGroupBuilder.create(new Identifier(UncraftEverything.MODID, "uncrafteverything")).icon(() -> new ItemStack(UEBlocks.UNCRAFTING_TABLE)).appendItems((itemStacks) -> {
            itemStacks.add(new ItemStack(UEBlocks.UNCRAFTING_TABLE));
        }).build();
    }
}
