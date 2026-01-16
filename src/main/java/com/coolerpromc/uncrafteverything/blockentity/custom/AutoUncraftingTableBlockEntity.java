package com.coolerpromc.uncrafteverything.blockentity.custom;

import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.screen.custom.AutoUncraftingTableMenu;
import com.coolerpromc.uncrafteverything.util.ImplementedInventory;
import com.coolerpromc.uncrafteverything.util.RecipeSelectionHistory;
import com.coolerpromc.uncrafteverything.util.Status;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
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
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coolerpromc.uncrafteverything.block.custom.AutoUncraftingTableBlock.ACTIVE;

@SuppressWarnings({"unused", "NullableProblems"})
public class AutoUncraftingTableBlockEntity extends AbstractUncraftingTableBE implements ExtendedMenuProvider<BlockPos> {
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

    private final ImplementedInventory inputHandler = new ImplementedInventory(1, level){

        @Override
        public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
            return dir != Direction.DOWN;
        }

        @Override
        public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
            return false;
        }

        @Override
        public void onContentChanged(ItemStack previousContents) {
            if (level != null && !level.isClientSide()){
                setChanged();
                getOutputStacks(this, true);
                byPass = false;
                currentStack = getItem(0);
                RecipeSelectionHistory history = recipeSelectionHistory.get(currentStack.typeHolder());

                if (player != null) {
                    if (!currentStack.is(previousContents.getItem())){
                        if (!(history != null && history.patch().equals(currentStack.getComponentsPatch()))){
                            page = 0;
                            index = 0;
                        }
                        else if (history.patch().equals(currentStack.getComponentsPatch())){
                            page = history.page();
                            index = history.index();
                            data.set(9, index);
                            currentRecipe = history.recipe();
                        }
                    }
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                    int fromIndex = page * 7;
                    if (fromIndex >= currentRecipes.size()) {
                        fromIndex = currentRecipes.size();
                    }
                    int toIndex = Math.min(fromIndex + 7, currentRecipes.size());
                    ServerPlayNetworking.send(player, new UncraftingTableDataPayload(getBlockPos(), new ArrayList<>(currentRecipes.subList(fromIndex, toIndex)), currentRecipes.size(), false));
                }
                else{
                    if (history != null && history.patch().equals(currentStack.getComponentsPatch())){
                        currentRecipe = history.recipe();
                        byPass = true;
                    }
                }
            }
        }
    };

    private final ImplementedInventory outputHandler = new ImplementedInventory(9){
        @Override
        public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
            return false;
        }

        @Override
        public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
            return dir == Direction.DOWN;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            super.setItem(slot, stack);
            if (player != null){
                handleRecipeSelection(currentRecipe);
            }
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return false;
        }
    };

    public AutoUncraftingTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(UEBlockEntities.AUTO_UNCRAFTING_TABLE_BE, pos, blockState);
        this.experienceType = UncraftEverythingConfig.experienceType == UncraftEverythingConfig.ExperienceType.LEVEL ? 1 : 0;
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
    public Component getDisplayName() {
        return Component.translatable("block.uncrafteverything.auto_uncrafting_table");
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer serverPlayerEntity) {
        return getBlockPos();
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
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);

        ContainerHelper.saveAllItems(view.child("input"), this.inputHandler.items);
        ContainerHelper.saveAllItems(view.child("output"), this.outputHandler.items);

        view.putInt("experience", experience);
        view.putInt("experienceType", experienceType);
        view.putInt("totalExperience", totalExperience);
        view.putInt("experienceLevel", experienceLevel);
        view.putInt("experienceProgress", experienceProgress);
        view.putInt("typeToAdd", typeToAdd);
        view.putInt("amountToAdd", amountToAdd);
        view.putInt("page", page);
        view.putBoolean("isActive", isActive);
        view.storeNullable("recipe", UncraftingTableRecipe.CODEC, currentRecipe);
        view.putInt("index", index);
        view.store("history", Codec.unboundedMap(Item.CODEC, RecipeSelectionHistory.CODEC), recipeSelectionHistory);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);

        ContainerHelper.loadAllItems(view.childOrEmpty("input"), this.inputHandler.items);
        ContainerHelper.loadAllItems(view.childOrEmpty("output"), this.outputHandler.items);

        experience = view.getIntOr("experience", 0);
        experienceType = view.getIntOr("experienceType", 0);
        totalExperience = view.getIntOr("totalExperience", 0);
        experienceLevel = view.getIntOr("experienceLevel", 0);
        experienceProgress = view.getIntOr("experienceProgress", 0);
        typeToAdd = view.getIntOr("typeToAdd", 0);
        amountToAdd = view.getIntOr("amountToAdd", 1);
        page = view.getIntOr("page", 0);
        isActive = view.getBooleanOr("isActive", false);
        currentRecipe = view.read("recipe", UncraftingTableRecipe.CODEC).orElse(null);
        index = view.getIntOr("index", 0);
        recipeSelectionHistory.putAll(view.read("history", Codec.unboundedMap(Item.CODEC, RecipeSelectionHistory.CODEC)).orElse(new HashMap<>()));
    }

    public ImplementedInventory getInputHandler() {
        return inputHandler;
    }

    public ImplementedInventory getOutputHandler() {
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
    public void setCurrentRecipe(List<UncraftingTableRecipe> currentRecipes) {
        RecipeSelectionHistory history = recipeSelectionHistory.get(currentStack.typeHolder());
        if (history != null && history.patch().equals(currentStack.getComponentsPatch())) {
            currentRecipe = history.recipe();
        } else {
            currentRecipe = currentRecipes.getFirst();
        }
    }

    @Override
    public void handleRecipeSelection(UncraftingTableRecipe recipe){
        this.currentRecipe = recipe;

        if(!hasRecipe()){
            if (inputHandler.getItem(0).isEmpty()){
                this.status = Status.BLANK;
            }
            else {
                if (UncraftEverythingConfig.isItemLocked(player, this.inputHandler.getItem(0)).getLeft()){
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
                recipeSelectionHistory.put(currentStack.typeHolder(), new RecipeSelectionHistory(recipe, page, index, currentRecipes.size(), currentStack.getComponentsPatch()));
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
        if (UncraftEverythingConfig.experienceType.equals(UncraftEverythingConfig.ExperienceType.POINT)){
            return totalExperience >= getExperience(this.inputHandler);
        }
        else if (UncraftEverythingConfig.experienceType.equals(UncraftEverythingConfig.ExperienceType.LEVEL)){
            return experienceLevel >= getExperience(this.inputHandler);
        }
        return true;
    }

    private int findSuitableOutputSlot(ItemStack result) {
        for (int i = 0; i < this.outputHandler.getContainerSize(); i++) {
            ItemStack stackInSlot = this.outputHandler.getItem(i);
            if (stackInSlot.isEmpty() || (ItemStack.isSameItemSameComponents(stackInSlot, result) && stackInSlot.getCount() + result.getCount() <= stackInSlot.getMaxStackSize())) {
                return i;
            }
        }
        return -1;
    }

    private void processUncraft(){
        List<ItemStack> outputs = currentRecipe.getOutputs();

        for (ItemStack o : outputs) {
            ItemStack output = o.copy();
            int slot = this.findSuitableOutputSlot(output);
            if (slot != -1) {
                ItemStack slotStack = outputHandler.getItem(slot);
                if (slotStack.isEmpty()) {
                    outputHandler.setItem(slot, output);
                } else if (ItemStack.isSameItemSameComponents(slotStack, output) && slotStack.getCount() + output.getCount() <= slotStack.getMaxStackSize()) {
                    slotStack.grow(output.getCount());
                }
            }
        }

        if (UncraftEverythingConfig.experienceType.equals(UncraftEverythingConfig.ExperienceType.POINT)){
            removeExperiencePoints(-getExperience(this.inputHandler));
        }
        else if (UncraftEverythingConfig.experienceType.equals(UncraftEverythingConfig.ExperienceType.LEVEL)){
            removeExperienceLevels(-getExperience(this.inputHandler));
        }

        inputHandler.removeItem(0, this.currentRecipe.getInput().getCount());
        setChanged();
        getOutputStacks(this.inputHandler, true);

        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public boolean hasRecipe() {
        if ((currentRecipes.isEmpty() && !byPass) || currentRecipe == null) {
            return false;
        }

        ItemStack inputStack = inputHandler.getItem(0);
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

        for (int i = 0; i < this.outputHandler.getContainerSize(); i++) {
            ItemStack stackInSlot = this.outputHandler.getItem(i);
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
        for (int i = 0; i < this.outputHandler.getContainerSize(); i++) {
            ItemStack stackInSlot = this.outputHandler.getItem(i);
            if (stackInSlot.isEmpty() || (ItemStack.isSameItemSameComponents(stackInSlot, result) && stackInSlot.getCount() + result.getCount() <= stackInSlot.getMaxStackSize())) {
                return false;
            }
        }
        return true;
    }

    private boolean cannotInsertItemIntoOutputSlot(ItemStack item) {
        for (int i = 0; i < this.outputHandler.getContainerSize(); i++) {
            ItemStack stackInSlot = this.outputHandler.getItem(i);
            if (stackInSlot.isEmpty() || ItemStack.isSameItemSameComponents(stackInSlot, item)) {
                return false;
            }
        }
        return true;
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
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN){
            return new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        }
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        if (dir == Direction.DOWN){
            return outputHandler.canPlaceItemThroughFace(slot, stack, dir);
        }
        return inputHandler.canPlaceItemThroughFace(slot, stack, dir);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        if (dir == Direction.DOWN){
            return outputHandler.canTakeItemThroughFace(slot, stack, dir);
        }
        return inputHandler.canTakeItemThroughFace(slot, stack, dir);
    }

    @Override
    public int getContainerSize() {
        return 10;
    }

    @Override
    public boolean isEmpty() {
        return inputHandler.isEmpty() && outputHandler.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot > 0){
            return outputHandler.getItem(slot - 1);
        }
        return inputHandler.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot > 0){
            return outputHandler.removeItem(slot - 1, amount);
        }
        return inputHandler.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot > 0){
            return outputHandler.removeItemNoUpdate(slot - 1);
        }
        return inputHandler.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot > 0){
            outputHandler.setItem(slot - 1, stack);
        }
        else{
            inputHandler.setItem(slot, stack);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        inputHandler.clearContent();
        outputHandler.clearContent();
    }
}