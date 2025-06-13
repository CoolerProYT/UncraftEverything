package com.coolerpromc.uncrafteverything.screen.custom;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntArray;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class UncraftingTableMenu extends Container {
    public final UncraftingTableBlockEntity blockEntity;
    private final World level;
    private final IIntArray data;
    public final PlayerEntity player;

    public UncraftingTableMenu(int pContainerId, PlayerInventory inventory, PacketBuffer friendlyByteBuf){
        this(pContainerId, inventory, inventory.player.level.getBlockEntity(friendlyByteBuf.readBlockPos()), new IntArray(3));
    }

    public UncraftingTableMenu(int pContainerId, PlayerInventory inventory, TileEntity blockEntity, IIntArray data){
        super(UEMenuTypes.UNCRAFTING_TABLE_MENU.get(), pContainerId);
        this.blockEntity = (UncraftingTableBlockEntity) blockEntity;
        this.level = inventory.player.level;
        this.data = data;
        this.player = inventory.player;

        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);

        IItemHandler inputHandler = this.blockEntity.getInputHandler();
        this.addSlot(new SlotItemHandler(inputHandler, 0, 26, 35));

        IItemHandler outputHandler = this.blockEntity.getOutputHandler();
        for (int i = 0; i < this.blockEntity.getOutputHandler().getSlots(); i ++){
            this.addSlot(new SlotItemHandler(outputHandler, i, 98 + 18 * (i % 3), 17 + (i / 3) * 18));
        }

        addDataSlots(data);
    }

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 10;

    @Override
    public ItemStack quickMoveStack(PlayerEntity pPlayer, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + pIndex);
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(pPlayer, sourceStack);
        blockEntity.getOutputStacks();
        if (player instanceof ServerPlayerEntity){
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            UncraftingTableDataPayload.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new UncraftingTableDataPayload(blockEntity.getBlockPos(), blockEntity.getCurrentRecipes()));
        }
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(PlayerEntity pPlayer) {
        return stillValid(IWorldPosCallable.create(level, blockEntity.getBlockPos()),
                pPlayer, UEBlocks.UNCRAFTING_TABLE.get());
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 102 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 160));
        }
    }

    @Override
    public void removed(PlayerEntity player) {
        ItemStack stack = blockEntity.getInputHandler().getStackInSlot(0);
        if (!stack.isEmpty()) {
            player.inventory.placeItemBackInInventory(level, stack);
            blockEntity.getInputHandler().setStackInSlot(0, ItemStack.EMPTY);
            blockEntity.setChanged();
        }

        for (int i = 0; i < blockEntity.getOutputHandler().getSlots(); i++) {
            ItemStack outputStack = blockEntity.getOutputHandler().getStackInSlot(i);
            if (!outputStack.isEmpty()) {
                player.inventory.placeItemBackInInventory(level, outputStack);
                blockEntity.getOutputHandler().setStackInSlot(i, ItemStack.EMPTY);
                blockEntity.setChanged();
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
