package com.coolerpromc.uncrafteverything.blockentity.custom;

import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.compat.tacz.TaczCompat;
import com.coolerpromc.uncrafteverything.networking.ServerPayloadHandler;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableMenu;
import com.coolerpromc.uncrafteverything.util.ModCompatibilities;
import com.coolerpromc.uncrafteverything.util.ModItemStackHandler;
import com.coolerpromc.uncrafteverything.util.Status;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.coolerpromc.uncrafteverything.util.UncraftingOutputPlan;
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
import com.coolerpromc.uncrafteverything.platform.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused"})
public class UncraftingTableBlockEntity extends AbstractUncraftingTableBE implements net.minecraft.world.MenuProvider {
    protected final ContainerData data;

    private final ModItemStackHandler inputHandler = new ModItemStackHandler(1){
        @Override
        protected void onContentsChanged(int slot) {
            UncraftingTableBlockEntity.this.setChanged();
            getOutputStacks(this, false);
            if (level != null && !level.isClientSide() && player != null) {
                if (currentStack.getItem() != getStackInSlot(0).getItem() && !getStackInSlot(0).isEmpty()){
                    for (int i = 0; i < getOutputHandler().getSlots(); i++) {
                        ItemStack outputStack = getOutputHandler().getStackInSlot(i);
                        if (!outputStack.isEmpty()) {
                            player.getInventory().placeItemBackInInventory(outputStack);
                            getOutputHandler().setStackInSlot(i, ItemStack.EMPTY);
                            UncraftingTableBlockEntity.this.setChanged();
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
                UncraftingTableDataPayload.INSTANCE.send(() -> player, new UncraftingTableDataPayload(getBlockPos(), new ArrayList<>(currentRecipes.subList(fromIndex, toIndex)), currentRecipes.size()));
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
            UncraftingTableBlockEntity.this.setChanged();
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

    public com.coolerpromc.uncrafteverything.platform.ItemStackHandler automationInventory(@Nullable Direction side) {
        return side == Direction.DOWN ? outputHandler : inputHandler;
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
        // Fabric 1.9.2 stored a single ten-slot Items list (input at slot 0).
        if (!tag.contains("input") && tag.contains("Items", 9)) {
            net.minecraft.core.NonNullList<ItemStack> legacy = net.minecraft.core.NonNullList.withSize(10, ItemStack.EMPTY);
            net.minecraft.world.ContainerHelper.loadAllItems(tag, legacy);
            inputHandler.setStackInSlot(0, legacy.get(0));
            for (int slot = 0; slot < 9; slot++) outputHandler.setStackInSlot(slot, legacy.get(slot + 1));
        }

       
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
        if (status == Status.NOT_ENOUGH_INPUT_ITEM && currentRecipes.isEmpty()) return;

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
        List<ItemStack> plan = UncraftingOutputPlan.create(outputHandler, currentRecipe.getOutputs(), true);
        if (plan == null) return;
        UncraftingOutputPlan.apply(outputHandler, plan);

        if (ServerPayloadHandler.AUTO_MOVE) {
            for (int slot = 0; slot < currentRecipe.getOutputs().size(); slot++) {
                player.getInventory().placeItemBackInInventory(outputHandler.getStackInSlot(slot));
                outputHandler.setStackInSlot(slot, ItemStack.EMPTY);
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
        if ((TaczCompat.isTaczItem(inputStack) || TaczCompat.isTaczItem(currentRecipe.getInput()))
                && !ItemStack.isSameItemSameTags(inputStack, currentRecipe.getInput())) {
            return false;
        }
        if (inputStack.getCount() < currentRecipe.getInput().getCount()) {
            return false;
        }

        return UncraftingOutputPlan.create(outputHandler, currentRecipe.getOutputs(), true) != null;
    }

    private boolean hasNextRecipe() {
        if (currentRecipes.isEmpty() || currentRecipe == null) {
            return false;
        }

        if (inputHandler.getStackInSlot(0).getCount() - currentRecipe.getInput().getCount() < currentRecipe.getInput().getCount()){
            return false;
        }

        return UncraftingOutputPlan.create(outputHandler, currentRecipe.getOutputs(), true) != null;
    }
    public List<UncraftingTableRecipe> getCurrentRecipes() {
        return currentRecipes;
    }

    public ContainerData getData() {
        return data;
    }
    public void writeScreenOpeningData(ServerPlayer player, net.minecraft.network.FriendlyByteBuf buffer) {
        buffer.writeBlockPos(getBlockPos());
    }

}