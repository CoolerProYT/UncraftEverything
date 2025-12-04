package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.blockentity.custom.AbstractUncraftingTableBE;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractUncraftingMenu<T extends AbstractUncraftingTableBE> extends ScreenHandler {
    public final T blockEntity;
    protected final World level;
    protected final PropertyDelegate data;
    public final PlayerEntity player;

    public AbstractUncraftingMenu(@Nullable ScreenHandlerType<?> menuType, int containerId, T blockEntity, World level, PlayerEntity player, PropertyDelegate data) {
        super(menuType, containerId);
        this.blockEntity = blockEntity;
        this.level = level;
        this.player = player;
        this.data = data;

        addProperties(data);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < 10) {
                if (!this.insertItem(originalStack, 10, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, 10, false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(ScreenHandlerContext.create(level, blockEntity.getPos()), player, blockEntity.getCachedState().getBlock());
    }

    protected void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 102 + i * 18));
            }
        }
    }

    protected void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 160));
        }
    }

    public abstract String getExpType();

    public abstract int getExpAmount();

    public abstract int getStatus();
}