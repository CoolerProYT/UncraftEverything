package com.coolerpromc.uncrafteverything.platform;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class HandlerSlot extends Slot {
  private final ItemStackHandler handler;

  public HandlerSlot(ItemStackHandler handler, int slot, int x, int y) {
    super(handler, slot, x, y);
    this.handler = handler;
  }

  @Override
  public void set(ItemStack stack) {
    handler.setStackInSlot(getContainerSlot(), stack);
  }

  @Override
  public void setChanged() {
    // Menus can grow an existing stack in place without calling Container.setItem.
    handler.onContentsChanged(getContainerSlot());
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return handler.isItemValid(getContainerSlot(), stack);
  }

  @Override
  public int getMaxStackSize() {
    return handler.getSlotLimit(getContainerSlot());
  }

  @Override
  public int getMaxStackSize(ItemStack stack) {
    return Math.min(getMaxStackSize(), stack.getMaxStackSize());
  }
}
