package com.coolerpromc.uncrafteverything.blockentity.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.ClientBoundUncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.platform.Services;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableMenu;
import com.coolerpromc.uncrafteverything.util.ImplementedInventory;
import com.coolerpromc.uncrafteverything.util.Status;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"unused"})
public class UncraftingTableBlockEntity extends AbstractUncraftingTableBE implements MenuProvider {
    private final ContainerData data;

    private final int[] inputSlots = {0};
    private final int[] outputSlots = {1, 2, 3, 4, 5, 6, 7, 8, 9};

    private final ImplementedInventory slots = new ImplementedInventory(10){
        @Override
        public void setItem(int slot, ItemStack stack) {
            super.setItem(slot, stack);
            getOutputStacks(this, false);
            if (level != null && !level.isClientSide() && slot == inputSlots[0] && player != null) {
                if (currentStack.getItem() != this.getItem(0).getItem() && !this.getItem(0).isEmpty()){
                    for (int outputSlot : outputSlots) {
                        ItemStack outputStack = this.getItem(outputSlot);
                        if (!outputStack.isEmpty()) {
                            player.getInventory().placeItemBackInInventory(outputStack, Prediction.SERVER_ONLY);
                            this.setItem(outputSlot, ItemStack.EMPTY);
                            setChanged();
                        }
                    }
                }
                currentStack = this.getItem(0);
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                int fromIndex = page * 7;
                if (fromIndex >= currentRecipes.size()) {
                    fromIndex = currentRecipes.size();
                }
                int toIndex = Math.min(fromIndex + 7, currentRecipes.size());
                Services.NETWORK.sendToPlayer(player, new ClientBoundUncraftingTableDataPayload(getBlockPos(), new ArrayList<>(currentRecipes.subList(fromIndex, toIndex)), currentRecipes.size()));
                player.containerMenu.broadcastChanges();
            }
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            getOutputStacks(this, false);
            if (slot != 0){
                if (player != null){
                    handleRecipeSelection(currentRecipe);
                }
            }
            return super.removeItemNoUpdate(slot);
        }

        @Override
        public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
            return slot == inputSlots[0] && side != Direction.DOWN;
        }

