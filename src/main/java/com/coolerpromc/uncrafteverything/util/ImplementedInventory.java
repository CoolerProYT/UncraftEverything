package com.coolerpromc.uncrafteverything.util;

import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;


public abstract class ImplementedInventory extends SimpleInventory implements SidedInventory {
    public ImplementedInventory(int size){
        super(size);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        int[] result = new int[size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = i;
        }

        return result;
    }
}