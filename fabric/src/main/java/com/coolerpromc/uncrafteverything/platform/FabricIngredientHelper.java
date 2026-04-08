package com.coolerpromc.uncrafteverything.platform;

import com.coolerpromc.uncrafteverything.platform.services.IIngredientHelper;
import com.coolerpromc.uncrafteverything.platform.util.ingredient.ComponentIngredient;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.ComponentsIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class FabricIngredientHelper implements IIngredientHelper {
    @Override
    public Ingredient componentIngredient(ComponentIngredient ingredient) {
        return new ComponentsIngredient(ingredient.ingredient(), ingredient.patch()).toVanilla();
    }

    @Override
    public List<Pair<Item, DataComponentPatch>> getItemsFromIngredient(Ingredient ingredient, ItemStack inputStack) {
        List<Pair<Item, DataComponentPatch>> items = new ArrayList<>();

        if (ingredient.getCustomIngredient() != null && !ingredient.getCustomIngredient().items().toList().isEmpty()) {
            if (ingredient.getCustomIngredient() instanceof ComponentsIngredient dataComponentIngredient){
                for (var holder : dataComponentIngredient.items().toList()) {
                    items.add(Pair.of(holder.value(), dataComponentIngredient.display().resolveForFirstStack(new ContextMap.Builder().create(new ContextKeySet.Builder().build())).getComponentsPatch()));
                }
            }
            else{
                for (var holder : ingredient.getCustomIngredient().items().toList()) {
                    items.add(Pair.of(holder.value(), DataComponentPatch.EMPTY));
                }
            }
        }
        else {
            try {
                items = ingredient.items()
                        .map(holder -> Pair.of(holder.value(), DataComponentPatch.EMPTY))
                        .distinct()
                        .toList();
            } catch (IllegalStateException e) {
                LogUtils.getLogger().warn("Skipping unsupported ingredient type: {}", ingredient);
                return Collections.emptyList();
            }
        }

        return items.stream()
                .filter(item -> filterIngredient(item, inputStack))
                .sorted(Comparator.comparing(tuple -> tuple.getLeft().getDescriptionId()))
                .toList();
    }

    @Override
    public boolean isVanillaIngredientRecipe(Recipe<?> recipe) {
        List<Optional<Ingredient>> ingredients;

        switch (recipe) {
            case ShapedRecipe shaped -> ingredients = shaped.getIngredients();
            case ShapelessRecipe shapeless -> ingredients = shapeless.ingredients.stream().map(Optional::of).toList();
            case SmithingTransformRecipe smithingTransformRecipe -> ingredients = List.of(
                    Optional.of(smithingTransformRecipe.baseIngredient()),
                    smithingTransformRecipe.additionIngredient(),
                    smithingTransformRecipe.templateIngredient()
            );
            case null, default -> {
                return true;
            }
        }

        for (Optional<Ingredient> ingredient : ingredients) {
            if (ingredient.isPresent()){
                if (ingredient.get().getCustomIngredient() != null && !ingredient.get().getCustomIngredient().items().toList().isEmpty()) {
                    if (!ingredient.get().getCustomIngredient().items().map(Holder::value).map(BuiltInRegistries.ITEM::getKey).map(Identifier::getNamespace).toList().contains("minecraft")) {
                        return false;
                    }
                }
                else{
                    if (!ingredient.get().items().map(Holder::value).map(BuiltInRegistries.ITEM::getKey).map(Identifier::getNamespace).toList().contains("minecraft")) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
