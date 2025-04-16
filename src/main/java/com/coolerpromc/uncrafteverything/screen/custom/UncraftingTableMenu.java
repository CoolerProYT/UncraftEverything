package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UncraftingTableMenu extends ScreenHandler {
    public final UncraftingTableBlockEntity blockEntity;
    private final World world;

    public UncraftingTableMenu(int syncId, PlayerInventory playerInventory, BlockPos blockPos) {
        this(syncId, playerInventory, playerInventory.player.getWorld().getBlockEntity(blockPos));
    }

    public UncraftingTableMenu(int syncId, PlayerInventory playerInventory, BlockEntity blockEntity) {
        super(UEMenuTypes.UNCRAFTING_TABLE_MENU, syncId);
        this.blockEntity = (UncraftingTableBlockEntity) blockEntity;
        this.world = playerInventory.player.getWorld();

        this.addSlot(new Slot(this.blockEntity, this.blockEntity.getInputSlots()[0], 26, 35));

        for (int i = 0; i < this.blockEntity.getOutputSlots().length; i++) {
            this.addSlot(new Slot(this.blockEntity, this.blockEntity.getOutputSlots()[i], 98 + 18 * (i % 3), 17 + (i / 3) * 18){
                @Override
                public boolean canInsert(ItemStack stack) {
                    return false;
                }
            });
        }

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.blockEntity.size()) {
                if (!this.insertItem(originalStack, this.blockEntity.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.blockEntity.size(), false)) {
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
        return canUse(ScreenHandlerContext.create(world, blockEntity.getPos()), player, UEBlocks.UNCRAFTING_TABLE);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 102 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 160));
        }
    }

    @Override
    public void onClosed(PlayerEntity player) {
        ItemStack stack = blockEntity.getStack(blockEntity.getInputSlots()[0]);
        if (!stack.isEmpty()) {
            player.getInventory().offerOrDrop(stack);
            blockEntity.setStack(blockEntity.getInputSlots()[0], ItemStack.EMPTY);
            blockEntity.markDirty();
        }

        for (int i : blockEntity.getOutputSlots()) {
            ItemStack outputStack = blockEntity.getStack(i);
            if (!outputStack.isEmpty()) {
                player.getInventory().offerOrDrop(outputStack);
                blockEntity.setStack(i, ItemStack.EMPTY);
                blockEntity.markDirty();
            }
        }
    }
}
