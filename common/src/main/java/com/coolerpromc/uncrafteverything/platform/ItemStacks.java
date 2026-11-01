package com.coolerpromc.uncrafteverything.platform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class ItemStacks {
  private ItemStacks() {}

  public static ItemStack create(ItemLike item, int count, CompoundTag tag) {
    ItemStack stack = new ItemStack(item, count);
    if (tag != null) stack.setTag(tag.copy());
    return stack;
  }

  public static ItemStack remaining(ItemStack stack) {
    return Services.INGREDIENT.remaining(stack);
  }
}
