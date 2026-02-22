package com.coolerpromc.uncrafteverything.blockentity.custom;

import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.screen.custom.AutoUncraftingTableMenu;
import com.coolerpromc.uncrafteverything.util.*;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.coolerpromc.uncrafteverything.block.custom.AutoUncraftingTableBlock.ACTIVE;

@SuppressWarnings({"unused", "NullableProblems"})
public class AutoUncraftingTableBlockEntity extends AbstractUncraftingTableBE implements MenuProvider {
    protected final ContainerData data;
    private final Map<ResourceLocation, RecipeSelectionHistory> recipeSelectionHistory = new HashMap<>();
    private int totalExperience = 0;
    private int experienceLevel = 0;
    private int experienceProgress = 0;
    private boolean isActive = false;
    public int index = 0;
    private boolean byPass = false;

    private int typeToAdd = 0; // 0 = LEVEL, 1 = POINT
    private int amountToAdd = 1;

    private final ModItemStackHandler inputHandler = new ModItemStackHandler(1){
        @Override
        protected void onContentsChanged(int i) {
            if (level != null && !level.isClientSide()){
                setChanged();
                getOutputStacks(this, true);
                byPass = false;
                currentStack = getStackInSlot(0);
                RecipeSelectionHistory history = recipeSelectionHistory.get(ForgeRegistries.ITEMS.getKey(currentStack.getItem()));
                boolean sendPacket = false;

                if (player != null) {
                    if (!(history != null && history.tag().equals(currentStack.copy().getOrCreateTag()))){
                        page = 0;
                        index = 0;
                        sendPacket = true;
                    }
                    else if (history.tag().equals(currentStack.copy().getOrCreateTag())){
                        page = history.page();
                        index = history.index();
                        data.set(9, index);
                        currentRecipe = history.recipe();
                    }
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                    int fromIndex = page * 7;
                    if (fromIndex >= currentRecipes.size()) {
                        fromIndex = currentRecipes.size();
                    }
                    int toIndex = Math.min(fromIndex + 7, currentRecipes.size());
                    UncraftingTableDataPayload.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new UncraftingTableDataPayload(getBlockPos(), new ArrayList<>(currentRecipes.subList(fromIndex, toIndex)), currentRecipes.size(), sendPacket));
                }
                else {
                    if (history != null && history.tag().equals(currentStack.copy().getOrCreateTag())) {
                        currentRecipe = history.recipe();
                        byPass = true;
                    }
                }
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
        public boolean isItemValid(int slot, ItemStack stack) {
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
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.put("input", inputHandler.serializeNBT());
        tag.put("output", outputHandler.serializeNBT());

        tag.putInt("experience", experience);
        tag.putInt("experienceType", experienceType);
        tag.putInt("totalExperience", totalExperience);
        tag.putInt("experienceLevel", experienceLevel);
        tag.putInt("experienceProgress", experienceProgress);
        tag.putInt("typeToAdd", typeToAdd);
        tag.putInt("amountToAdd", amountToAdd);
        tag.putInt("page", page);
        tag.putBoolean("isActive", isActive);
        if (currentRecipe != null){
            tag.put("recipe", UncraftingTableRecipe.CODEC.encodeStart(NbtOps.INSTANCE, currentRecipe).getOrThrow(false, System.out::println));
        }
        tag.putInt("index", index);
        tag.put("history", Codec.unboundedMap(ResourceLocation.CODEC, RecipeSelectionHistory.CODEC).encodeStart(NbtOps.INSTANCE, recipeSelectionHistory).getOrThrow(false, System.out::println));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        inputHandler.deserializeNBT(tag.getCompound("input"));
        outputHandler.deserializeNBT(tag.getCompound("output"));

        experience = tag.getInt("experience");
        experienceType = tag.getInt("experienceType");
        totalExperience = tag.getInt("totalExperience");
        experienceLevel = tag.getInt("experienceLevel");
        experienceProgress = tag.getInt("experienceProgress");
        typeToAdd = tag.getInt("typeToAdd");
        amountToAdd = tag.getInt("amountToAdd");
        page = tag.getInt("page");
        isActive = tag.getBoolean("isActive");
        if (tag.contains("recipe")){
            currentRecipe = UncraftingTableRecipe.CODEC.parse(NbtOps.INSTANCE, tag.getCompound("recipe")).getOrThrow(false, System.out::println);
        }
        index = tag.getInt("index");
        recipeSelectionHistory.putAll(Codec.unboundedMap(ResourceLocation.CODEC, RecipeSelectionHistory.CODEC).parse(NbtOps.INSTANCE, tag.getCompound("history")).getOrThrow(false, System.out::println));
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
                recipeSelectionHistory.put(ForgeRegistries.ITEMS.getKey(currentStack.getItem()), new RecipeSelectionHistory(recipe, page, index, currentRecipes.size(), currentStack.getOrCreateTag()));
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
            if (stackInSlot.isEmpty() || (ItemStack.isSameItemSameTags(stackInSlot, result) && stackInSlot.getCount() + result.getCount() <= stackInSlot.getMaxStackSize())) {
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
                ItemStack slotStack = outputHandler.getStackInSlot(slot).copy();

                if (slotStack.isEmpty()) {
                    outputHandler.setStackInSlot(slot, output.copy());
                } else if (ItemStack.isSameItemSameTags(slotStack, output) && slotStack.getCount() + output.getCount() <= slotStack.getMaxStackSize()) {
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

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setAmountToAdd(int amountToAdd) {
        this.amountToAdd = amountToAdd;
    }

    public void setTypeToAdd(UncraftEverythingConfig.ExperienceType typeToAdd) {
        this.typeToAdd = typeToAdd == UncraftEverythingConfig.ExperienceType.LEVEL ? 0 : 1;
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
}