package com.coolerpromc.uncrafteverything.util;

import net.minecraft.world.item.ItemStack;
import com.coolerpromc.uncrafteverything.platform.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

/** Simulate the entire batch so repeated stacks cannot claim the same free space. */
public final class UncraftingOutputPlan {
    private UncraftingOutputPlan() {}

    public static List<ItemStack> create(ItemStackHandler handler, List<ItemStack> outputs, boolean indexed) {
        List<ItemStack> plan = new ArrayList<>();
        for (int slot = 0; slot < handler.getSlots(); slot++) plan.add(handler.getStackInSlot(slot).copy());
        for (int index = 0; index < outputs.size(); index++) {
            ItemStack output = outputs.get(index);
            if (output.isEmpty()) continue;
            int remaining = output.getCount();
            int first = indexed ? index : 0;
            int end = indexed ? Math.min(index + 1, handler.getSlots()) : handler.getSlots();
            for (int slot = first; slot < end && remaining > 0; slot++) {
                ItemStack existing = plan.get(slot);
                if (!existing.isEmpty() && !ItemStack.isSameItemSameTags(existing, output)) continue;
                int limit = Math.min(handler.getSlotLimit(slot), output.getMaxStackSize());
                int added = Math.min(remaining, Math.max(0, limit - existing.getCount()));
                if (added == 0) continue;
                if (existing.isEmpty()) plan.set(slot, output.copyWithCount(added));
                else existing.grow(added);
                remaining -= added;
            }
            if (remaining > 0) return null;
        }
        return plan;
    }

    public static void apply(ItemStackHandler handler, List<ItemStack> plan) {
        for (int slot = 0; slot < plan.size(); slot++) handler.setStackInSlot(slot, plan.get(slot));
    }
}
