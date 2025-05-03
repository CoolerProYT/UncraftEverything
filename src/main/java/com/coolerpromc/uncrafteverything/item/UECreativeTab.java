package com.coolerpromc.uncrafteverything.item;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class UECreativeTab {
    public static final ItemGroup UNCRAFTEVERYTHING_TAB = new ItemGroup(8, "uncrafteverything") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(UEBlocks.UNCRAFTING_TABLE);
        }

        @Override
        public void appendStacks(DefaultedList<ItemStack> stacks) {
            stacks.add(new ItemStack(UEBlocks.UNCRAFTING_TABLE));
        }
    };

    public static void register() {

    }
}
