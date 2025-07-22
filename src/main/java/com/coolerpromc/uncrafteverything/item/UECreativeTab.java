package com.coolerpromc.uncrafteverything.item;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;

public class UECreativeTab {
    public static final CreativeModeTab UE_TAB = new CreativeModeTab("uncrafteverything"){

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(UEBlocks.UNCRAFTING_TABLE.get());
        }

        @Override
        public Component getDisplayName() {
            return new TranslatableComponent("creativetab.uncrafteverything");
        }

        @Override
        public void fillItemList(NonNullList<ItemStack> pItems) {
            pItems.add(UEBlocks.UNCRAFTING_TABLE.get().asItem().getDefaultInstance());
        }
    };

    public static void register(IEventBus eventBus) {

    }
}