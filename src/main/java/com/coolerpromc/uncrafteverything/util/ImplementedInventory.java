package com.coolerpromc.uncrafteverything.util;

import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public abstract class ImplementedInventory extends SimpleInventory implements SidedInventory {
    public ItemStack previousInputStack = ItemStack.EMPTY;
    private boolean isProcessingChange = false;

    public ImplementedInventory(int size){
        this(size, null);
    }

    public ImplementedInventory(int size, World world){
        super(size);
        addListener(inventory -> {
            if (world != null && world.isClient()) return;
            if (isProcessingChange) return;

            ItemStack newStack = inventory.getStack(0);
            if (!ItemStack.areEqual(previousInputStack, newStack)) {
                isProcessingChange = true;
                previousInputStack = newStack.copy();
                isProcessingChange = false;
            }
        });
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        int[] result = new int[size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = i;
        }
        return result;
    }

    public void onContentChanged(ItemStack previousContent) {
    }
}