        @Override
        public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
            return slot != inputSlots[0] && side == Direction.DOWN;
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return Arrays.stream(outputSlots).noneMatch(value -> value == slot);
        }
    };

    public UncraftingTableBlockEntity(BlockPos pos, BlockState state) {
        super(UEBlockEntities.UNCRAFTING_TABLE_BE.get(), pos, state);
        this.experienceType = UncraftEverythingConfig.CONFIG.experienceType() == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0;
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index){
                    case 0 -> experience;
                    case 1 -> experienceType;
                    case 2 -> status.getIndex();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index){
                    case 0 -> experience = value;
                    case 1 -> experienceType = value;
                    case 2 -> status = Status.byIndex(value);
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.uncrafteverything.uncrafting_table");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        if (player instanceof ServerPlayer serverPlayer){
            this.player = serverPlayer;
        }
        return new UncraftingTableMenu(syncId, playerInventory, this, data);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);

        ContainerHelper.saveAllItems(view, slots.items);

        view.putInt("experience", experience);
        view.putInt("experienceType", experienceType);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);

        ContainerHelper.loadAllItems(view, slots.items);

        experience = view.getIntOr("experience", UncraftEverythingConfig.CONFIG.experience());
        experienceType = view.getIntOr("experienceType", UncraftEverythingConfig.CONFIG.experienceType() == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0);
    }

    public int[] getInputSlots() {
        return inputSlots;
    }

    public int[] getOutputSlots() {
        return outputSlots;
    }

    public void handleUncraftButtonClicked(boolean hasShiftDown){
        if (hasShiftDown){
            while (hasRecipe() && hasEnoughExperience()) {
                processUncraft(hasNextRecipe());
            }
        }
        else{
            if (hasRecipe() && hasEnoughExperience()) {
                processUncraft(false);
            }
        }
    }

    public void handleRecipeSelection(UncraftingTableRecipe recipe){
        this.currentRecipe = recipe;

        if(!hasRecipe()){
            if (slots.getItem(0).isEmpty()){
                this.status = Status.BLANK;
            }
            else {
                if (UncraftEverythingConfig.CONFIG.isItemLocked(player, slots.getItem(inputSlots[0])).getLeft()){
                    this.status = Status.LOCKED_ITEM;
                }
                else{
                    this.status = Status.NO_SUITABLE_OUTPUT_SLOT;
                }
            }
        }
        else{
            if (hasEnoughExperience()){
                this.status = Status.BLANK;
            }
            else{
                this.status = Status.NOT_ENOUGH_EXP;
            }
        }
    }

    public boolean hasEnoughExperience(){
        if (UncraftEverythingConfig.CONFIG.experienceType().equals(UncraftEverythingConfig.ExperienceType.POINT)){
            return player.totalExperience >= getExperience(this.slots) || player.isCreative();
        }
        else if (UncraftEverythingConfig.CONFIG.experienceType().equals(UncraftEverythingConfig.ExperienceType.LEVEL)){
            return player.experienceLevel >= getExperience(this.slots) || player.isCreative();
        }
        return true;
    }

    private int findSuitableOutputSlot(ItemStack result) {
        for (int i = 0; i < this.outputSlots.length; i++) {
            ItemStack stackInSlot = this.slots.getItem(outputSlots[i]);
            if (stackInSlot.isEmpty() || (ItemStack.isSameItemSameComponents(stackInSlot, result) && stackInSlot.getCount() + result.getCount() <= stackInSlot.getMaxStackSize())) {
                return i;
            }
        }
        return -1;
    }

    private void processUncraft(boolean hasNext){
        List<ItemStack> outputs = currentRecipe.getOutputs();

        for (ItemStack output : outputs) {
            int slot = this.findSuitableOutputSlot(output);
            if (slot != -1) {
                ItemStack slotStack = slots.getItem(outputSlots[slot]);

                if (slotStack.isEmpty()) {
                    slots.setItem(outputSlots[slot], output.copy());
                } else if (ItemStack.isSameItemSameComponents(slotStack, output) && slotStack.getCount() + output.getCount() <= slotStack.getMaxStackSize()) {
                    slotStack.grow(output.getCount());
                    slots.setItem(outputSlots[slot], slotStack);
                }

                if (UncraftEverything.AUTO_MOVE){
                    player.getInventory().placeItemBackInInventory(slots.getItem(outputSlots[slot]), Prediction.SERVER_ONLY);
                    slots.setItem(outputSlots[slot], ItemStack.EMPTY);
                }
            }
        }

        if (UncraftEverythingConfig.CONFIG.experienceType().equals(UncraftEverythingConfig.ExperienceType.POINT)){
            player.giveExperiencePoints(-getExperience(this.slots));
        }
        else if (UncraftEverythingConfig.CONFIG.experienceType().equals(UncraftEverythingConfig.ExperienceType.LEVEL)){
            player.giveExperiencePoints(-calculateBaseXpFromLevel(getExperience(this.slots)));
        }

        slots.removeItem(0, this.currentRecipe.getInput().getCount());
        setChanged();

        getOutputStacks(this.slots, false);
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            if (!hasNext){
                int fromIndex = page * 7;
                if (fromIndex >= currentRecipes.size()) {
                    fromIndex = currentRecipes.size();
                }
                int toIndex = Math.min(fromIndex + 7, currentRecipes.size());
                Services.NETWORK.sendToPlayer(player, new ClientBoundUncraftingTableDataPayload(getBlockPos(), new ArrayList<>(currentRecipes.subList(fromIndex, toIndex)), currentRecipes.size()));
            }
        }
    }

    public boolean hasRecipe() {
        if (currentRecipes.isEmpty() || currentRecipe == null) {
            return false;
        }

        ItemStack inputStack = slots.getItem(inputSlots[0]);
        if (inputStack.getCount() < currentRecipe.getInput().getCount()) {
            return false;
        }

        List<ItemStack> results = currentRecipe.getOutputs();

        for (ItemStack result : results) {
            if (cannotInsertAmountIntoOutputSlot(result) || cannotInsertItemIntoOutputSlot(result)) {
                return false;
            }
        }

        return checkSlot(results);
    }

    private boolean checkSlot(List<ItemStack> results){
        int count = results.size();
        int emptyCount = 0;

        for (int outputSlot : this.outputSlots) {
            ItemStack stackInSlot = this.slots.getItem(outputSlot);
            if (!stackInSlot.isEmpty()) {
                for (ItemStack result : results) {
                    if (stackInSlot.getItem() == result.getItem()) {
                        if (stackInSlot.getCount() + result.getCount() <= 64) {
                            emptyCount++;
                        }
                    }
                }
            } else {
                emptyCount++;
            }
        }

        return emptyCount >= count;
    }

    private boolean cannotInsertAmountIntoOutputSlot(ItemStack result) {
        for (int outputSlot : this.outputSlots) {
            ItemStack stackInSlot = this.slots.getItem(outputSlot);
            if (stackInSlot.isEmpty() || (ItemStack.isSameItemSameComponents(stackInSlot, result) && stackInSlot.getCount() + result.getCount() <= stackInSlot.getMaxStackSize())) {
                return false;
            }
        }
        return true;
    }

    private boolean cannotInsertItemIntoOutputSlot(ItemStack item) {
        for (int outputSlot : this.outputSlots) {
            ItemStack stackInSlot = this.slots.getItem(outputSlot);
            if (stackInSlot.isEmpty() || ItemStack.isSameItemSameComponents(stackInSlot, item)) {
                return false;
            }
        }

        return true;
    }

    private boolean hasNextRecipe() {
        if (currentRecipes.isEmpty() || currentRecipe == null) {
            return false;
        }

        if (slots.getItem(inputSlots[0]).getCount() - currentRecipe.getInput().getCount() < currentRecipe.getInput().getCount()){
            return false;
        }

        List<ItemStack> results = currentRecipe.getOutputs();

        for (ItemStack result : results) {
            if (cannotInsertAmountIntoOutputSlot(result) || cannotInsertItemIntoOutputSlot(result)) {
                return false;
            }
        }

        return checkSlot(results);
    }

    public List<UncraftingTableRecipe> getCurrentRecipes() {
        return currentRecipes;
    }

    public ContainerData getData() {
        return data;
    }

    public ImplementedInventory getSlots() {
        return slots;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return slots.getSlotsForFace(side);
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @org.jspecify.annotations.Nullable Direction dir) {
        return slots.canPlaceItemThroughFace(slot, stack, dir);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slots.canTakeItemThroughFace(slot, stack, dir);
    }

    @Override
    public int getContainerSize() {
        return slots.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return slots.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slots.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return slots.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return slots.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        slots.setItem(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return slots.stillValid(player);
    }

    @Override
    public void clearContent() {
        slots.clearContent();
    }

    private static class Group {
        List<Integer> positions;
        List<Item> items;

        Group(List<Integer> positions, List<Item> items) {
            this.positions = positions;
            this.items = items;
        }
    }
}
