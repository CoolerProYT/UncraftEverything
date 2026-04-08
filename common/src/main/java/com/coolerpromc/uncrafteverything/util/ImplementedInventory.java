package com.coolerpromc.uncrafteverything.util;

import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class ImplementedInventory extends SimpleContainer implements WorldlyContainer {
    public ItemStack previousInputStack = ItemStack.EMPTY;
    private boolean isProcessingChange = false;
    private final Level world;

    public ImplementedInventory(int size){
        this(size, null);
    }

    public ImplementedInventory(int size, Level world){
        super(size);
        this.world = world;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        int[] result = new int[getContainerSize()];
        for (int i = 0; i < result.length; i++) {
            result[i] = i;
        }
        return result;
    }

    @Override
    public void setChanged() {
        if (world != null && world.isClientSide()) return;
        if (isProcessingChange) return;

        ItemStack newStack = this.getItem(0);
        if (!ItemStack.matches(previousInputStack, newStack)) {
            isProcessingChange = true;
            onContentChanged(previousInputStack);
            previousInputStack = newStack.copy();
            isProcessingChange = false;
        }
    }

    public void onContentChanged(ItemStack previousContent) {
    }
}