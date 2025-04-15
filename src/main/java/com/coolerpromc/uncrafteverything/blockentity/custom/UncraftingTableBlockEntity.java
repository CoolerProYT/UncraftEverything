package com.coolerpromc.uncrafteverything.blockentity.custom;

import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableMenu;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class UncraftingTableBlockEntity extends BlockEntity implements MenuProvider {
    private List<UncraftingTableRecipe> currentRecipes = new ArrayList<>();
    private UncraftingTableRecipe currentRecipe = null;
    private boolean isCrafting = false;

    private final ItemStackHandler inputHandler = new ItemStackHandler(1){
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            getOutputStacks();
        }
    };

    private final ItemStackHandler outputHandler = new ItemStackHandler(9){
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    };

    public UncraftingTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(UEBlockEntities.UNCRAFTING_TABLE_BE.get(), pos, blockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.uncrafting_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new UncraftingTableMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("isCrafting", isCrafting);
        tag.put("input", inputHandler.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        isCrafting = tag.getBoolean("isCrafting");
        if (tag.contains("input")) {
            inputHandler.deserializeNBT(registries, tag.getCompound("input"));
        }
    }

    public ItemStackHandler getInputHandler() {
        return inputHandler;
    }

    public ItemStackHandler getOutputHandler() {
        return outputHandler;
    }

    public void getOutputStacks() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        ItemStack inputStack = this.inputHandler.getStackInSlot(0);

        List<Item> materialItems = List.of(
                Items.IRON_NUGGET,
                Items.IRON_INGOT,
                Items.IRON_BLOCK,

                Items.GOLD_NUGGET,
                Items.GOLD_INGOT,
                Items.GOLD_BLOCK
        );

        if (materialItems.contains(inputStack.getItem())) return;

        List<RecipeHolder<?>> recipes = serverLevel.recipeAccess().getRecipes().stream().filter(recipeHolder -> {
            if (recipeHolder.value() instanceof ShapedRecipe shapedRecipe){
                return shapedRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() >= shapedRecipe.result.getCount();
            }

            if (recipeHolder.value() instanceof ShapelessRecipe shapelessRecipe){
                return shapelessRecipe.result.getItem() == inputStack.getItem() && inputStack.getCount() >= shapelessRecipe.result.getCount();
            }

            return false;
        }).toList();

        List<UncraftingTableRecipe> outputs = new ArrayList<>();

        for (RecipeHolder<?> r : recipes){
            if (r.value() instanceof ShapedRecipe shapedRecipe){
                UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(shapedRecipe.result.getItem(), shapedRecipe.result.getCount()));
                for(Optional<Ingredient> i : shapedRecipe.getIngredients()){
                    if (i.isPresent()){
                        Item item = i.get().getValues().get(0).value();
                        if (outputStack.getOutputs().contains(item.getDefaultInstance())){
                            ItemStack stack = outputStack.getOutputs().get(outputStack.getOutputs().indexOf(item.getDefaultInstance()));
                            outputStack.setOutput(outputStack.getOutputs().indexOf(item.getDefaultInstance()), new ItemStack(stack.getItem(), stack.getCount() + 1));
                        }
                        else{
                            outputStack.addOutput(new ItemStack(item, 1));
                        }
                    }
                    else{
                        Item item = Items.AIR;
                        if (outputStack.getOutputs().contains(item.getDefaultInstance())){
                            ItemStack stack = outputStack.getOutputs().get(outputStack.getOutputs().indexOf(item.getDefaultInstance()));
                            outputStack.setOutput(outputStack.getOutputs().indexOf(item.getDefaultInstance()), new ItemStack(stack.getItem(), stack.getCount() + 1));
                        }
                        else{
                            outputStack.addOutput(new ItemStack(item, 1));
                        }
                    }
                }
                outputs.add(outputStack);
            }

            if (r.value() instanceof ShapelessRecipe shapelessRecipe){
                UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(shapelessRecipe.result.getItem(), shapelessRecipe.result.getCount()));
                for(Ingredient i : shapelessRecipe.ingredients){
                    Item item = i.getValues().get(0).value();
                    if (outputStack.getOutputs().contains(item.getDefaultInstance())){
                        ItemStack stack = outputStack.getOutputs().get(outputStack.getOutputs().indexOf(item.getDefaultInstance()));
                        outputStack.setOutput(outputStack.getOutputs().indexOf(item.getDefaultInstance()), new ItemStack(stack.getItem(), stack.getCount() + 1));
                    }
                    else{
                        outputStack.addOutput(new ItemStack(item, 1));
                    }
                }
                outputs.add(outputStack);
            }
        }

        this.currentRecipes = outputs;

        if (!currentRecipes.isEmpty()) {
            this.currentRecipe = outputs.getFirst();
        }
    }

    public void handleButtonClick(String data){
        if (hasRecipe()){
            int i = 0;
            for (ItemStack stack : currentRecipe.getOutputs()){
                if (i < outputHandler.getSlots()){
                    i++;
                    if (!canInsertItemIntoOutputSlot(stack.getItem())) continue;
                    outputHandler.setStackInSlot(i, new ItemStack(stack.getItem(), stack.getCount() + outputHandler.getStackInSlot(i).getCount()));
                }
            }
            inputHandler.extractItem(0, this.currentRecipe.getInput().getCount(), false);
            setChanged();
        }
    }

    private boolean hasRecipe() {
        if (currentRecipes.isEmpty()) {
            return false;
        }

        List<ItemStack> results = currentRecipe.getOutputs();

        for (ItemStack stack : results) {
            if (!canInsertAmountIntoOutputSlot(stack) || !canInsertItemIntoOutputSlot(stack.getItem())) {
                return false;
            }
        }

        return checkSlot(results);
    }

    private boolean checkSlot(List<ItemStack> results) {
        int count = 0;
        int emptyCount = 0;
        for (ItemStack result : results) {
            count++;
        }
        for (int i = 0; i < this.outputHandler.getSlots(); i++) {
            ItemStack stackInSlot = this.outputHandler.getStackInSlot(i);
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

    private boolean canInsertAmountIntoOutputSlot(ItemStack result) {
        for (int i = 0; i < this.outputHandler.getSlots(); i++) {
            ItemStack stackInSlot = this.outputHandler.getStackInSlot(i);
            if (stackInSlot.isEmpty() || (stackInSlot.getItem() == result.getItem() && stackInSlot.getCount() + result.getCount() <= stackInSlot.getMaxStackSize())) {
                return true;
            }
        }
        return false;
    }

    private boolean canInsertItemIntoOutputSlot(Item item) {
        for (int i = 0; i < this.outputHandler.getSlots(); i++) {
            ItemStack stackInSlot = this.outputHandler.getStackInSlot(i);
            if (stackInSlot.isEmpty() || stackInSlot.getItem() == item) {
                return true;
            }
        }
        return false;
    }

    public List<UncraftingTableRecipe> getCurrentRecipes() {
        return currentRecipes;
    }

    public UncraftingTableRecipe getCurrentRecipe() {
        return currentRecipe;
    }
}
