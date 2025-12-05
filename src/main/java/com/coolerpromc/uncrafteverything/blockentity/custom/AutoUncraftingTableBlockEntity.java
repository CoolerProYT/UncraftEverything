package com.coolerpromc.uncrafteverything.blockentity.custom;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.screen.custom.AutoUncraftingTableMenu;
import com.coolerpromc.uncrafteverything.util.*;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coolerpromc.uncrafteverything.block.custom.AutoUncraftingTableBlock.ACTIVE;

@SuppressWarnings({"unused", "NullableProblems"})
public class AutoUncraftingTableBlockEntity extends AbstractUncraftingTableBE implements MenuProvider {
    protected final ContainerData data;
    private final Map<Holder<Item>, RecipeSelectionHistory> recipeSelectionHistory = new HashMap<>();
    private int totalExperience = 0;
    private int experienceLevel = 0;
    private int experienceProgress = 0;
    private boolean isActive = false;
    public int index = 0;
    private boolean byPass = false;

    private int typeToAdd = 0; // 0 = LEVEL, 1 = POINT
    private int amountToAdd = 1;

    private final ModItemStackHandler inputHandler = new ModItemStackHandler(1){
        ItemStack previousContents = ItemStack.EMPTY;

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            super.setStackInSlot(slot, stack);
            if (level != null && level.isClientSide()) return;
            ItemStack newStack = getStackInSlot(0);
            previousContents = newStack.copy();
        }

        @Override
        protected void onContentsChanged(int i) {
            if (level != null && !level.isClientSide()){
                setChanged();
                getOutputStacks(this, true);
                byPass = false;
                currentStack = getStackInSlot(0);
                RecipeSelectionHistory history = recipeSelectionHistory.get(currentStack.getItemHolder());
                boolean sendPacket = false;

                if (player != null) {
                    if (!(history != null && history.patch().equals(currentStack.getComponentsPatch()))){
                        page = 0;
                        index = 0;
                        sendPacket = true;
                    }
                    else if (history.patch().equals(currentStack.getComponentsPatch())){
                        page = history.page();
                        index = history.index();
                        data.set(9, index);
                        currentRecipe = history.recipe();
                        if (hasRecipe()) status = Status.BLANK;
                    }
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                    int fromIndex = page * 7;
                    if (fromIndex >= currentRecipes.size()) {
                        fromIndex = currentRecipes.size();
                    }
                    int toIndex = Math.min(fromIndex + 7, currentRecipes.size());
                    UncraftEverything.CHANNEL.send(new UncraftingTableDataPayload(getBlockPos(), new ArrayList<>(currentRecipes.subList(fromIndex, toIndex)), currentRecipes.size(), sendPacket), PacketDistributor.PLAYER.with(player));
                }
                else{
                    if (history != null && history.patch().equals(currentStack.getComponentsPatch())){
                        currentRecipe = history.recipe();
                        byPass = true;
                    }
                }
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            int toReturn = ModCompatibilities.getMaxStackSize();
            if (toReturn == -1){
                return super.getSlotLimit(slot);
            }
            return toReturn;
        }
    };

