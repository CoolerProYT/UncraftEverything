package com.coolerpromc.uncrafteverything.platform.services;

import com.coolerpromc.uncrafteverything.platform.util.ingredient.ComponentIngredient;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface IIngredientHelper {
    Ingredient componentIngredient(ComponentIngredient ingredient);
    List<Pair<Item, DataComponentPatch>> getItemsFromIngredient(Ingredient ingredient, ItemStack inputStack);
    boolean isVanillaIngredientRecipe(Recipe<?> recipe);

    default boolean filterIngredient(Pair<Item, DataComponentPatch> item, ItemStack inputStack){
        if (item.getLeft().getDescriptionId().contains("shulker_box") && inputStack.getItem().builtInRegistryHolder().key().identifier().getPath().contains("_shulker_box")){
            return item.getLeft() == Items.SHULKER_BOX;
        }
        if (item.getLeft().getDescriptionId().contains("bundle") && inputStack.getItem().builtInRegistryHolder().key().identifier().getPath().contains("_bundle")){
            return item.getLeft() == Items.BUNDLE;
        }
        if (item.getLeft().getDescriptionId().contains("wool") && inputStack.getItem().builtInRegistryHolder().key().identifier().getPath().contains("_wool")){
            return item.getLeft() == Items.WHITE_WOOL;
        }
        if (item.getLeft().getDescriptionId().contains("carpet") && inputStack.getItem().builtInRegistryHolder().key().identifier().getPath().contains("_carpet")){
            return item.getLeft() == Items.WHITE_CARPET;
        }
        if (item.getLeft().getDescriptionId().contains("harness") && inputStack.getItem().builtInRegistryHolder().key().identifier().getPath().contains("_harness")){
            return inputStack.getItem() == Items.WHITE_HARNESS ? item.getLeft() == Items.GRAY_HARNESS : item.getLeft() == Items.WHITE_HARNESS;
        }
        return item.getLeft().getCraftingRemainder() == null || !item.getLeft().getCraftingRemainder().is(item.getLeft().getDefaultInstance().getItem());
    }

}
