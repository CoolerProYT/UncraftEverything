package com.coolerpromc.uncrafteverything.util;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Builds trim previews without repeatedly expanding broad material/template tags. */
final class TrimRecipeViewerRecipes {
    static final int MAX_PER_RECIPE = 256;
    static final int MAX_TOTAL = 4096;

    private TrimRecipeViewerRecipes() {}

    static List<JEIUncraftingTableRecipe> getRecipes(RegistryAccess access, SmithingTrimRecipe recipe, int remaining) {
        int limit = Math.min(remaining, MAX_PER_RECIPE);
        if (limit <= 0) return List.of();

        var patterns = access.registryOrThrow(Registries.TRIM_PATTERN).holders()
                .filter(holder -> recipe.template.test(new ItemStack(holder.value().templateItem())))
                .toList();
        if (patterns.isEmpty()) return List.of();

        var bases = Arrays.stream(recipe.base.getItems())
                .filter(stack -> !stack.isEmpty() && stack.is(ItemTags.TRIMMABLE_ARMOR))
                .toList();
        if (bases.isEmpty()) return List.of();

        List<JEIUncraftingTableRecipe> entries = new ArrayList<>();
        // Match materials lazily so broad tags stop being searched once the preview budget is full.
        var materials = access.registryOrThrow(Registries.TRIM_MATERIAL).holders().iterator();
        while (materials.hasNext()) {
            var material = materials.next();
            ItemStack materialStack = new ItemStack(material.value().ingredient());
            if (!recipe.addition.test(materialStack)) continue;
            Ingredient materialOutput = Ingredient.of(materialStack.getItem());
            for (var pattern : patterns) {
                Ingredient templateOutput = Ingredient.of(pattern.value().templateItem().value());
                var trim = new ArmorTrim(material, pattern);
                for (ItemStack base : bases) {
                    // Ingredient stacks are shared with the recipe manager. Never mutate them.
                    ItemStack input = base.copy();
                    if (ArmorTrim.setTrim(access, input, trim)) {
                        entries.add(new JEIUncraftingTableRecipe(input, List.of(
                                Ingredient.of(base.getItem()), materialOutput, templateOutput)));
                        if (entries.size() == limit) return entries;
                    }
                }
            }
        }
        return entries;
    }
}