    private final ItemStackHandler outputHandler = new ItemStackHandler(9){
        @Override
        protected void onContentsChanged(int i) {
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
        public int getSlotLimit(int slot) {
            int toReturn = ModCompatibilities.getMaxStackSize();
            if (toReturn == -1){
                return super.getSlotLimit(slot);
            }
            return toReturn;
        }
    };

    public AutoUncraftingTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(UEBlockEntities.AUTO_UNCRAFTING_TABLE_BE.get(), pos, blockState);
        this.experienceType = UncraftEverythingConfig.CONFIG.experienceType.get() == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0;
        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i){
                    case 0 -> experience;
                    case 1 -> experienceType;
                    case 2 -> status.getIndex();
                    case 3 -> totalExperience;
                    case 4 -> experienceLevel;
                    case 5 -> experienceProgress;
                    case 6 -> typeToAdd;
                    case 7 -> amountToAdd;
                    case 8 -> page;
                    case 9 -> index;
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i){
                    case 0 -> experience = value;
                    case 1 -> experienceType = value;
                    case 2 -> status = Status.byIndex(value);
                    case 3 -> totalExperience = value;
                    case 4 -> experienceLevel = value;
                    case 5 -> experienceProgress = value;
                    case 6 -> typeToAdd = value;
                    case 7 -> amountToAdd = value;
                    case 8 -> page = value;
                    case 9 -> index = value;
                }
                setChanged();
            }

            @Override
            public int getCount() {
                return 10;
            }
        };
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
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.uncrafteverything.auto_uncrafting_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        if (player instanceof ServerPlayer serverPlayer){
            this.player = serverPlayer;
        }
        return new AutoUncraftingTableMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);

        NonNullList<ItemStack> inputStacks = NonNullList.withSize(inputHandler.getSlots(), ItemStack.EMPTY);
        for (int i = 0; i < inputHandler.getSlots(); i++) {
            inputStacks.set(i, inputHandler.getStackInSlot(i));
        }
        NonNullList<ItemStack> outputStacks = NonNullList.withSize(outputHandler.getSlots(), ItemStack.EMPTY);
        for (int i = 0; i < outputHandler.getSlots(); i++) {
            outputStacks.set(i, outputHandler.getStackInSlot(i));
        }
        ContainerHelper.saveAllItems(valueOutput.child("input"), inputStacks);
        ContainerHelper.saveAllItems(valueOutput.child("output"), outputStacks);
        valueOutput.putInt("experience", experience);
        valueOutput.putInt("experienceType", experienceType);
        valueOutput.putInt("totalExperience", totalExperience);
        valueOutput.putInt("experienceLevel", experienceLevel);
        valueOutput.putInt("experienceProgress", experienceProgress);
        valueOutput.putInt("typeToAdd", typeToAdd);
        valueOutput.putInt("amountToAdd", amountToAdd);
        valueOutput.putInt("page", page);
        valueOutput.putBoolean("isActive", isActive);
        valueOutput.storeNullable("recipe", UncraftingTableRecipe.CODEC, currentRecipe);
        valueOutput.putInt("index", index);
        valueOutput.store("history", Codec.unboundedMap(Item.CODEC, RecipeSelectionHistory.CODEC), recipeSelectionHistory);
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);

        NonNullList<ItemStack> inputStacks = NonNullList.withSize(inputHandler.getSlots(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(valueInput.childOrEmpty("input"), inputStacks);
        for (int i = 0; i < inputHandler.getSlots(); i++) {
            inputHandler.setStackInSlot(i, inputStacks.get(i));
        }
        NonNullList<ItemStack> outputStacks = NonNullList.withSize(outputHandler.getSlots(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(valueInput.childOrEmpty("output"), outputStacks);
        for (int i = 0; i < outputHandler.getSlots(); i++) {
            outputHandler.setStackInSlot(i, outputStacks.get(i));
        }

        experience = valueInput.getIntOr("experience", 0);
        experienceType = valueInput.getIntOr("experienceType", 0);
        totalExperience = valueInput.getIntOr("totalExperience", 0);
        experienceLevel = valueInput.getIntOr("experienceLevel", 0);
        experienceProgress = valueInput.getIntOr("experienceProgress", 0);
        typeToAdd = valueInput.getIntOr("typeToAdd", 0);
        amountToAdd = valueInput.getIntOr("amountToAdd", 1);
        page = valueInput.getIntOr("page", 0);
        isActive = valueInput.getBooleanOr("isActive", false);
        currentRecipe = valueInput.read("recipe", UncraftingTableRecipe.CODEC).orElse(null);
        index = valueInput.getIntOr("index", 0);
        recipeSelectionHistory.putAll(valueInput.read("history", Codec.unboundedMap(Item.CODEC, RecipeSelectionHistory.CODEC)).orElse(new HashMap<>()));
    }

    public ItemStackHandler getInputHandler() {
        return inputHandler;
    }

    public ItemStackHandler getOutputHandler() {
        return outputHandler;
    }

    public int getTotalExperience() {
        return totalExperience;
    }

    public int getExperienceLevel() {
        return experienceLevel;
    }

    public int getTotalXpForLevel() {
        return calculateBaseXpFromLevel(experienceLevel);
    }

    public int getLevelFromTotalXp() {
        if (totalExperience <= 352) {
            return (int) (Math.sqrt(totalExperience + 9) - 3);
        } else if (totalExperience <= 1507) {
            return (int) (8.1 + Math.sqrt((2D/5D) * (totalExperience - (7839D/40D))));
        } else {
            return (int) (18.06 + Math.sqrt((2D/9D) * (totalExperience - (54215D/72D))));
        }
    }

    public void addExperienceLevels(int levels, ServerPlayer player) {
        int totalExp = calculateBaseXpFromLevel(Math.abs(levels));
        if (levels > 0){
            addExperiencePoints(totalExp, player);
        }
        else{
            addExperiencePoints(-totalExp, player);
        }
    }

    public void addExperiencePoints(int xpPoints, ServerPlayer player) {
        boolean process = true;

        if (xpPoints > 0) {
            if (player.totalExperience >= xpPoints) {
                player.giveExperiencePoints(-xpPoints);
            } else {
                process = false;
            }
        } else {
            int xpToGive = -xpPoints;
            if (this.totalExperience >= xpToGive) {
                player.giveExperiencePoints(xpToGive);
            } else {
                process = false;
            }
        }

        if (process) {
            this.totalExperience = Mth.clamp(this.totalExperience + xpPoints, 0, Integer.MAX_VALUE);
            this.experienceLevel = getLevelFromTotalXp();
            int baseXp = getTotalXpForLevel();
            int nextRequired = getXpNeededForNextLevel();
            this.experienceProgress = (int) ((nextRequired > 0 ? (float)(this.totalExperience - baseXp) / (float)nextRequired : 0.0f) * 125f);
            checkExpStatus();
            setChanged();
        }
    }

    public void removeExperienceLevels(int levels) {
        int xpToRemove = calculateBaseXpFromLevel(Math.abs(levels));
        removeExperiencePoints(-xpToRemove);
    }

    public void removeExperiencePoints(int xpPoints) {
        this.totalExperience = Mth.clamp(this.totalExperience + xpPoints, 0, Integer.MAX_VALUE);
        this.experienceLevel = getLevelFromTotalXp();
        int baseXp = getTotalXpForLevel();
        int nextRequired = getXpNeededForNextLevel();
        this.experienceProgress = (int) ((nextRequired > 0 ? (float)(this.totalExperience - baseXp) / (float)nextRequired : 0.0f) * 125f);

        checkExpStatus();
        setChanged();
    }

    public int getXpNeededForNextLevel() {
        if (this.experienceLevel >= 30) {
            return 112 + (this.experienceLevel - 30) * 9;
        } else {
            return this.experienceLevel >= 15 ? 37 + (this.experienceLevel - 15) * 5 : 7 + this.experienceLevel * 2;
        }
    }

    public int getExperienceProgress() {
        return experienceProgress;
    }

    public void tick(Level level, BlockPos pos, BlockState state){
        if (!level.isClientSide()){
            if (isActive && hasRecipe() && hasEnoughExperience()){
                level.setBlock(pos, state.setValue(ACTIVE, true), 3);
                processUncraft();
            }
            else{
                level.setBlock(pos, state.setValue(ACTIVE, false), 3);
            }
        }
    }

    @Override
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
            checkExpStatus();
            if (!this.currentStack.isEmpty()){
                recipeSelectionHistory.put(currentStack.getItemHolder(), new RecipeSelectionHistory(recipe, page, index, currentRecipes.size(), currentStack.getComponentsPatch()));
            }
        }
    }

    public void checkExpStatus(){
        if (hasEnoughExperience() || !hasRecipe()){
            this.status = Status.BLANK;
        }
        else{
            this.status = Status.NOT_ENOUGH_EXP;
        }
    }

    @Override
    public boolean hasEnoughExperience(){
        if (UncraftEverythingConfig.CONFIG.experienceType.get().equals(UncraftEverythingConfig.ExperienceType.POINT)){
            return totalExperience >= getExperience(this.inputHandler);
        }
        else if (UncraftEverythingConfig.CONFIG.experienceType.get().equals(UncraftEverythingConfig.ExperienceType.LEVEL)){
            return experienceLevel >= getExperience(this.inputHandler);
        }
        return true;
    }

    private int findSuitableOutputSlot(ItemStack result) {
        for (int i = 0; i < this.outputHandler.getSlots(); i++) {
            ItemStack stackInSlot = this.outputHandler.getStackInSlot(i).copy();
            if (stackInSlot.isEmpty() || (ItemStack.isSameItemSameComponents(stackInSlot, result) && stackInSlot.getCount() + result.getCount() <= stackInSlot.getMaxStackSize())) {
                return i;
            }
        }
        return -1;
    }

    private void processUncraft(){
        List<ItemStack> outputs = currentRecipe.getOutputs();

        for (ItemStack output : outputs) {
            int slot = this.findSuitableOutputSlot(output);
            if (slot != -1) {
                ItemStack slotStack = outputHandler.getStackInSlot(slot);

                if (slotStack.isEmpty()) {
                    outputHandler.setStackInSlot(slot, output.copy());
                } else if (ItemStack.isSameItemSameComponents(slotStack, output) && slotStack.getCount() + output.getCount() <= slotStack.getMaxStackSize()) {
                    slotStack.grow(output.getCount());
                    outputHandler.setStackInSlot(slot, slotStack);
                }
            }
        }

        if (UncraftEverythingConfig.CONFIG.experienceType.get().equals(UncraftEverythingConfig.ExperienceType.POINT)){
            removeExperiencePoints(-getExperience(this.inputHandler));
        }
        else if (UncraftEverythingConfig.CONFIG.experienceType.get().equals(UncraftEverythingConfig.ExperienceType.LEVEL)){
            removeExperienceLevels(-getExperience(this.inputHandler));
        }

        inputHandler.extractItem(0, this.currentRecipe.getInput().getCount(), false);
        setChanged();

        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public boolean hasRecipe() {
        if ((currentRecipes.isEmpty() && !byPass) || currentRecipe == null) {
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
            if (stackInSlot.isEmpty() || (ItemStack.isSameItemSameComponents(stackInSlot, result) && stackInSlot.getCount() + result.getCount() <= stackInSlot.getMaxStackSize())) {
                return false;
            }
        }
        return true;
    }

    private boolean cannotInsertItemIntoOutputSlot(ItemStack item) {
        for (int i = 0; i < this.outputHandler.getSlots(); i++) {
            ItemStack stackInSlot = this.outputHandler.getStackInSlot(i).copy();
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

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setAmountToAdd(int amountToAdd) {
        this.amountToAdd = amountToAdd;
    }

    public void setTypeToAdd(UncraftEverythingConfig.ExperienceType typeToAdd) {
        this.typeToAdd = typeToAdd == UncraftEverythingConfig.ExperienceType.LEVEL ? 0 : 1;
    }
}