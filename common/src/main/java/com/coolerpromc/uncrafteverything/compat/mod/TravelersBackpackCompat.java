package com.coolerpromc.uncrafteverything.compat.mod;

import com.coolerpromc.uncrafteverything.blockentity.custom.AbstractUncraftingTableBE;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.platform.Services;
import com.coolerpromc.uncrafteverything.util.DebugLogger;
import com.coolerpromc.uncrafteverything.util.Status;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.tiviacz.travelersbackpack.common.recipes.BackpackUpgradeRecipe;
import com.tiviacz.travelersbackpack.common.recipes.ShapedBackpackRecipe;
import com.tiviacz.travelersbackpack.item.TravelersBackpackItem;
import com.tiviacz.travelersbackpack.init.ModDataComponents;
import com.tiviacz.travelersbackpack.init.ModTags;
import com.tiviacz.travelersbackpack.inventory.Tiers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static com.coolerpromc.uncrafteverything.util.UncraftingTableHelpers.getIngredientCombinations;

public class TravelersBackpackCompat {

    public static <T extends AbstractUncraftingTableBE> boolean hasRecipe(RecipeHolder<?> recipeHolder, ItemStack inputStack, T blockEntity){
        if (recipeHolder.value() instanceof ShapedBackpackRecipe shapedRecipe){
            if (UncraftEverythingConfig.CONFIG.restrictAmbiguouslyCraftedItems()){
                for (Optional<Ingredient> ing : shapedRecipe.getIngredients()){
                    if (ing.isPresent() && Services.INGREDIENT.getItemsFromIngredient(ing.get(), inputStack).size() > 1){
                        blockEntity.status = Status.RESTRICTED_BY_CONFIG;
                        return false;
                    }
                }
            }
            return validateBackpackRecipe(shapedRecipe, inputStack, blockEntity);
        }

        if (recipeHolder.value() instanceof BackpackUpgradeRecipe backpackUpgradeRecipe){
            if (!UncraftEverythingConfig.CONFIG.allowUnSmithing() || (inputStack.get(DataComponents.ENCHANTMENTS) != ItemEnchantments.EMPTY && UncraftEverythingConfig.CONFIG.outputEnchantedBook())){
                return false;
            }
            return validateBackpackUpgradeRecipe(backpackUpgradeRecipe, inputStack);
        }

        return false;
    }

    public static <T extends AbstractUncraftingTableBE> boolean validateBackpackRecipe(ShapedBackpackRecipe recipe, ItemStack inputStack, T blockEntity){
        ItemStack resultTemplate = recipe.result.create();
        if (resultTemplate.getItem() == inputStack.getItem() && inputStack.getCount() < resultTemplate.getCount()){
            blockEntity.status = Status.NOT_ENOUGH_INPUT_ITEM;
        }
        if (!ItemStack.isSameItem(resultTemplate, inputStack) || inputStack.getCount() < resultTemplate.getCount()) return false;

        for (Optional<Ingredient> optIng : recipe.getIngredients()) {
            if (optIng.isEmpty()) continue;
            for (Holder<Item> holder : optIng.get().items().toList()) {
                if (holder.value() instanceof TravelersBackpackItem) {
                    Integer tier = inputStack.get(ModDataComponents.TIER);
                    return tier == null || tier == 0;
                }
            }
        }

        int expectedColor = -1;
        boolean recipeHasTanks = false;
        for (Optional<Ingredient> optIng : recipe.getIngredients()) {
            if (optIng.isEmpty()) continue;
            for (Holder<Item> holder : optIng.get().items().toList()) {
                if (holder.is(ModTags.SLEEPING_BAGS)) {
                    expectedColor = ShapedBackpackRecipe.getProperColor(holder.value());
                }
                if (holder.value() == Services.TRAVELERS_BACKPACK.getTankItem()) {
                    recipeHasTanks = true;
                }
            }
        }

        Integer inputColor = inputStack.get(ModDataComponents.SLEEPING_BAG_COLOR);
        if (inputColor == null) inputColor = -1;
        if (expectedColor != inputColor) return false;

        boolean inputHasTanks = inputStack.get(ModDataComponents.STARTER_UPGRADES) != null;
        return recipeHasTanks == inputHasTanks;
    }

