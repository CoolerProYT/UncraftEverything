package com.coolerpromc.uncrafteverything.compat.mod;

import com.jahirtrap.randomisfits.init.ModComponents;
import net.minecraft.item.ItemStack;

public class RandomMisfitsCompat {
    public static void removeComponent(ItemStack inputStack){
        inputStack.remove(ModComponents.RANGE_KEY);
        inputStack.remove(ModComponents.MODE_KEY);
        inputStack.remove(ModComponents.FELLING_KEY);
    }
}
