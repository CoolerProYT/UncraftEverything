package com.coolerpromc.uncrafteverything.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class ModItemStackHandler extends ItemStacksResourceHandler {
    public ModItemStackHandler(int size) {
        super(NonNullList.withSize(size, ItemStack.EMPTY));
    }

    public ItemStack extractItemWithoutTriggerChanges(int slot, int amount, boolean simulate) {
        if (amount == 0)
            return ItemStack.EMPTY;

        if (!(slot >= 0 && slot < size())) throw new IllegalStateException("Slot " + slot + " invalid for size " + size());

        ItemStack existing = this.stacks.get(slot);

        if (existing.isEmpty())
            return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                this.stacks.set(slot, ItemStack.EMPTY);
                return existing;
            } else {
                return existing.copy();
            }
        } else {
            if (!simulate) {
                this.stacks.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
            }

            return existing.copyWithCount(toExtract);
        }
    }

}
