package com.coolerpromc.uncrafteverything.blockentity.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingClientConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableMenu;
import com.coolerpromc.uncrafteverything.util.ImplementedInventory;
import com.coolerpromc.uncrafteverything.util.Status;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"unused"})
public class UncraftingTableBlockEntity extends AbstractUncraftingTableBE implements ExtendedScreenHandlerFactory<BlockPos> {
    private final PropertyDelegate data;

    private final int[] inputSlots = {0};
    private final int[] outputSlots = {1, 2, 3, 4, 5, 6, 7, 8, 9};

    private final ImplementedInventory slots = new ImplementedInventory(10){
        @Override
        public void setStack(int slot, ItemStack stack) {
            super.setStack(slot, stack);
            getOutputStacks(this, false);
            if (world != null && !world.isClient() && slot == inputSlots[0] && player != null) {
                if (currentStack.getItem() != this.getStack(0).getItem() && !this.getStack(0).isEmpty()){
                    for (int outputSlot : outputSlots) {
                        ItemStack outputStack = this.getStack(outputSlot);
                        if (!outputStack.isEmpty()) {
                            player.getInventory().offerOrDrop(outputStack);
                            this.setStack(outputSlot, ItemStack.EMPTY);
                            markDirty();
                        }
                    }
                }
                currentStack = this.getStack(0);
                world.updateListeners(pos, getCachedState(), getCachedState(), 3);
                int fromIndex = page * 7;
                if (fromIndex >= currentRecipes.size()) {
                    fromIndex = currentRecipes.size();
                }
                int toIndex = Math.min(fromIndex + 7, currentRecipes.size());
                ServerPlayNetworking.send(player, new UncraftingTableDataPayload(getPos(), new ArrayList<>(currentRecipes.subList(fromIndex, toIndex)), currentRecipes.size()));
                player.currentScreenHandler.sendContentUpdates();
            }
        }

        @Override
        public ItemStack removeStack(int slot) {
            getOutputStacks(this, false);
            if (slot != 0){
                if (player != null){
                    handleRecipeSelection(currentRecipe);
                }
            }
            return super.removeStack(slot);
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) {
            return slot == inputSlots[0] && side != Direction.DOWN;
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction side) {
            return slot != inputSlots[0] && side == Direction.DOWN;
        }

        @Override
        public boolean isValid(int slot, ItemStack stack) {
            return Arrays.stream(outputSlots).noneMatch(value -> value == slot);
        }
    };

    public UncraftingTableBlockEntity(BlockPos pos, BlockState state) {
        super(UEBlockEntities.UNCRAFTING_TABLE_BE, pos, state);
        this.experienceType = UncraftEverythingConfig.experienceType == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0;
        this.data = new PropertyDelegate() {
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
            public int size() {
                return 3;
            }
        };
    }

