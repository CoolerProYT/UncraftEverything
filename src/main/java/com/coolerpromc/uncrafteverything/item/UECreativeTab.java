package com.coolerpromc.uncrafteverything.item;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.eventbus.api.IEventBus;

public class UECreativeTab {
    public static final ItemGroup PRODUCTIVE_SLIMES_TAB = new ItemGroup("uncrafteverything"){
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(UEBlocks.UNCRAFTING_TABLE.get());
        }

        @Override
        public void fillItemList(NonNullList<ItemStack> itemStacks) {
            itemStacks.add(new ItemStack(UEBlocks.UNCRAFTING_TABLE.get()));
        }
    };

    public static void register(IEventBus eventBus) {

    }
}