package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class UncraftingTableMenu extends AbstractUncraftingMenu<UncraftingTableBlockEntity> {
    public UncraftingTableMenu(int syncId, PlayerInventory playerInventory, BlockPos blockPos) {
        this(syncId, playerInventory, playerInventory.player.getEntityWorld().getBlockEntity(blockPos), new ArrayPropertyDelegate(3));
    }

    public UncraftingTableMenu(int syncId, PlayerInventory playerInventory, BlockEntity blockEntity, PropertyDelegate data) {
        super(UEMenuTypes.UNCRAFTING_TABLE_MENU, syncId, (UncraftingTableBlockEntity) blockEntity, playerInventory.player.getEntityWorld(), playerInventory.player, data);

        this.addSlot(new Slot(this.blockEntity.getSlots(), this.blockEntity.getInputSlots()[0], 26, 35));

        for (int i = 0; i < this.blockEntity.getOutputSlots().length; i++) {
            this.addSlot(new Slot(this.blockEntity.getSlots(), this.blockEntity.getOutputSlots()[i], 98 + 18 * (i % 3), 17 + (i / 3) * 18){
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
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        this.sendContentUpdates();
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (!player.getEntityWorld().isClient()){
            ItemStack stack = blockEntity.getSlots().getStack(blockEntity.getInputSlots()[0]);
            if (!stack.isEmpty()) {
                player.getInventory().offerOrDrop(stack);
                blockEntity.getSlots().setStack(blockEntity.getInputSlots()[0], ItemStack.EMPTY);
                blockEntity.markDirty();
            }

            for (int i : blockEntity.getOutputSlots()) {
                ItemStack outputStack = blockEntity.getSlots().getStack(i);
                if (!outputStack.isEmpty()) {
                    player.getInventory().offerOrDrop(outputStack);
                    blockEntity.getSlots().setStack(i, ItemStack.EMPTY);
                    blockEntity.markDirty();
                }
            }
        }
    }

    public String getExpType(){
        return this.data.get(1) == 0 ? "Point" : "Level";
    }

    public int getExpAmount(){
        return this.data.get(0);
    }

    public int getStatus(){
        return this.data.get(2);
    }

}
