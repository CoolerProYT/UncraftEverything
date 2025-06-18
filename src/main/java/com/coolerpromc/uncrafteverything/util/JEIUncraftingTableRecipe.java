package com.coolerpromc.uncrafteverything.util;

import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class JEIUncraftingTableRecipe {
    private final ItemStack input;
    private final List<Ingredient> outputs = new ArrayList<>();
    private final List<ItemStack> itemStackOutputs = new ArrayList<>();
    private final boolean isItemStackOutputs;

    public JEIUncraftingTableRecipe(ItemStack input, List<Ingredient> outputs) {
        this.input = input;
        this.outputs.addAll(outputs);
        this.isItemStackOutputs = false;
    }

    public JEIUncraftingTableRecipe(ItemStack input, List<ItemStack> outputs, boolean isItemStackOutputs) {
        this.input = input;
        this.itemStackOutputs.addAll(outputs);
        this.isItemStackOutputs = isItemStackOutputs;
    }

    public ItemStack getInput() {
        return input;
    }

    public List<Ingredient> getOutputs() {
        return outputs;
    }

    public List<EntryIngredient> getEntryIngredientOutput(){
        List<EntryIngredient> entryIngredients = new ArrayList<>();
        if (isItemStackOutputs){
            for (ItemStack output : itemStackOutputs) {
                if (output.isEmpty()) {
                    entryIngredients.add(EntryIngredient.empty());
                } else {
                    entryIngredients.add(EntryIngredients.of(output));
                }
            }
        }
        else {
            for (Ingredient output : outputs) {
                if (output == null){
                    entryIngredients.add(EntryIngredient.empty());
                }
                else{
                    entryIngredients.add(EntryIngredients.ofIngredient(output));
                }
            }
        }
        return entryIngredients;
    }
}