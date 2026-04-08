package com.coolerpromc.uncrafteverything.platform.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public interface BlockRegistryHandler<T> extends RegistryHandler<T>, ItemLike {
    default ItemStack toStack(){
        return asItem().getDefaultInstance();
    }
}
