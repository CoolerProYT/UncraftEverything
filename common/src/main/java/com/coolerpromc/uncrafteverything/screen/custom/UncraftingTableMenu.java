package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class UncraftingTableMenu extends AbstractUncraftingMenu<UncraftingTableBlockEntity> {
    public UncraftingTableMenu(int syncId, Inventory playerInventory, BlockPos blockPos) {
        this(syncId, playerInventory, playerInventory.player.level().getBlockEntity(blockPos), new SimpleContainerData(3));
    }

    public UncraftingTableMenu(int syncId, Inventory playerInventory, BlockEntity blockEntity, ContainerData data) {
        super(UEMenuTypes.UNCRAFTING_TABLE_MENU.get(), syncId, (UncraftingTableBlockEntity) blockEntity, playerInventory.player.level(), playerInventory.player, data);

        this.addSlot(new Slot(this.blockEntity.getSlots(), this.blockEntity.getInputSlots()[0], 26, 35));

        for (int i = 0; i < this.blockEntity.getOutputSlots().length; i++) {
            this.addSlot(new Slot(this.blockEntity.getSlots(), this.blockEntity.getOutputSlots()[i], 98 + 18 * (i % 3), 17 + (i / 3) * 18){
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    @Override
    public void slotsChanged(Container inventory) {
        super.slotsChanged(inventory);
        this.broadcastChanges();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()){
            ItemStack stack = blockEntity.getSlots().getItem(blockEntity.getInputSlots()[0]);
            if (!stack.isEmpty()) {
                player.getInventory().placeItemBackInInventory(stack);
                blockEntity.getSlots().setItem(blockEntity.getInputSlots()[0], ItemStack.EMPTY);
                blockEntity.setChanged();
            }

            for (int i : blockEntity.getOutputSlots()) {
                ItemStack outputStack = blockEntity.getSlots().getItem(i);
                if (!outputStack.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(outputStack);
                    blockEntity.getSlots().setItem(i, ItemStack.EMPTY);
                    blockEntity.setChanged();
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