    @Override
    public @NotNull BlockPos getScreenOpeningData(@NotNull ServerPlayerEntity serverPlayerEntity) {
        return pos;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.uncrafteverything.uncrafting_table");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer){
            this.player = serverPlayer;
        }
        return new UncraftingTableMenu(syncId, playerInventory, this, data);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);

        Inventories.writeData(view, slots.heldStacks);

        view.putInt("experience", experience);
        view.putInt("experienceType", experienceType);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);

        Inventories.readData(view, slots.heldStacks);

        experience = view.getInt("experience", UncraftEverythingConfig.experience);
        experienceType = view.getInt("experienceType", UncraftEverythingConfig.experienceType == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0);
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
            if (slots.getStack(0).isEmpty()){
                this.status = Status.BLANK;
            }
            else {
                if (UncraftEverythingConfig.isItemLocked(player, slots.getStack(inputSlots[0])).getLeft()){
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
        if (UncraftEverythingConfig.experienceType.equals(UncraftEverythingConfig.ExperienceType.POINT)){
            return player.totalExperience >= getExperience(this.slots) || player.isCreative();
        }
        else if (UncraftEverythingConfig.experienceType.equals(UncraftEverythingConfig.ExperienceType.LEVEL)){
            return player.experienceLevel >= getExperience(this.slots) || player.isCreative();
        }
        return true;
    }

    private int findSuitableOutputSlot(ItemStack result) {
        for (int i = 0; i < this.outputSlots.length; i++) {
            ItemStack stackInSlot = this.slots.getStack(outputSlots[i]);
            if (stackInSlot.isEmpty() || (ItemStack.areItemsAndComponentsEqual(stackInSlot, result) && stackInSlot.getCount() + result.getCount() <= stackInSlot.getMaxCount())) {
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
                ItemStack slotStack = slots.getStack(outputSlots[slot]);

                if (slotStack.isEmpty()) {
                    slots.setStack(outputSlots[slot], output.copy());
                } else if (ItemStack.areItemsAndComponentsEqual(slotStack, output) && slotStack.getCount() + output.getCount() <= slotStack.getMaxCount()) {
                    slotStack.increment(output.getCount());
                    slots.setStack(outputSlots[slot], slotStack);
                }

                if (UncraftEverything.AUTO_MOVE){
                    player.getInventory().offerOrDrop(slots.getStack(outputSlots[slot]));
                    slots.setStack(outputSlots[slot], ItemStack.EMPTY);
                }
            }
        }

        if (UncraftEverythingConfig.experienceType.equals(UncraftEverythingConfig.ExperienceType.POINT)){
            player.addExperience(-getExperience(this.slots));
        }
        else if (UncraftEverythingConfig.experienceType.equals(UncraftEverythingConfig.ExperienceType.LEVEL)){
            player.addExperience(-calculateBaseXpFromLevel(getExperience(this.slots)));
        }

        slots.removeStack(0, this.currentRecipe.getInput().getCount());
        markDirty();

        getOutputStacks(this.slots, false);
        if (world != null && !world.isClient()) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
            if (!hasNext){
                int fromIndex = page * 7;
                if (fromIndex >= currentRecipes.size()) {
                    fromIndex = currentRecipes.size();
                }
                int toIndex = Math.min(fromIndex + 7, currentRecipes.size());
                ServerPlayNetworking.send(player, new UncraftingTableDataPayload(getPos(), new ArrayList<>(currentRecipes.subList(fromIndex, toIndex)), currentRecipes.size()));
            }
        }
    }

    public boolean hasRecipe() {
        if (currentRecipes.isEmpty() || currentRecipe == null) {
            return false;
        }

        ItemStack inputStack = slots.getStack(inputSlots[0]);
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
            ItemStack stackInSlot = this.slots.getStack(outputSlot);
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
            ItemStack stackInSlot = this.slots.getStack(outputSlot);
            if (stackInSlot.isEmpty() || (ItemStack.areItemsAndComponentsEqual(stackInSlot, result) && stackInSlot.getCount() + result.getCount() <= stackInSlot.getMaxCount())) {
                return false;
            }
        }
        return true;
    }

    private boolean cannotInsertItemIntoOutputSlot(ItemStack item) {
        for (int outputSlot : this.outputSlots) {
            ItemStack stackInSlot = this.slots.getStack(outputSlot);
            if (stackInSlot.isEmpty() || ItemStack.areItemsAndComponentsEqual(stackInSlot, item)) {
                return false;
            }
        }

        return true;
    }

    private boolean hasNextRecipe() {
        if (currentRecipes.isEmpty() || currentRecipe == null) {
            return false;
        }

        if (slots.getStack(inputSlots[0]).getCount() - currentRecipe.getInput().getCount() < currentRecipe.getInput().getCount()){
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

    public PropertyDelegate getData() {
        return data;
    }

    public ImplementedInventory getSlots() {
        return slots;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return slots.getAvailableSlots(side);
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @org.jspecify.annotations.Nullable Direction dir) {
        return slots.canInsert(slot, stack, dir);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slots.canExtract(slot, stack, dir);
    }

    @Override
    public int size() {
        return slots.size();
    }

    @Override
    public boolean isEmpty() {
        return slots.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return slots.getStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return slots.removeStack(slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return slots.removeStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        slots.setStack(slot, stack);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return slots.canPlayerUse(player);
    }

    @Override
    public void clear() {
        slots.clear();
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
