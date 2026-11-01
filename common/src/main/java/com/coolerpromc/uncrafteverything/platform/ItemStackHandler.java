package com.coolerpromc.uncrafteverything.platform;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Fixed inventory, used by menus, persisted block entities and native automation adapters. */
public class ItemStackHandler implements Container {
  protected final NonNullList<ItemStack> stacks;

  public ItemStackHandler(int size) {
    this(NonNullList.withSize(size, ItemStack.EMPTY));
  }

  public ItemStackHandler(NonNullList<ItemStack> stacks) {
    this.stacks = stacks;
  }

  public int getSlots() {
    return stacks.size();
  }

  protected void validateSlotIndex(int slot) {
    if (slot < 0 || slot >= getSlots()) throw new IndexOutOfBoundsException(slot);
  }

  public ItemStack getStackInSlot(int slot) {
    validateSlotIndex(slot);
    return stacks.get(slot);
  }

  public void setStackInSlot(int slot, ItemStack stack) {
    validateSlotIndex(slot);
    stacks.set(slot, stack);
    onContentsChanged(slot);
  }

  public boolean isItemValid(int slot, ItemStack stack) {
    return true;
  }

  public int getSlotLimit(int slot) {
    return 64;
  }

  protected int getStackLimit(int slot, ItemStack stack) {
    return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
  }

  protected void onContentsChanged(int slot) {}

  public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
    if (stack.isEmpty() || !isItemValid(slot, stack)) return stack;
    ItemStack existing = getStackInSlot(slot);
    if (!existing.isEmpty() && !ItemStack.isSameItemSameTags(existing, stack)) return stack;
    int added = Math.min(stack.getCount(), getStackLimit(slot, stack) - existing.getCount());
    if (added <= 0) return stack;
    if (!simulate) setStackInSlot(slot, stack.copyWithCount(existing.getCount() + added));
    return added == stack.getCount()
        ? ItemStack.EMPTY
        : stack.copyWithCount(stack.getCount() - added);
  }

  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    ItemStack existing = getStackInSlot(slot);
    if (existing.isEmpty() || amount <= 0) return ItemStack.EMPTY;
    int count = Math.min(amount, Math.min(existing.getCount(), existing.getMaxStackSize()));
    ItemStack result = existing.copyWithCount(count);
    if (!simulate)
      setStackInSlot(
          slot,
          count == existing.getCount()
              ? ItemStack.EMPTY
              : existing.copyWithCount(existing.getCount() - count));
    return result;
  }

  public CompoundTag serializeNBT() {
    CompoundTag tag = new CompoundTag();
    ListTag items = new ListTag();
    for (int slot = 0; slot < getSlots(); slot++)
      if (!stacks.get(slot).isEmpty()) {
        CompoundTag item = stacks.get(slot).save(new CompoundTag());
        item.putInt("Slot", slot);
        items.add(item);
      }
    tag.putInt("Size", getSlots());
    tag.put("Items", items);
    return tag;
  }

  public void deserializeNBT(CompoundTag tag) {
    for (int slot = 0; slot < getSlots(); slot++) stacks.set(slot, ItemStack.EMPTY);
    ListTag items = tag.getList("Items", 10);
    for (int i = 0; i < items.size(); i++) {
      CompoundTag item = items.getCompound(i);
      int slot = item.getInt("Slot");
      if (slot >= 0 && slot < getSlots()) stacks.set(slot, ItemStack.of(item));
    }
  }

  @Override
  public int getContainerSize() {
    return getSlots();
  }

  @Override
  public boolean isEmpty() {
    return stacks.stream().allMatch(ItemStack::isEmpty);
  }

  @Override
  public ItemStack getItem(int slot) {
    return getStackInSlot(slot);
  }

  @Override
  public ItemStack removeItem(int slot, int amount) {
    return extractItem(slot, amount, false);
  }

  @Override
  public ItemStack removeItemNoUpdate(int slot) {
    ItemStack stack = getStackInSlot(slot);
    stacks.set(slot, ItemStack.EMPTY);
    return stack;
  }

  @Override
  public void setItem(int slot, ItemStack stack) {
    setStackInSlot(slot, stack);
  }

  @Override
  public void setChanged() {}

  @Override
  public boolean stillValid(Player player) {
    return true;
  }

  @Override
  public boolean canPlaceItem(int slot, ItemStack stack) {
    return isItemValid(slot, stack);
  }

  @Override
  public void clearContent() {
    for (int slot = 0; slot < getSlots(); slot++) setStackInSlot(slot, ItemStack.EMPTY);
  }
}
