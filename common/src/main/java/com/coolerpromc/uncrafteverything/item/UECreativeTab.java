package com.coolerpromc.uncrafteverything.item;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.platform.Services;
import com.coolerpromc.uncrafteverything.platform.util.RegistryHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class UECreativeTab {
    public static final RegistryHandler<CreativeModeTab, CreativeModeTab> UNCRAFTEVERYTHING_TAB = Services.REGISTRY.registerCreativeTab("uncrafteverything", () -> new ItemStack(UEBlocks.UNCRAFTING_TABLE), Component.translatable("creativetab.uncrafteverything"), param -> new ItemStack[]{
            UEBlocks.UNCRAFTING_TABLE.toStack(),
            UEBlocks.AUTO_UNCRAFTING_TABLE.toStack()
    });

    public static void load() {
    }
}
