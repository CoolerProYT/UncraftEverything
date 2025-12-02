package com.coolerpromc.uncrafteverything.blockentity.custom;

import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.ServerPayloadHandler;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableMenu;
import com.coolerpromc.uncrafteverything.util.ModCompatibilities;
import com.coolerpromc.uncrafteverything.util.ModItemStackHandler;
import com.coolerpromc.uncrafteverything.util.Status;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "NullableProblems"})
public class UncraftingTableBlockEntity extends AbstractUncraftingTableBE implements MenuProvider {
    protected final ContainerData data;

    private final ModItemStackHandler inputHandler = new ModItemStackHandler(1){
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            getOutputStacks(this, false);
            if (level != null && !level.isClientSide() && player != null) {
                if (currentStack.getItem() != getStackInSlot(0).getItem() && !getStackInSlot(0).isEmpty()){
                    for (int i = 0; i < getOutputHandler().getSlots(); i++) {
                        ItemStack outputStack = getOutputHandler().getStackInSlot(i);
                        if (!outputStack.isEmpty()) {
                            player.getInventory().placeItemBackInInventory(outputStack);
                            getOutputHandler().setStackInSlot(i, ItemStack.EMPTY);
                            setChanged();
                        }
                    }
                }
                currentStack = getStackInSlot(0);
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                int fromIndex = page * 7;
                if (fromIndex >= currentRecipes.size()) {
                    fromIndex = currentRecipes.size();
                }
                int toIndex = Math.min(fromIndex + 7, currentRecipes.size());
                UncraftingTableDataPayload.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new UncraftingTableDataPayload(getBlockPos(), new ArrayList<>(currentRecipes.subList(fromIndex, toIndex)), currentRecipes.size()));
            }
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            int toReturn = ModCompatibilities.getMaxStackSize();
            if (toReturn == -1){
                return super.getStackLimit(slot, stack);
            }
            return toReturn;
        }
    };

    private final ItemStackHandler outputHandler = new ItemStackHandler(9){
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (player != null){
                handleRecipeSelection(currentRecipe);
            }
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            int toReturn = ModCompatibilities.getMaxStackSize();
            if (toReturn == -1){
                return super.getStackLimit(slot, stack);
            }
            return toReturn;
        }
    };

    public UncraftingTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(UEBlockEntities.UNCRAFTING_TABLE_BE.get(), pos, blockState);
        this.experienceType = UncraftEverythingConfig.CONFIG.experienceType.get() == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0;
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
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.uncrafteverything.uncrafting_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        if (player instanceof ServerPlayer serverPlayer){
            this.player = serverPlayer;
        }
        return new UncraftingTableMenu(containerId, playerInventory, this, data);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER){
            if (side == Direction.DOWN){
                return LazyOptional.of(() -> outputHandler).cast();
            }

            return LazyOptional.of(() -> inputHandler).cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);

        tag.put("input", inputHandler.serializeNBT());
        tag.put("output", outputHandler.serializeNBT());

        tag.putInt("experience", experience);
        tag.putInt("experienceType", experienceType);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);

        inputHandler.deserializeNBT(tag.getCompound("input"));
        outputHandler.deserializeNBT(tag.getCompound("output"));
       
        experience = tag.getInt("experience");
        experienceType = tag.getInt("experienceType");
    }

    public ItemStackHandler getInputHandler() {
        return inputHandler;
    }

    public ItemStackHandler getOutputHandler() {
        return outputHandler;
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
            if (inputHandler.getStackInSlot(0).isEmpty()){
                this.status = Status.BLANK;
            }
            else {
                if (UncraftEverythingConfig.isItemLocked(player, this.inputHandler.getStackInSlot(0)).getLeft()){
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
        if (UncraftEverythingConfig.CONFIG.experienceType.get().equals(UncraftEverythingConfig.ExperienceType.POINT)){
            return player.totalExperience >= getExperience(this.inputHandler) || player.isCreative();
        }
        else if (UncraftEverythingConfig.CONFIG.experienceType.get().equals(UncraftEverythingConfig.ExperienceType.LEVEL)){
            return player.experienceLevel >= getExperience(this.inputHandler) || player.isCreative();
        }
        return true;
    }

    private int findSuitableOutputSlot(ItemStack result) {
        for (int i = 0; i < this.outputHandler.getSlots(); i++) {
            ItemStack stackInSlot = this.outputHandler.getStackInSlot(i).copy();
            if (stackInSlot.isEmpty() || (ItemStack.isSameItemSameTags(stackInSlot, result) && stackInSlot.getCount() + result.getCount() <= stackInSlot.getMaxStackSize())) {
                return i;
            }
        }
        return -1;
    }

    private void processUncraft(boolean hasNext){
        List<ItemStack> outputs = currentRecipe.getOutputs();

        for (int i = 0; i < outputs.size(); i++) {
            ItemStack output = outputs.get(i);
            if (i < outputHandler.getSlots()) {
                ItemStack slotStack = outputHandler.getStackInSlot(i);

                if (slotStack.isEmpty()) {
                    outputHandler.setStackInSlot(i, output.copy());
                } else if (ItemStack.isSameItemSameTags(slotStack, output) && slotStack.getCount() + output.getCount() <= slotStack.getMaxStackSize()) {
                    slotStack.grow(output.getCount());
                    outputHandler.setStackInSlot(i, slotStack);
                }
            }

            if (ServerPayloadHandler.AUTO_MOVE){
                player.getInventory().placeItemBackInInventory(outputHandler.getStackInSlot(i));
                outputHandler.setStackInSlot(i, ItemStack.EMPTY);
            }
        }

        if (UncraftEverythingConfig.CONFIG.experienceType.get().equals(UncraftEverythingConfig.ExperienceType.POINT)){
            player.giveExperiencePoints(-getExperience(this.inputHandler));
        }
        else if (UncraftEverythingConfig.CONFIG.experienceType.get().equals(UncraftEverythingConfig.ExperienceType.LEVEL)){
            player.giveExperiencePoints(-calculateBaseXpFromLevel(getExperience(this.inputHandler)));
        }

        if (hasNext){
            inputHandler.extractItemWithoutTriggerChanges(0, this.currentRecipe.getInput().getCount(), false);
        }
        else{
            inputHandler.extractItem(0, this.currentRecipe.getInput().getCount(), false);
        }
        setChanged();

        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public boolean hasRecipe() {
        if (currentRecipes.isEmpty() || currentRecipe == null) {
            return false;
        }

        ItemStack inputStack = inputHandler.getStackInSlot(0);
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

        for (int i = 0; i < this.outputHandler.getSlots(); i++) {
            ItemStack stackInSlot = this.outputHandler.getStackInSlot(i).copy();
            if(!stackInSlot.isEmpty()){
                for (ItemStack result : results){
                    if(stackInSlot.getItem() == result.getItem()){
                        if(stackInSlot.getCount() + result.getCount() <= 64){
                            emptyCount++;
                        }
                    }
                }
            }
            else {
                emptyCount++;
            }
        }

        return emptyCount >= count;
    }

    private boolean cannotInsertAmountIntoOutputSlot(ItemStack result) {
        for (int i = 0; i < this.outputHandler.getSlots(); i++) {
            ItemStack stackInSlot = this.outputHandler.getStackInSlot(i).copy();
            if (stackInSlot.isEmpty() || (ItemStack.isSameItemSameTags(stackInSlot, result) && stackInSlot.getCount() + result.getCount() <= stackInSlot.getMaxStackSize())) {
                return false;
            }
        }
        return true;
    }

    private boolean cannotInsertItemIntoOutputSlot(ItemStack item) {
        for (int i = 0; i < this.outputHandler.getSlots(); i++) {
            ItemStack stackInSlot = this.outputHandler.getStackInSlot(i).copy();
            if (stackInSlot.isEmpty() || ItemStack.isSameItemSameTags(stackInSlot, item)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasNextRecipe() {
        if (currentRecipes.isEmpty() || currentRecipe == null) {
            return false;
        }

        if (inputHandler.getStackInSlot(0).getCount() - currentRecipe.getInput().getCount() < currentRecipe.getInput().getCount()){
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
}