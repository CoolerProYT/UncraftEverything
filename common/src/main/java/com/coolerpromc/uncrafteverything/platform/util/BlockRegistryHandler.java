package com.coolerpromc.uncrafteverything.platform.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public interface BlockRegistryHandler<T extends Block> extends RegistryHandler<Block, T>, ItemLike {
    default ItemStack toStack(){
        return asItem().getDefaultInstance();
    }
}
