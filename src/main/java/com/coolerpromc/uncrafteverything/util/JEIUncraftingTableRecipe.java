package com.coolerpromc.uncrafteverything.util;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class JEIUncraftingTableRecipe {
    private final ItemStack input;
    private final List<Ingredient> outputs = new ArrayList<>();

    public JEIUncraftingTableRecipe(ItemStack input, List<Ingredient> outputs) {
        this.input = input;
        this.outputs.addAll(outputs);
    }

    public ItemStack getInput() {
        return input;
    }

    public List<Ingredient> getOutputs() {
        return outputs;
    }
}
