package com.coolerpromc.uncrafteverything.compat.rei;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UncraftingRecipeDisplay implements RecipeDisplay {
    public final List<EntryStack> inputs;
    public final List<List<EntryStack>> outputs;

    public UncraftingRecipeDisplay(List<EntryStack> inputs, List<List<EntryStack>> outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
    }

    @Override
    public @NotNull List<List<EntryStack>> getInputEntries() {
        return outputs;
    }

    @Override
    public @NotNull List<EntryStack> getOutputEntries() {
        return inputs;
    }

    @Override
    public @NotNull Identifier getRecipeCategory() {
        return UncraftingRecipeCategory.ID;
    }
}
