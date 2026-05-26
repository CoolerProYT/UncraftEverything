package com.coolerpromc.uncrafteverything.util;

import com.coolerpromc.uncrafteverything.blockentity.custom.AbstractUncraftingTableBE;
import com.coolerpromc.uncrafteverything.compat.mod.RandomMisfitsCompat;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.platform.Services;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class UncraftingTableHelpers {
    public static <T extends AbstractUncraftingTableBE> boolean validateInput(ItemStack inputStack, @Nullable ServerPlayer player, T blockEntity){
        if (inputStack.isEmpty()
                || UncraftEverythingConfig.isItemLocked(player, inputStack).getLeft()
                || (inputStack.getDamageValue() > 0 && !UncraftEverythingConfig.allowDamaged())
                || UncraftEverythingConfig.isItemBlacklisted(inputStack)
                || UncraftEverythingConfig.isItemWhitelisted(inputStack)
                || (!UncraftEverythingConfig.isEnchantedItemsAllowed(inputStack) && !inputStack.has(DataComponents.TRIM))
                || (inputStack.getItem() == Items.SHULKER_BOX && inputStack.get(DataComponents.CONTAINER) != ItemContainerContents.EMPTY)
                || (inputStack.getItem() == Items.ENCHANTED_BOOK)
        ) {
            if (inputStack.getDamageValue() > 0 && !UncraftEverythingConfig.allowDamaged()){
                blockEntity.status = Status.DAMAGED_ITEM;
            }

            if (UncraftEverythingConfig.isItemBlacklisted(inputStack)){
                blockEntity.status = Status.RESTRICTED_BY_CONFIG;
            }

            if (UncraftEverythingConfig.isItemWhitelisted(inputStack)){
                blockEntity.status = Status.RESTRICTED_BY_CONFIG;
            }

            if (!UncraftEverythingConfig.isEnchantedItemsAllowed(inputStack) && !inputStack.has(DataComponents.TRIM)){
                blockEntity.status = Status.ENCHANTED_ITEM;
            }

            if(inputStack.getItem() == Items.SHULKER_BOX && inputStack.get(DataComponents.CONTAINER) != ItemContainerContents.EMPTY){
                blockEntity.status = Status.NOT_EMPTY_SHULKER;
            }

            if (inputStack.getItem() == Items.ENCHANTED_BOOK) {
                blockEntity.status = Status.NO_RECIPE_FOUND;
            }

            if (inputStack.isEmpty()){
                blockEntity.status = Status.BLANK;
            }

            Pair<Boolean, Status> isItemLocked = UncraftEverythingConfig.isItemLocked(player, inputStack);
            if (isItemLocked.getLeft() && !inputStack.isEmpty()){
                blockEntity.status = isItemLocked.getRight();
            }

            return false;
        }
        return true;
    }

    public static <T extends AbstractUncraftingTableBE> List<RecipeHolder<?>> findRecipe(ServerLevel serverLevel, ItemStack input, T blockEntity){
        ItemStack inputStack = input.copy();
        inputStack.remove(DataComponents.CUSTOM_NAME);
        if (Services.PLATFORM.isModLoaded("whatdurability") && inputStack.has(DataComponents.DAMAGE)) inputStack.set(DataComponents.DAMAGE, 0);
        if (Services.PLATFORM.isModLoaded("randomisfits")) RandomMisfitsCompat.removeComponent(inputStack);
        return serverLevel.recipeAccess().getRecipes().stream().filter(recipeHolder -> {
            if (recipeHolder.value() instanceof ShapedRecipe shapedRecipe){
                if (UncraftEverythingConfig.restrictAmbiguouslyCraftedItems()){
                    for (Optional<Ingredient> ing : shapedRecipe.getIngredients()){
                        if (ing.isPresent() && Services.INGREDIENT.getItemsFromIngredient(ing.get(), inputStack).size() > 1){
                            blockEntity.status = Status.RESTRICTED_BY_CONFIG;
                            return false;
                        }
                    }
                }
                return validateRecipe(shapedRecipe.result.create(), inputStack, blockEntity);
            }

            if (recipeHolder.value() instanceof ShapelessRecipe shapelessRecipe){
                if (inputStack.getItem() instanceof BedItem) return false;
                if (UncraftEverythingConfig.restrictAmbiguouslyCraftedItems()){
                    for (Ingredient ing : shapelessRecipe.ingredients){
                        if (Services.INGREDIENT.getItemsFromIngredient(ing, inputStack).size() > 1){
                            blockEntity.status = Status.RESTRICTED_BY_CONFIG;
                            return false;
                        }
                    }
                }
                return validateRecipe(shapelessRecipe.result.create(), inputStack, blockEntity);
            }

            if(recipeHolder.value() instanceof TransmuteRecipe transmuteRecipe){
                ItemStack stack = inputStack.copy();
                if (stack.has(DataComponents.CONTAINER)) stack.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
                if (stack.has(DataComponents.BUNDLE_CONTENTS)) stack.set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
                return ItemStack.isSameItemSameComponents(stack, new ItemStack(transmuteRecipe.result.item(), transmuteRecipe.result.count(), transmuteRecipe.result.components()));
            }

            if (recipeHolder.value() instanceof SmithingTransformRecipe smithingTransformRecipe){
                if (!UncraftEverythingConfig.allowUnSmithing() || (inputStack.get(DataComponents.ENCHANTMENTS) != ItemEnchantments.EMPTY && UncraftEverythingConfig.outputEnchantedBook())){
                    return false;
                }
                return validateSmithingRecipe(smithingTransformRecipe, inputStack);
            }

            if (recipeHolder.value() instanceof SmithingTrimRecipe smithingTrimRecipe){
                if (!UncraftEverythingConfig.allowUnSmithing()){
                    return false;
                }
                ArmorTrim armorTrim = inputStack.get(DataComponents.TRIM);

                if (armorTrim != null){
                    Optional<Ingredient> ingredient = smithingTrimRecipe.additionIngredient();
                    if (ingredient.isPresent() && armorTrim.pattern().is(smithingTrimRecipe.pattern)){
                        return true;
                    }
                }
            }

            if (blockEntity.status == Status.BLANK && !inputStack.isEmpty()){
                blockEntity.status = Status.NO_RECIPE_FOUND;
            }
            return false;
        }).toList();
    }

    public static <T extends AbstractUncraftingTableBE> boolean validateRecipe(ItemStack result, ItemStack inputStack, T blockEntity){
        if (result.getItem() == inputStack.getItem() && inputStack.getCount() < result.getCount()){
            blockEntity.status = Status.NOT_ENOUGH_INPUT_ITEM;
        }
        if (inputStack.get(DataComponents.ENCHANTMENTS) != ItemEnchantments.EMPTY && UncraftEverythingConfig.outputEnchantedBook()){
            return false;
        }
        if (inputStack.isDamaged()){
            return result.getItem() == inputStack.getItem() && inputStack.getCount() >= result.getCount();
        }
        if (inputStack.get(DataComponents.ENCHANTMENTS) != ItemEnchantments.EMPTY && UncraftEverythingConfig.allowEnchantedItems && result.getItem() == inputStack.getItem()){
            return true;
        }
        return ItemStack.isSameItemSameComponents(result, inputStack) && inputStack.getCount() >= result.getCount();
    }

    public static boolean validateSmithingRecipe(SmithingTransformRecipe smithingTransformRecipe, ItemStack inputStack){
        if (inputStack.get(DataComponents.ENCHANTMENTS) != ItemEnchantments.EMPTY && UncraftEverythingConfig.outputEnchantedBook()){
            return false;
        }
        if (inputStack.isDamaged()){
            return inputStack.is(smithingTransformRecipe.result.item().value()) && inputStack.getCount() >= smithingTransformRecipe.result.count();
        }
        if (inputStack.get(DataComponents.ENCHANTMENTS) != ItemEnchantments.EMPTY && UncraftEverythingConfig.allowEnchantedItems && smithingTransformRecipe.result.item().value() == inputStack.getItem()){
            return true;
        }
        return ItemStack.isSameItemSameComponents(inputStack, new ItemStack(smithingTransformRecipe.result.item(), smithingTransformRecipe.result.count(), smithingTransformRecipe.result.components()));
    }


    public static <T extends AbstractUncraftingTableBE> Pair<List<UncraftingTableRecipe>, Boolean> getOutputs(ItemStack inputStack, List<RecipeHolder<?>> recipes, T blockEntity){
        List<UncraftingTableRecipe> outputs = new ArrayList<>();

        if (inputStack.is(Items.TIPPED_ARROW)){
            PotionContents potionContents = inputStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(inputStack.getItem().builtInRegistryHolder(), 8, inputStack.getComponentsPatch()));
            ItemStack potion = new ItemStack(Items.LINGERING_POTION);
            potion.set(DataComponents.POTION_CONTENTS, potionContents);

            outputStack.addOutput(new ItemStack(Items.ARROW, 8));
            outputStack.addOutput(potion);
            outputs.add(outputStack);
        }

        if (inputStack.get(DataComponents.ENCHANTMENTS) != ItemEnchantments.EMPTY && recipes.isEmpty() && UncraftEverythingConfig.outputEnchantedBook()){
            UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(inputStack.getItem().builtInRegistryHolder(), 1, inputStack.getComponentsPatch()));
            ItemEnchantments enchantments = inputStack.get(DataComponents.ENCHANTMENTS);
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            book.set(DataComponents.STORED_ENCHANTMENTS, enchantments);
            ItemStack output = new ItemStack(inputStack.getItem(), 1);
            output.setDamageValue(inputStack.getDamageValue());

            outputStack.addOutput(output);
            outputStack.addOutput(book);

            outputs.add(outputStack);
        }

        for (RecipeHolder<?> r : recipes) {
            if (r.value() instanceof TransmuteRecipe transmuteRecipe){
                List<Ingredient> ingredients = List.of(transmuteRecipe.input, transmuteRecipe.material);
                boolean cont = false;
                for (Ingredient ingredient : ingredients){
                    for (Holder<Item> item : ingredient.items().toList()){
                        if (BuiltInRegistries.ITEM.getKey(inputStack.getItem()).getNamespace().equals("minecraft") && UncraftEverythingConfig.preventModdedIngredientRecipes() && !item.unwrapKey().get().identifier().getNamespace().equals("minecraft")){
                            DebugLogger.log("[Prevent Modded Ingredient Enabled]");
                            DebugLogger.log("Skipping Recipe: " + r.id().identifier());
                            DebugLogger.log("Skipped item: " + inputStack.typeHolder());
                            cont = true;
                            break;
                        }
                    }
                }
                if (cont) continue;
                List<List<Pair<Item, DataComponentPatch>>> allIngredientCombinations = getShapelessIngredientCombinations(ingredients, inputStack);

                for (List<Pair<Item, DataComponentPatch>> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(transmuteRecipe.result.item().value().builtInRegistryHolder(), 1, inputStack.getComponentsPatch()));

                    for (Pair<Item, DataComponentPatch> item : ingredientCombination) {
                        if (outputStack.contains(item)) {
                            outputStack.getStack(item).grow(1);
                        } else {
                            ItemStack itemStack = new ItemStack(item.getLeft().builtInRegistryHolder(), 1, item.getRight());
                            if (itemStack.has(DataComponents.CONTAINER)) itemStack.set(DataComponents.CONTAINER, inputStack.get(DataComponents.CONTAINER));
                            if (itemStack.has(DataComponents.BUNDLE_CONTENTS)) itemStack.set(DataComponents.BUNDLE_CONTENTS, inputStack.get(DataComponents.BUNDLE_CONTENTS));
                            outputStack.addOutput(itemStack);
                        }
                    }
                    outputs.add(outputStack);
                }
            }

            if (r.value() instanceof ShapedRecipe shapedRecipe) {
                boolean cont = false;
                for (Optional<Ingredient> ingredient : shapedRecipe.getIngredients()){
                    if (ingredient.isPresent()){
                        Ingredient ing = ingredient.get();
                        for (Holder<Item> item : ing.items().toList()){
                            if (BuiltInRegistries.ITEM.getKey(inputStack.getItem()).getNamespace().equals("minecraft") && UncraftEverythingConfig.preventModdedIngredientRecipes() && !item.unwrapKey().get().identifier().getNamespace().equals("minecraft")){
                                DebugLogger.log("[Prevent Modded Ingredient Enabled]");
                                DebugLogger.log("Skipping Recipe: " + r.id().identifier());
                                DebugLogger.log("Skipped item: " + inputStack.typeHolder());
                                cont = true;
                                break;
                            }
                        }
                    }
                }
                if (cont) continue;
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
                    if (inputStack.isDamaged()){
                        Repairable repairableComponent = inputStack.get(DataComponents.REPAIRABLE);
                        if (repairableComponent != null){
                            for (var x : allIngredients.entrySet()){
                                if (repairableComponent.isValidRepairItem(new ItemStack(x.getKey().getLeft(), x.getValue()))){
                                    int damagedPercentage = (int) Math.ceil((double) inputStack.getDamageValue() / inputStack.getMaxDamage() * x.getValue());
                                    while (outputStack.getStack(x.getKey()).getCount() > 0 && damagedPercentage != 0){
                                        if (outputStack.getStack(x.getKey()).is(x.getKey().getLeft())){
                                            outputStack.getStack(x.getKey()).shrink(1);
                                            damagedPercentage--;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        else{
                            blockEntity.status = Status.DAMAGED_ITEM;
                            outputs.clear();
                            return Pair.of(outputs, false);
                        }
                    }
                    outputs.add(outputStack);
                }
            }

            if (r.value() instanceof ShapelessRecipe shapelessRecipe) {
                boolean cont = false;
                for (Ingredient ingredient : shapelessRecipe.ingredients){
                    for (Holder<Item> item : ingredient.items().toList()){
                        if (BuiltInRegistries.ITEM.getKey(inputStack.getItem()).getNamespace().equals("minecraft") && UncraftEverythingConfig.preventModdedIngredientRecipes() && !item.unwrapKey().get().identifier().getNamespace().equals("minecraft")){
                            DebugLogger.log("[Prevent Modded Ingredient Enabled]");
                            DebugLogger.log("Skipping Recipe: " + r.id().identifier());
                            DebugLogger.log("Skipped item: " + inputStack.typeHolder());
                            cont = true;
                            break;
                        }
                    }
                }
                if (cont) continue;
                List<Ingredient> ingredients = new ArrayList<>(shapelessRecipe.ingredients);

                if (inputStack.has(DataComponents.FIREWORKS)){
                    Fireworks fireworks = inputStack.getOrDefault(DataComponents.FIREWORKS, new Fireworks(1, List.of()));
                    for(int i = 1;i < fireworks.flightDuration();i++){
                        ingredients.add(Ingredient.of(Items.GUNPOWDER));
                    }
                }
                List<List<Pair<Item, DataComponentPatch>>> allIngredientCombinations = getShapelessIngredientCombinations(ingredients, inputStack);

                // Create a recipe for each combination
                for (List<Pair<Item, DataComponentPatch>> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(shapelessRecipe.result.item().value().builtInRegistryHolder(), shapelessRecipe.result.count(), inputStack.getComponentsPatch()));
                    Map<Pair<Item, DataComponentPatch>, Integer> allIngredients = new HashMap<>();

                    for (Pair<Item, DataComponentPatch> item : ingredientCombination) {
                        if (item.getLeft() != Items.AIR) {
                            if (outputStack.contains(item)) {
                                ItemStack stack = outputStack.getStack(item);
                                stack.grow(1);
                                outputStack.setOutput(outputStack.indexOf(item), stack);
                            } else {
                                outputStack.addOutput(new ItemStack(item.getLeft().builtInRegistryHolder(), 1, item.getRight()));
                            }
                            allIngredients.put(item, allIngredients.getOrDefault(item, 0) + 1);
                        }
                    }
                    if (inputStack.isDamaged()){
                        Repairable repairableComponent = inputStack.get(DataComponents.REPAIRABLE);
                        if (repairableComponent != null){
                            for (var x : allIngredients.entrySet()){
                                if (repairableComponent.isValidRepairItem(new ItemStack(x.getKey().getLeft(), x.getValue()))){
                                    int damagedPercentage = (int) Math.ceil((double) inputStack.getDamageValue() / inputStack.getMaxDamage() * x.getValue());
                                    while (outputStack.getStack(x.getKey()).getCount() > 0 && damagedPercentage != 0){
                                        if (outputStack.getStack(x.getKey()).is(x.getKey().getLeft())){
                                            outputStack.getStack(x.getKey()).shrink(1);
                                            damagedPercentage--;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        else{
                            blockEntity.status = Status.DAMAGED_ITEM;
                            outputs.clear();
                            return Pair.of(outputs, false);
                        }
                    }
                    outputs.add(outputStack);
                }
            }

            if (r.value() instanceof SmithingTransformRecipe smithingTransformRecipe){
                List<Optional<Ingredient>> ingredients = new ArrayList<>();

                ingredients.add(Optional.of(smithingTransformRecipe.baseIngredient()));
                ingredients.add(smithingTransformRecipe.additionIngredient());
                ingredients.add(smithingTransformRecipe.templateIngredient());

                boolean cont = false;
                for (Optional<Ingredient> ingredient : ingredients){
                    if(ingredient.isPresent()){
                        Ingredient ing = ingredient.get();
                        for (Holder<Item> item : ing.items().toList()){
                            if (BuiltInRegistries.ITEM.getKey(inputStack.getItem()).getNamespace().equals("minecraft") && UncraftEverythingConfig.preventModdedIngredientRecipes() && !item.unwrapKey().get().identifier().getNamespace().equals("minecraft")){
                                DebugLogger.log("[Prevent Modded Ingredient Enabled]");
                                DebugLogger.log("Skipping Recipe: " + r.id().identifier());
                                DebugLogger.log("Skipped item: " + inputStack.typeHolder());
                                cont = true;
                                break;
                            }
                        }
                    }
                }
                if (cont) continue;

                List<List<Pair<Item, DataComponentPatch>>> allIngredientCombinations = getIngredientCombinations(ingredients, inputStack);

                // Create a recipe for each combination
                for (List<Pair<Item, DataComponentPatch>> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(smithingTransformRecipe.result.item().value().builtInRegistryHolder(), 1, inputStack.getComponentsPatch()));

                    for (Pair<Item, DataComponentPatch> item : ingredientCombination) {
                        if (outputStack.contains(item)) {
                            ItemStack stack = outputStack.getStack(item);
                            if (item.getLeft().getDefaultInstance().isDamageableItem()){
                                stack.set(DataComponents.DAMAGE, inputStack.get(DataComponents.DAMAGE));
                            }
                            stack.grow(1);
                            outputStack.setOutput(outputStack.indexOf(item), stack);
                        } else {
                            ItemStack itemStack = new ItemStack(item.getLeft().builtInRegistryHolder(), 1, item.getRight());
                            // If the item is damageable, set the damage to the input stack's damage
                            if (item.getLeft().getDefaultInstance().isDamageableItem()){
                                itemStack.set(DataComponents.DAMAGE, inputStack.get(DataComponents.DAMAGE));
                                if (itemStack.getOrDefault(DataComponents.DAMAGE, 0) >= itemStack.getOrDefault(DataComponents.MAX_DAMAGE, 0)){
                                    itemStack = ItemStack.EMPTY;
                                }
                            }
                            outputStack.addOutput(itemStack);
                        }
                    }
                    outputs.add(outputStack);
                }
            }

            if (r.value() instanceof SmithingTrimRecipe smithingTrimRecipe){
                ArmorTrim armorTrim = inputStack.get(DataComponents.TRIM);
                Optional<Ingredient> additionIngredient = smithingTrimRecipe.additionIngredient();

                List<Optional<Ingredient>> ingredients = new ArrayList<>();
                smithingTrimRecipe.baseIngredient().items().filter(itemHolder -> inputStack.is(itemHolder.value())).forEach(itemHolder -> ingredients.add(Optional.of(Ingredient.of(itemHolder.value()))));
                ingredients.add(smithingTrimRecipe.templateIngredient());
                if (additionIngredient.isPresent() && armorTrim != null){
                    additionIngredient.get().items().filter(itemHolder -> {
                        ResourceKey<Item> itemResourceKey = itemHolder.unwrapKey().orElse(null);
                        ResourceKey<TrimMaterial> armorTrimKey = armorTrim.material().unwrapKey().orElse(null);
                        if (itemResourceKey != null && armorTrimKey != null){
                            return itemResourceKey.identifier().getPath().contains(armorTrimKey.identifier().getPath());
                        }
                        return false;
                    }).forEach(itemHolder -> ingredients.add(Optional.of(Ingredient.of(itemHolder.value()))));
                }

                List<List<Pair<Item, DataComponentPatch>>> allIngredientCombinations = getIngredientCombinations(ingredients, inputStack);
                ItemEnchantments itemEnchantments = inputStack.get(DataComponents.ENCHANTMENTS);

                // Create a recipe for each combination
                for (List<Pair<Item, DataComponentPatch>> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(inputStack.copyWithCount(1));

                    for (Pair<Item, DataComponentPatch> item : ingredientCombination) {
                        if (outputStack.contains(item)) {
                            ItemStack stack = outputStack.getStack(item);
                            if (item.getLeft().getDefaultInstance().is(ingredients.getFirst().isPresent() ? ingredients.getFirst().get().items().toList().getFirst().value() : Items.AIR)){
                                stack.set(DataComponents.ENCHANTMENTS, itemEnchantments);
                                stack.set(DataComponents.DAMAGE, inputStack.get(DataComponents.DAMAGE));
                            }
                            stack.grow(1);
                            outputStack.setOutput(outputStack.indexOf(item), stack);
                        } else {
                            ItemStack itemStack = new ItemStack(item.getLeft().builtInRegistryHolder(), 1, item.getRight());
                            if (item.getLeft().getDefaultInstance().is(ingredients.getFirst().isPresent() ? ingredients.getFirst().get().items().toList().getFirst().value() : Items.AIR)){
                                itemStack.set(DataComponents.ENCHANTMENTS, itemEnchantments);
                                itemStack.set(DataComponents.DAMAGE, inputStack.get(DataComponents.DAMAGE));
                            }
                            outputStack.addOutput(itemStack);
                        }
                    }
                    outputs.add(outputStack);
                }
            }
        }

        return Pair.of(outputs, true);
    }

    public static List<Pair<Item, DataComponentPatch>> getItemsFromIngredient(Ingredient ingredient, ItemStack inputStack) {
        return Services.INGREDIENT.getItemsFromIngredient(ingredient, inputStack);
    }

    public static List<List<Pair<Item, DataComponentPatch>>> getIngredientCombinations(List<Optional<Ingredient>> ingredients, ItemStack inputStack) {
        Map<String, Group> groupKeyToGroup = new HashMap<>();

        for (int i = 0; i < ingredients.size(); i++) {
            Optional<Ingredient> optIngredient = ingredients.get(i);
            List<Pair<Item, DataComponentPatch>> items = optIngredient.map(ingredient -> {
                        List<Pair<Item, DataComponentPatch>> ingredientItems = getItemsFromIngredient(ingredient, inputStack);
                        return ingredientItems.isEmpty() ? List.of(Pair.of(Items.AIR, DataComponentPatch.EMPTY)) : ingredientItems;
                    })
                    .orElse(List.of(Pair.of(Items.AIR, DataComponentPatch.EMPTY)));
            List<Pair<Item, DataComponentPatch>> finalItems1 = items;
            items = items.stream().filter(item -> {
                boolean isVanillaInput = BuiltInRegistries.ITEM.getKey(inputStack.getItem()).getNamespace().equals("minecraft");

                if (isVanillaInput && UncraftEverythingConfig.preventModdedIngredientRecipes()) {
                    return BuiltInRegistries.ITEM.getKey(item.getLeft()).getNamespace().equals("minecraft");
                }
                else if(finalItems1.size() > 1){
                    Identifier ingredientRL = BuiltInRegistries.ITEM.getKey(item.getLeft());
                    return !UncraftEverythingConfig.getRestrictedModIngredients().contains(ingredientRL.getNamespace());
                }
                return true;
            }).toList();

            String key = items.stream()
                    .map(Pair::getLeft)
                    .map(Item::getDescriptionId)
                    .sorted()
                    .collect(Collectors.joining(","));

            List<Pair<Item, DataComponentPatch>> finalItems = items;
            Group group = groupKeyToGroup.computeIfAbsent(key, k -> new Group(new ArrayList<>(), finalItems));
            group.positions.add(i);
        }

        return getLists(groupKeyToGroup, ingredients.size());
    }

    public static List<List<Pair<Item, DataComponentPatch>>> getShapelessIngredientCombinations(List<Ingredient> ingredients, ItemStack inputStack) {
        Map<String, Group> groupKeyToGroup = new HashMap<>();

        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ingredient = ingredients.get(i);
            List<Pair<Item, DataComponentPatch>> items = getItemsFromIngredient(ingredient, inputStack);
            if (items.isEmpty()) items = List.of(Pair.of(Items.AIR, DataComponentPatch.EMPTY));

            List<Pair<Item, DataComponentPatch>> finalItems1 = items;
            items = items.stream().filter(item -> {
                boolean isVanillaInput = BuiltInRegistries.ITEM.getKey(inputStack.getItem()).getNamespace().equals("minecraft");

                if (isVanillaInput && UncraftEverythingConfig.preventModdedIngredientRecipes()) {
                    return BuiltInRegistries.ITEM.getKey(item.getLeft()).getNamespace().equals("minecraft");
                }
                else if(finalItems1.size() > 1){
                    Identifier ingredientRL = BuiltInRegistries.ITEM.getKey(item.getLeft());
                    return !UncraftEverythingConfig.getRestrictedModIngredients().contains(ingredientRL.getNamespace());
                }
                return true;
            }).toList();

            String key = items.stream()
                    .map(Pair::getLeft)
                    .map(Item::getDescriptionId)
                    .sorted()
                    .collect(Collectors.joining(","));

            List<Pair<Item, DataComponentPatch>> finalItems = items;
            Group group = groupKeyToGroup.computeIfAbsent(key, k -> new Group(new ArrayList<>(), finalItems));
            group.positions.add(i);
        }

        return getLists(groupKeyToGroup, ingredients.size());
    }

    @NotNull
    public static List<List<Pair<Item, DataComponentPatch>>> getLists(Map<String, Group> groupKeyToGroup, int size) {
        List<Group> groups = new ArrayList<>(groupKeyToGroup.values());
        List<List<Pair<Item, DataComponentPatch>>> groupChoices = groups.stream()
                .map(group -> group.items)
                .collect(Collectors.toList());

        List<List<Pair<Item, DataComponentPatch>>> product = UncraftingTableHelpers.cartesianProduct(groupChoices);

        List<List<Pair<Item, DataComponentPatch>>> combinations = new ArrayList<>();

        for (List<Pair<Item, DataComponentPatch>> choiceList : product) {
            NonNullList<Pair<Item, DataComponentPatch>> itemsArray = UncraftingTableHelpers.getTuples(size, choiceList, groups);
            combinations.add(itemsArray);
        }

        return combinations;
    }

    public static @NotNull NonNullList<Pair<Item, DataComponentPatch>> getTuples(int size, List<Pair<Item, DataComponentPatch>> choiceList, List<Group> groups) {
        NonNullList<Pair<Item, DataComponentPatch>> itemsArray = NonNullList.withSize(size, Pair.of(Items.AIR, DataComponentPatch.EMPTY));

        for (int groupIdx = 0; groupIdx < groups.size(); groupIdx++) {
            Group group = groups.get(groupIdx);
            Pair<Item, DataComponentPatch> chosenItem = choiceList.get(groupIdx);
            for (int pos : group.positions) {
                if (pos >= 0 && pos < itemsArray.size()) {
                    itemsArray.set(pos, chosenItem);
                }
            }
        }
        return itemsArray;
    }

    public static <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
        List<List<T>> result = new ArrayList<>();
        if (lists.isEmpty()) {
            result.add(new ArrayList<>());
            return result;
        }

        List<T> firstList = lists.getFirst();
        List<List<T>> remainingLists = cartesianProduct(lists.subList(1, lists.size()));

        for (T item : firstList) {
            for (List<T> remaining : remainingLists) {
                List<T> combination = new ArrayList<>();
                combination.add(item);
                combination.addAll(remaining);
                result.add(combination);
            }
        }

        return result;
    }

    @SuppressWarnings("unused")
    public static boolean isVanillaIngredientRecipe(Recipe<?> recipe) {
        return Services.INGREDIENT.isVanillaIngredientRecipe(recipe);
    }

    public static class Group {
        public List<Integer> positions;
        public List<Pair<Item, DataComponentPatch>> items;

        public Group(List<Integer> positions, List<Pair<Item, DataComponentPatch>> items) {
            this.positions = positions;
            this.items = items;
        }
    }
}