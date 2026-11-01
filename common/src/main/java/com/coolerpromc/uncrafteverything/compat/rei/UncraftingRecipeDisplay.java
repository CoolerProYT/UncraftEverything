package com.coolerpromc.uncrafteverything.compat.rei;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;

import java.util.List;

public class UncraftingRecipeDisplay extends BasicDisplay {
    public static CategoryIdentifier<UncraftingRecipeDisplay> CATEGORY_IDENTIFIER = CategoryIdentifier.of(UncraftEverything.MODID, "uncrafting_table");

    public UncraftingRecipeDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
        super(inputs, outputs);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return CATEGORY_IDENTIFIER;
    }
}
