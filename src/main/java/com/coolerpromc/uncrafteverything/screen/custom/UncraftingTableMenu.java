package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.network.PacketDistributor;

public class UncraftingTableMenu extends AbstractUncraftingMenu<UncraftingTableBlockEntity> {
    public UncraftingTableMenu(int pContainerId, Inventory inventory, FriendlyByteBuf friendlyByteBuf){
        this(pContainerId, inventory, inventory.player.level().getBlockEntity(friendlyByteBuf.readBlockPos()), new SimpleContainerData(3));
    }

    public UncraftingTableMenu(int pContainerId, Inventory inventory, BlockEntity blockEntity, ContainerData data){
        super(UEMenuTypes.UNCRAFTING_TABLE_MENU.get(), pContainerId, (UncraftingTableBlockEntity) blockEntity, inventory.player.level(), inventory.player, data);

        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);

        IItemHandler inputHandler = this.blockEntity.getInputHandler();
        this.addSlot(new SlotItemHandler(inputHandler, 0, 26, 35));

        IItemHandler outputHandler = this.blockEntity.getOutputHandler();
        for (int i = 0; i < this.blockEntity.getOutputHandler().getSlots(); i ++){
            this.addSlot(new SlotItemHandler(outputHandler, i, 98 + 18 * (i % 3), 17 + (i / 3) * 18));
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()){
            ItemStack stack = blockEntity.getInputHandler().getStackInSlot(0);
            if (!stack.isEmpty()) {
                player.getInventory().placeItemBackInInventory(stack);
                blockEntity.getInputHandler().setStackInSlot(0, ItemStack.EMPTY);
                blockEntity.setChanged();
            }

            for (int i = 0; i < blockEntity.getOutputHandler().getSlots(); i++) {
                ItemStack outputStack = blockEntity.getOutputHandler().getStackInSlot(i);
                if (!outputStack.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(outputStack);
                    blockEntity.getOutputHandler().setStackInSlot(i, ItemStack.EMPTY);
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
