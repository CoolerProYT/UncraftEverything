package com.coolerpromc.uncrafteverything.util;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

import java.util.ArrayList;
import java.util.List;

/** Builds trim previews without repeatedly expanding broad material/template tags. */
final class TrimRecipeViewerRecipes {
    private TrimRecipeViewerRecipes() {}

    static List<JEIUncraftingTableRecipe> getRecipes(RegistryAccess access, SmithingTrimRecipe recipe) {
        var materials = access.registryOrThrow(Registries.TRIM_MATERIAL).holders()
                .filter(holder -> recipe.addition.test(new ItemStack(holder.value().ingredient())))
                .toList();
        var patterns = access.registryOrThrow(Registries.TRIM_PATTERN).holders()
                .filter(holder -> recipe.template.test(new ItemStack(holder.value().templateItem())))
                .toList();
        List<JEIUncraftingTableRecipe> entries = new ArrayList<>();
        if (materials.isEmpty() || patterns.isEmpty()) return entries;

        for (ItemStack base : recipe.base.getItems()) {
            if (base.isEmpty()) continue;
            for (var material : materials) {
                for (var pattern : patterns) {
                    // Ingredient stacks are shared with the recipe manager. Never mutate them.
                    ItemStack input = base.copy();
                    if (ArmorTrim.setTrim(access, input, new ArmorTrim(material, pattern))) {
                        entries.add(new JEIUncraftingTableRecipe(input, List.of(
                                Ingredient.of(base.getItem()),
                                Ingredient.of(material.value().ingredient().value()),
                                Ingredient.of(pattern.value().templateItem().value()))));
                    }
                }
            }
        }
        return entries;
    }
}
