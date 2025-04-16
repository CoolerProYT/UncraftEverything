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
    public ItemStack quickMove(PlayerEntity player, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasStack()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getStack();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!insertItem(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!insertItem(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + pIndex);
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.setStack(ItemStack.EMPTY);
        } else {
            sourceSlot.markDirty();
        }
        sourceSlot.onTakeItem(player, sourceStack);
        return copyOfSourceStack;
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
