package com.coolerpromc.uncrafteverything.platform.util;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

@FunctionalInterface
public interface MenuFactory<T extends AbstractContainerMenu, D> {
    T create(int syncId, Inventory inventory, D pos);
}
