package com.coolerpromc.uncrafteverything.platform;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public record ForgeInventoryAdapter(ItemStackHandler inventory) implements IItemHandler {
    public int getSlots() {
        return inventory.getSlots();
    }

    public ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(slot);
    }

    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return inventory.insertItem(slot, stack, simulate);
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return inventory.extractItem(slot, amount, simulate);
    }

    public int getSlotLimit(int slot) {
        return inventory.getSlotLimit(slot);
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        return inventory.isItemValid(slot, stack);
    }
}
