package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.blockentity.custom.AbstractUncraftingTableBE;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public abstract class AbstractUncraftingMenu<T extends AbstractUncraftingTableBE> extends AbstractContainerMenu {
    public final T blockEntity;
    protected final Level level;
    protected final ContainerData data;
    public final Player player;

    public AbstractUncraftingMenu(@Nullable MenuType<?> menuType, int containerId, T blockEntity, Level level, Player player, ContainerData data) {
        super(menuType, containerId);
        this.blockEntity = blockEntity;
        this.level = level;
        this.player = player;
        this.data = data;

        addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();
            if (invSlot < 10) {
                if (!this.moveItemStackTo(originalStack, 10, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(originalStack, 0, 10, false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return newStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, blockEntity.getBlockState().getBlock());
    }

    protected void addPlayerInventory(Container playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 102 + i * 18));
            }
        }
    }

    protected void addPlayerHotbar(Container playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 160));
        }
    }

    public abstract String getExpType();

    public abstract int getExpAmount();

    public abstract int getStatus();
}