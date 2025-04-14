package com.coolerpromc.uncrafteverything.blockentity.custom;

import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableMenu;
import net.minecraft.core.BlockPos;
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
    private ItemStack currentInput = ItemStack.EMPTY;
    private boolean isCrafting = false;

    private final ItemStackHandler input = new ItemStackHandler(1){
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (this.getStackInSlot(slot).isEmpty() && !isCrafting){
                for (int i = 0; i < output.getSlots(); i++){
                    output.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
            else{
                getOutputStacks();
            }
        }
    };

    private final ItemStackHandler output = new ItemStackHandler(9){
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();

            System.out.println("isCrafting: " + isCrafting);
            if (this.getStackInSlot(slot).isEmpty()){
                if (!isCrafting){
                    input.extractItem(0, currentInput.getCount(), false);
                    isCrafting = true;
                }
                else{
                    isCrafting = false;
                    for (int i = 0; i < output.getSlots(); i++){
                        if (!output.getStackInSlot(i).isEmpty()){
                            isCrafting = true;
                            break;
                        }
                    }
                }
            }
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

    public ItemStackHandler getInput() {
        return input;
    }

    public ItemStackHandler getOutput() {
        return output;
    }

    public void getOutputStacks() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        ItemStack inputStack = this.input.getStackInSlot(0);

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

        Map<Item, Integer> outputs = new HashMap<>();

        for (RecipeHolder<?> r : recipes){
            if (r.value() instanceof ShapedRecipe shapedRecipe){
                currentInput = shapedRecipe.result;
                for(Optional<Ingredient> i : shapedRecipe.getIngredients()){
                    if (i.isPresent()){
                        Item item = i.get().getValues().get(0).value();
                        if (outputs.containsKey(item)){
                            outputs.replace(item, outputs.get(item) + 1);
                        }
                        else{
                            outputs.put(item, 1);
                        }
                    }
                }
            }

            if (r.value() instanceof ShapelessRecipe shapelessRecipe){
                currentInput = shapelessRecipe.result;
                for(Ingredient i : shapelessRecipe.ingredients){
                    Item item = i.getValues().get(0).value();
                    if (outputs.containsKey(item)){
                        outputs.replace(item, outputs.get(item) + 1);
                    }
                    else{
                        outputs.put(item, 1);
                    }
                }
            }
        }

        //ensure all output slot are empty
        boolean empty = true;

        for (int j = 0; j < output.getSlots(); j++){
            if (!output.getStackInSlot(j).isEmpty()){
                empty = false;
                break;
            }
        }

        if (empty){
            int i = 0;
            for (Map.Entry<Item, Integer> entry : outputs.entrySet()){
                Item item = entry.getKey();
                int count = entry.getValue();

                if (i < output.getSlots()){
                    output.setStackInSlot(i, new ItemStack(item, count));
                    i++;
                }
            }
            setChanged();
        }
    }
}
