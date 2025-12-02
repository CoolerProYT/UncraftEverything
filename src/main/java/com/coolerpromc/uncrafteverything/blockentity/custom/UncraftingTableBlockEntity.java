package com.coolerpromc.uncrafteverything.blockentity.custom;

import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingClientConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.ServerPayloadHandler;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableMenu;
import com.coolerpromc.uncrafteverything.util.ModCompatibilities;
import com.coolerpromc.uncrafteverything.util.ModItemStackHandler;
import com.coolerpromc.uncrafteverything.util.Status;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "NullableProblems"})
public class UncraftingTableBlockEntity extends AbstractUncraftingTableBE implements MenuProvider {
    protected final ContainerData data;

    private final ModItemStackHandler inputHandler = new ModItemStackHandler(1){
        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
            getOutputStacks(this, false);
            if (level != null && !level.isClientSide() && player != null) {
                if (currentStack.getItem() != getResource(0).getItem() && !getResource(0).isEmpty()){
                    for (int i = 0; i < getOutputHandler().size(); i++) {
                        ItemStack outputStack = getOutputHandler().copyToList().get(i);
                        if (!outputStack.isEmpty()) {
                            player.getInventory().placeItemBackInInventory(outputStack);
                            getOutputHandler().set(i, ItemResource.EMPTY, 0);
                            setChanged();
                        }
                    }
                }
                currentStack = getResource(0).toStack(getAmountAsInt(0));
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                int fromIndex = page * 7;
                if (fromIndex >= currentRecipes.size()) {
                    fromIndex = currentRecipes.size();
                }
                int toIndex = Math.min(fromIndex + 7, currentRecipes.size());
                PacketDistributor.sendToPlayer(player, new UncraftingTableDataPayload(getBlockPos(), new ArrayList<>(currentRecipes.subList(fromIndex, toIndex)), currentRecipes.size()));
            }
        }

        @Override
        protected int getCapacity(int index, ItemResource resource) {
            int toReturn = ModCompatibilities.getMaxStackSize();
            if (toReturn == -1){
                return super.getCapacity(index, resource);
            }
            return toReturn;
        }
    };

    private final ItemStacksResourceHandler outputHandler = new ItemStacksResourceHandler(9){
        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
            if (player != null){
                handleRecipeSelection(currentRecipe);
            }
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return false;
        }

        @Override
        protected int getCapacity(int index, ItemResource resource) {
            int toReturn = ModCompatibilities.getMaxStackSize();
            if (toReturn == -1){
                return super.getCapacity(index, resource);
            }
            return toReturn;
        }
    };

    public UncraftingTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(UEBlockEntities.UNCRAFTING_TABLE_BE.get(), pos, blockState);
        this.experienceType = UncraftEverythingConfig.CONFIG.experienceType.getRaw() == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0;
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
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);

        inputHandler.serialize(valueOutput.child("input"));
        outputHandler.serialize(valueOutput.child("output"));

        valueOutput.putInt("experience", experience);
        valueOutput.putInt("experienceType", experienceType);
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);

        inputHandler.deserialize(valueInput.childOrEmpty("input"));
        outputHandler.deserialize(valueInput.childOrEmpty("output"));

        experience = valueInput.getIntOr("experience", 0);
        experienceType = valueInput.getIntOr("experienceType", 0);
    }

    public ItemStacksResourceHandler getInputHandler() {
        return inputHandler;
    }

    public ItemStacksResourceHandler getOutputHandler() {
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
            if (inputHandler.getResource(0).isEmpty()){
                this.status = Status.BLANK;
            }
            else {
                if (UncraftEverythingConfig.isItemLocked(player, this.inputHandler.getResource(0).toStack(this.inputHandler.getAmountAsInt(0))).getLeft()){
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
        for (int i = 0; i < this.outputHandler.size(); i++) {
            ItemStack stackInSlot = this.outputHandler.copyToList().get(i);
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
                ItemStack slotStack = outputHandler.getResource(slot).toStack(outputHandler.getAmountAsInt(slot));

                if (slotStack.isEmpty()) {
                    outputHandler.set(slot, ItemResource.of(output.copy()), output.getCount());
                } else if (ItemStack.isSameItemSameComponents(slotStack, output) && slotStack.getCount() + output.getCount() <= slotStack.getMaxStackSize()) {
                    slotStack.grow(output.getCount());
                    outputHandler.set(slot, ItemResource.of(slotStack), slotStack.getCount());
                }

                if (ServerPayloadHandler.AUTO_MOVE){
                    player.getInventory().placeItemBackInInventory(outputHandler.copyToList().get(slot));
                    outputHandler.set(slot, ItemResource.EMPTY, 0);
                }
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
            try(Transaction tx = Transaction.open(null)){
                int count = inputHandler.extract(0,ItemResource.of(this.currentRecipe.getInput().getItem(), this.currentRecipe.getInput().getComponentsPatch()), this.currentRecipe.getInput().getCount(), tx);
                if (count == this.currentRecipe.getInput().getCount()){
                    tx.commit();
                }
            }
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

        ItemStack inputStack = inputHandler.getResource(0).toStack(this.inputHandler.getAmountAsInt(0));
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

        for (int i = 0; i < this.outputHandler.size(); i++) {
            ItemStack stackInSlot = this.outputHandler.copyToList().get(i);
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
        for (int i = 0; i < this.outputHandler.size(); i++) {
            ItemStack stackInSlot = this.outputHandler.copyToList().get(i);
            if (stackInSlot.isEmpty() || (ItemStack.isSameItemSameComponents(stackInSlot, result) && stackInSlot.getCount() + result.getCount() <= stackInSlot.getMaxStackSize())) {
                return false;
            }
        }
        return true;
    }

    private boolean cannotInsertItemIntoOutputSlot(ItemStack item) {
        for (int i = 0; i < this.outputHandler.size(); i++) {
            ItemStack stackInSlot = this.outputHandler.copyToList().get(i);
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

        if (inputHandler.getResource(0).toStack(this.inputHandler.getAmountAsInt(0)).getCount() - currentRecipe.getInput().getCount() < currentRecipe.getInput().getCount()){
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