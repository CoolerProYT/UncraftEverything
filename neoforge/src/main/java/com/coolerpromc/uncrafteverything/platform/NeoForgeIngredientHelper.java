package com.coolerpromc.uncrafteverything.platform;

import com.coolerpromc.uncrafteverything.platform.services.IIngredientHelper;
import com.coolerpromc.uncrafteverything.platform.util.ingredient.ComponentIngredient;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class NeoForgeIngredientHelper implements IIngredientHelper {
    @Override
    public Ingredient componentIngredient(ComponentIngredient ingredient) {
        return DataComponentIngredient.of(false, ingredient.patch(), ingredient.ingredient().getValues());
    }

    @Override
    public List<Pair<Item, DataComponentPatch>> getItemsFromIngredient(Ingredient ingredient, ItemStack inputStack) {
        List<Pair<Item, DataComponentPatch>> items = new ArrayList<>();

        if (ingredient.getCustomIngredient() != null && !ingredient.getCustomIngredient().items().toList().isEmpty()) {
            if (ingredient.getCustomIngredient() instanceof DataComponentIngredient dataComponentIngredient){
                for (var holder : dataComponentIngredient.itemSet()) {
                    items.add(Pair.of(holder.value(), dataComponentIngredient.components()));
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
                items = ingredient.getValues().stream()
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
                    if (!ingredient.get().getValues().stream().map(Holder::value).map(BuiltInRegistries.ITEM::getKey).map(Identifier::getNamespace).toList().contains("minecraft")) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
