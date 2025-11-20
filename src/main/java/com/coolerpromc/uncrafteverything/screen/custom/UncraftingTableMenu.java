package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class UncraftingTableMenu extends AbstractUncraftingMenu<UncraftingTableBlockEntity> {
    public UncraftingTableMenu(int pContainerId, Inventory inventory, FriendlyByteBuf friendlyByteBuf){
        this(pContainerId, inventory, inventory.player.level().getBlockEntity(friendlyByteBuf.readBlockPos()), new SimpleContainerData(3));
    }

    public UncraftingTableMenu(int pContainerId, Inventory inventory, BlockEntity blockEntity, ContainerData data){
        super(UEMenuTypes.UNCRAFTING_TABLE_MENU.get(), pContainerId, (UncraftingTableBlockEntity) blockEntity, inventory.player.level(), inventory.player, data);

        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);

        ItemStacksResourceHandler inputHandler = this.blockEntity.getInputHandler();
        this.addSlot(new ResourceHandlerSlot(inputHandler, inputHandler::set, 0, 26, 35));

        ItemStacksResourceHandler outputHandler = this.blockEntity.getOutputHandler();
        for (int i = 0; i < this.blockEntity.getOutputHandler().size(); i ++){
            this.addSlot(new ResourceHandlerSlot(outputHandler, outputHandler::set, i, 98 + 18 * (i % 3), 17 + (i / 3) * 18));
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()){
            ItemStack stack = blockEntity.getInputHandler().getResource(0).toStack(blockEntity.getInputHandler().getAmountAsInt(0));
            if (!stack.isEmpty()) {
                player.getInventory().placeItemBackInInventory(stack);
                blockEntity.getInputHandler().set(0, ItemResource.EMPTY, 0);
                blockEntity.setChanged();
            }

            for (int i = 0; i < blockEntity.getOutputHandler().size(); i++) {
                ItemStack outputStack = blockEntity.getOutputHandler().getResource(i).toStack(blockEntity.getOutputHandler().getAmountAsInt(i));
                if (!outputStack.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(outputStack);
                    blockEntity.getOutputHandler().set(i, ItemResource.EMPTY, 0);
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