    public static boolean validateBackpackUpgradeRecipe(BackpackUpgradeRecipe backpackUpgradeRecipe, ItemStack inputStack){
        if (!backpackUpgradeRecipe.baseIngredient().test(inputStack)) return false;
        Integer tier = inputStack.get(ModDataComponents.TIER);
        return tier != null && tier > 0;
    }

    public static List<UncraftingTableRecipe> getOutput(RecipeHolder<?> r, ItemStack inputStack){
        List<UncraftingTableRecipe> recipes = new ArrayList<>();

        if (r.value() instanceof ShapedBackpackRecipe shapedRecipe) {
            boolean cont = false;
            for (Optional<Ingredient> ingredient : shapedRecipe.getIngredients()){
                if (ingredient.isPresent()){
                    Ingredient ing = ingredient.get();
                    for (Holder<Item> item : ing.items().toList()){
                        if (BuiltInRegistries.ITEM.getKey(inputStack.getItem()).getNamespace().equals("minecraft") && UncraftEverythingConfig.CONFIG.preventModdedIngredientsFromVanillaItems() && !item.unwrapKey().get().identifier().getNamespace().equals("minecraft")){
                            DebugLogger.log("[Prevent Modded Ingredient Enabled]");
                            DebugLogger.log("Skipping Recipe: " + r.id().identifier());
                            DebugLogger.log("Skipped item: " + inputStack.typeHolder());
                            cont = true;
                            break;
                        }
                    }
                }
            }
            if (cont) return null;
            // Get all possible combinations of ingredients
            List<List<Pair<Item, DataComponentPatch>>> allIngredientCombinations = getIngredientCombinations(shapedRecipe.getIngredients(), inputStack);

            // Create a recipe for each combination
            for (List<Pair<Item, DataComponentPatch>> ingredientCombination : allIngredientCombinations) {
                UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(shapedRecipe.result.item().value().builtInRegistryHolder(), shapedRecipe.result.count(), inputStack.getComponentsPatch()));
                Map<Pair<Item, DataComponentPatch>, Integer> allIngredients = new HashMap<>();

                for (Pair<Item, DataComponentPatch> item : ingredientCombination) {
                    if (outputStack.contains(item)) {
                        ItemStack stack = outputStack.getStack(item);
                        stack.grow(1);
                        outputStack.setOutput(outputStack.indexOf(item), stack);
                    } else {
                        outputStack.addOutput(new ItemStack(item.getLeft().builtInRegistryHolder(), 1, item.getRight()));
                    }
                    allIngredients.put(item, allIngredients.getOrDefault(item, 0) + 1);
                }

                recipes.add(outputStack);
            }
        }

        if (r.value() instanceof BackpackUpgradeRecipe backpackUpgradeRecipe){
            int inputTier = inputStack.getOrDefault(ModDataComponents.TIER, 0);
            if (inputTier <= 0) return recipes;

            Tiers.Tier prevTier = Tiers.of(inputTier - 1);

            // Build lower-tier backpack: same item and data components, but with prev-tier slot counts
            ItemStack backpackOutput = inputStack.copyWithCount(1);
            backpackOutput.set(ModDataComponents.TIER, prevTier.getOrdinal());
            backpackOutput.set(ModDataComponents.STORAGE_SLOTS, prevTier.getStorageSlots());
            backpackOutput.set(ModDataComponents.UPGRADE_SLOTS, prevTier.getUpgradeSlots());
            backpackOutput.set(ModDataComponents.TOOL_SLOTS, prevTier.getToolSlots());

            UncraftingTableRecipe outputStack = new UncraftingTableRecipe(inputStack.copyWithCount(1));

            outputStack.addOutput(backpackOutput);

            // The specific upgrade material consumed when smithing from prevTier → inputTier
            Item tierUpgradeItem = prevTier.getTierUpgradeIngredient();
            if (!tierUpgradeItem.equals(net.minecraft.world.item.Items.AIR)) {
                outputStack.addOutput(new ItemStack(tierUpgradeItem));
            }

            // Template ingredient (e.g. leather from #c:leathers)
            backpackUpgradeRecipe.templateIngredient().ifPresent(templateIng -> {
                List<Holder<Item>> templateItems = templateIng.items().toList();
                if (!templateItems.isEmpty()) {
                    outputStack.addOutput(new ItemStack(templateItems.get(0).value()));
                }
            });

            recipes.add(outputStack);
        }
        return recipes;
    }
}
