package com.coolerpromc.uncrafteverything.util;

import com.coolerpromc.uncrafteverything.blockentity.custom.AbstractUncraftingTableBE;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.ComponentsIngredient;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.recipe.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.context.ContextParameterMap;
import net.minecraft.util.context.ContextType;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class UncraftingTableHelpers {
    public static <T extends AbstractUncraftingTableBE> boolean validateInput(ItemStack inputStack, @Nullable ServerPlayerEntity player, T blockEntity){
        if (inputStack.isEmpty()
                || UncraftEverythingConfig.isItemLocked(player, inputStack).getLeft()
                || (inputStack.getDamage() > 0 && !UncraftEverythingConfig.allowDamaged())
                || UncraftEverythingConfig.isItemBlacklisted(inputStack)
                || UncraftEverythingConfig.isItemWhitelisted(inputStack)
                || (!UncraftEverythingConfig.isEnchantedItemsAllowed(inputStack) && !inputStack.contains(DataComponentTypes.TRIM))
                || (inputStack.getItem() == Items.SHULKER_BOX && inputStack.get(DataComponentTypes.CONTAINER) != ContainerComponent.DEFAULT)
                || (inputStack.getItem() == Items.ENCHANTED_BOOK)
        ) {
            if (inputStack.getDamage() > 0 && !UncraftEverythingConfig.allowDamaged()){
                blockEntity.status = Status.DAMAGED_ITEM;
            }

            if (UncraftEverythingConfig.isItemBlacklisted(inputStack)){
                blockEntity.status = Status.RESTRICTED_BY_CONFIG;
            }

            if (UncraftEverythingConfig.isItemWhitelisted(inputStack)){
                blockEntity.status = Status.RESTRICTED_BY_CONFIG;
            }

            if (!UncraftEverythingConfig.isEnchantedItemsAllowed(inputStack) && !inputStack.contains(DataComponentTypes.TRIM)){
                blockEntity.status = Status.ENCHANTED_ITEM;
            }

            if(inputStack.getItem() == Items.SHULKER_BOX && inputStack.get(DataComponentTypes.CONTAINER) != ContainerComponent.DEFAULT){
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

    public static <T extends AbstractUncraftingTableBE> List<RecipeEntry<?>> findRecipe(ServerWorld serverLevel, ItemStack inputStack, T blockEntity){
        return serverLevel.getRecipeManager().values().stream().filter(RecipeEntry -> {
            if (!RecipeEntry.id().getValue().getNamespace().equals("minecraft") && Registries.ITEM.getId(inputStack.getItem()).getNamespace().equals("minecraft") && UncraftEverythingConfig.preventModdedIngredientRecipes()){
                return false;
            }

            if (RecipeEntry.value() instanceof ShapedRecipe shapedRecipe){
                return validateRecipe(shapedRecipe.result, inputStack, blockEntity);
            }

            if (RecipeEntry.value() instanceof ShapelessRecipe shapelessRecipe){
                return validateRecipe(shapelessRecipe.result, inputStack, blockEntity);
            }

            if(RecipeEntry.value() instanceof TransmuteRecipe transmuteRecipe){
                ItemStack stack = inputStack.copy();
                if (stack.contains(DataComponentTypes.CONTAINER)) stack.set(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
                if (stack.contains(DataComponentTypes.BUNDLE_CONTENTS)) stack.set(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
                return ItemStack.areItemsAndComponentsEqual(stack, new ItemStack(transmuteRecipe.result.itemEntry(), transmuteRecipe.result.count(), transmuteRecipe.result.components()));
            }

            if (RecipeEntry.value() instanceof SmithingTransformRecipe smithingTransformRecipe){
                if (!UncraftEverythingConfig.allowUnSmithing() || (inputStack.get(DataComponentTypes.ENCHANTMENTS) != ItemEnchantmentsComponent.DEFAULT && UncraftEverythingConfig.outputEnchantedBook())){
                    return false;
                }
                return validateSmithingRecipe(smithingTransformRecipe, inputStack);
            }

            if (RecipeEntry.value() instanceof SmithingTrimRecipe smithingTrimRecipe){
                if (!UncraftEverythingConfig.allowUnSmithing()){
                    return false;
                }
                ArmorTrim armorTrim = inputStack.get(DataComponentTypes.TRIM);

                if (armorTrim != null){
                    Optional<Ingredient> ingredient = smithingTrimRecipe.addition();
                    if (ingredient.isPresent() && armorTrim.pattern().matches(smithingTrimRecipe.pattern)){
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
        if (inputStack.get(DataComponentTypes.ENCHANTMENTS) != ItemEnchantmentsComponent.DEFAULT && UncraftEverythingConfig.outputEnchantedBook()){
            return false;
        }
        if (inputStack.isDamaged()){
            return result.getItem() == inputStack.getItem() && inputStack.getCount() >= result.getCount();
        }
        if (inputStack.get(DataComponentTypes.ENCHANTMENTS) != ItemEnchantmentsComponent.DEFAULT && UncraftEverythingConfig.allowEnchantedItems && result.getItem() == inputStack.getItem()){
            return true;
        }
        return ItemStack.areItemsAndComponentsEqual(result, inputStack) && inputStack.getCount() >= result.getCount();
    }

    public static boolean validateSmithingRecipe(SmithingTransformRecipe smithingTransformRecipe, ItemStack inputStack){
        if (inputStack.get(DataComponentTypes.ENCHANTMENTS) != ItemEnchantmentsComponent.DEFAULT && UncraftEverythingConfig.outputEnchantedBook()){
            return false;
        }
        if (inputStack.isDamaged()){
            return inputStack.isOf(smithingTransformRecipe.result.itemEntry().value()) && inputStack.getCount() >= smithingTransformRecipe.result.count();
        }
        if (inputStack.get(DataComponentTypes.ENCHANTMENTS) != ItemEnchantmentsComponent.DEFAULT && UncraftEverythingConfig.allowEnchantedItems && smithingTransformRecipe.result.itemEntry().value() == inputStack.getItem()){
            return true;
        }
        return ItemStack.areItemsAndComponentsEqual(inputStack, new ItemStack(smithingTransformRecipe.result.itemEntry(), smithingTransformRecipe.result.count(), smithingTransformRecipe.result.components()));
    }


    public static <T extends AbstractUncraftingTableBE> Pair<List<UncraftingTableRecipe>, Boolean> getOutputs(ItemStack inputStack, List<RecipeEntry<?>> recipes, T blockEntity){
        List<UncraftingTableRecipe> outputs = new ArrayList<>();

        if (inputStack.isOf(Items.TIPPED_ARROW)){
            PotionContentsComponent potionContents = inputStack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
            UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(inputStack.getItem().getRegistryEntry(), 8, inputStack.getComponentChanges()));
            ItemStack potion = new ItemStack(Items.LINGERING_POTION);
            potion.set(DataComponentTypes.POTION_CONTENTS, potionContents);

            outputStack.addOutput(new ItemStack(Items.ARROW, 8));
            outputStack.addOutput(potion);
            outputs.add(outputStack);
        }

        if (inputStack.get(DataComponentTypes.ENCHANTMENTS) != ItemEnchantmentsComponent.DEFAULT && recipes.isEmpty() && UncraftEverythingConfig.outputEnchantedBook()){
            UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(inputStack.getItem().getRegistryEntry(), 1, inputStack.getComponentChanges()));
            ItemEnchantmentsComponent enchantments = inputStack.get(DataComponentTypes.ENCHANTMENTS);
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            book.set(DataComponentTypes.STORED_ENCHANTMENTS, enchantments);
            ItemStack output = new ItemStack(inputStack.getItem(), 1);
            output.setDamage(inputStack.getDamage());

            outputStack.addOutput(output);
            outputStack.addOutput(book);

            outputs.add(outputStack);
        }

        for (RecipeEntry<?> r : recipes) {
            if (r.value() instanceof TransmuteRecipe transmuteRecipe){
                List<Ingredient> ingredients = List.of(transmuteRecipe.input, transmuteRecipe.material);
                List<List<Pair<Item, ComponentChanges>>> allIngredientCombinations = getLeftllShapelessIngredientCombinations(ingredients, inputStack);

                for (List<Pair<Item, ComponentChanges>> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(transmuteRecipe.result.itemEntry().value().getRegistryEntry(), 1, inputStack.getComponentChanges()));

                    for (Pair<Item, ComponentChanges> item : ingredientCombination) {
                        if (outputStack.contains(item)) {
                            outputStack.getStack(item).increment(1);
                        } else {
                            ItemStack itemStack = new ItemStack(item.getLeft().getRegistryEntry(), 1, item.getRight());
                            if (itemStack.contains(DataComponentTypes.CONTAINER)) itemStack.set(DataComponentTypes.CONTAINER, inputStack.get(DataComponentTypes.CONTAINER));
                            if (itemStack.contains(DataComponentTypes.BUNDLE_CONTENTS)) itemStack.set(DataComponentTypes.BUNDLE_CONTENTS, inputStack.get(DataComponentTypes.BUNDLE_CONTENTS));
                            outputStack.addOutput(itemStack);
                        }
                    }
                    outputs.add(outputStack);
                }
            }

            if (r.value() instanceof ShapedRecipe shapedRecipe) {
                // Get all possible combinations of ingredients
                List<List<Pair<Item, ComponentChanges>>> allIngredientCombinations = getLeftllIngredientCombinations(shapedRecipe.getIngredients(), inputStack);

                // Create a recipe for each combination
                for (List<Pair<Item, ComponentChanges>> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(shapedRecipe.result.getItem().getRegistryEntry(), shapedRecipe.result.getCount(), inputStack.getComponentChanges()));
                    Map<Pair<Item, ComponentChanges>, Integer> allIngredients = new HashMap<>();

                    for (Pair<Item, ComponentChanges> item : ingredientCombination) {
                        if (outputStack.contains(item)) {
                            ItemStack stack = outputStack.getStack(item);
                            stack.increment(1);
                            outputStack.setOutput(outputStack.indexOf(item), stack);
                        } else {
                            outputStack.addOutput(new ItemStack(item.getLeft().getRegistryEntry(), 1, item.getRight()));
                        }
                        allIngredients.put(item, allIngredients.getOrDefault(item, 0) + 1);
                    }
                    if (inputStack.isDamaged()){
                        RepairableComponent repairableComponent = inputStack.get(DataComponentTypes.REPAIRABLE);
                        if (repairableComponent != null){
                            for (var x : allIngredients.entrySet()){
                                if (repairableComponent.matches(new ItemStack(x.getKey().getLeft(), x.getValue()))){
                                    int damagedPercentage = (int) Math.ceil((double) inputStack.getDamage() / inputStack.getMaxDamage() * x.getValue());
                                    while (outputStack.getStack(x.getKey()).getCount() > 0 && damagedPercentage != 0){
                                        if (outputStack.getStack(x.getKey()).isOf(x.getKey().getLeft())){
                                            outputStack.getStack(x.getKey()).decrement(1);
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
                List<Ingredient> ingredients = new ArrayList<>(shapelessRecipe.ingredients);

                if (inputStack.contains(DataComponentTypes.FIREWORKS)){
                    FireworksComponent fireworks = inputStack.getOrDefault(DataComponentTypes.FIREWORKS, new FireworksComponent(1, List.of()));
                    for(int i = 1;i < fireworks.flightDuration();i++){
                        ingredients.add(Ingredient.ofItem(Items.GUNPOWDER));
                    }
                }
                List<List<Pair<Item, ComponentChanges>>> allIngredientCombinations = getLeftllShapelessIngredientCombinations(ingredients, inputStack);

                // Create a recipe for each combination
                for (List<Pair<Item, ComponentChanges>> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(shapelessRecipe.result.getItem().getRegistryEntry(), shapelessRecipe.result.getCount(), inputStack.getComponentChanges()));
                    Map<Pair<Item, ComponentChanges>, Integer> allIngredients = new HashMap<>();

                    for (Pair<Item, ComponentChanges> item : ingredientCombination) {
                        if (item.getLeft() != Items.AIR) {
                            if (outputStack.contains(item)) {
                                ItemStack stack = outputStack.getStack(item);
                                stack.increment(1);
                                outputStack.setOutput(outputStack.indexOf(item), stack);
                            } else {
                                outputStack.addOutput(new ItemStack(item.getLeft().getRegistryEntry(), 1, item.getRight()));
                            }
                            allIngredients.put(item, allIngredients.getOrDefault(item, 0) + 1);
                        }
                    }
                    if (inputStack.isDamaged()){
                        RepairableComponent repairableComponent = inputStack.get(DataComponentTypes.REPAIRABLE);
                        if (repairableComponent != null){
                            for (var x : allIngredients.entrySet()){
                                if (repairableComponent.matches(new ItemStack(x.getKey().getLeft(), x.getValue()))){
                                    int damagedPercentage = (int) Math.ceil((double) inputStack.getDamage() / inputStack.getMaxDamage() * x.getValue());
                                    while (outputStack.getStack(x.getKey()).getCount() > 0 && damagedPercentage != 0){
                                        if (outputStack.getStack(x.getKey()).isOf(x.getKey().getLeft())){
                                            outputStack.getStack(x.getKey()).decrement(1);
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

                ingredients.add(Optional.of(smithingTransformRecipe.base()));
                ingredients.add(smithingTransformRecipe.addition());
                ingredients.add(smithingTransformRecipe.template());

                List<List<Pair<Item, ComponentChanges>>> allIngredientCombinations = getLeftllIngredientCombinations(ingredients, inputStack);

                // Create a recipe for each combination
                for (List<Pair<Item, ComponentChanges>> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(smithingTransformRecipe.result.itemEntry().value().getRegistryEntry(), 1, inputStack.getComponentChanges()));

                    for (Pair<Item, ComponentChanges> item : ingredientCombination) {
                        if (outputStack.contains(item)) {
                            ItemStack stack = outputStack.getStack(item);
                            if (item.getLeft().getDefaultStack().isDamageable()){
                                stack.set(DataComponentTypes.DAMAGE, inputStack.get(DataComponentTypes.DAMAGE));
                            }
                            stack.increment(1);
                            outputStack.setOutput(outputStack.indexOf(item), stack);
                        } else {
                            ItemStack itemStack = new ItemStack(item.getLeft().getRegistryEntry(), 1, item.getRight());
                            // If the item is damageable, set the damage to the input stack's damage
                            if (item.getLeft().getDefaultStack().isDamageable()){
                                itemStack.set(DataComponentTypes.DAMAGE, inputStack.get(DataComponentTypes.DAMAGE));
                                if (itemStack.getOrDefault(DataComponentTypes.DAMAGE, 0) >= itemStack.getOrDefault(DataComponentTypes.MAX_DAMAGE, 0)){
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
                ArmorTrim armorTrim = inputStack.get(DataComponentTypes.TRIM);
                Optional<Ingredient> additionIngredient = smithingTrimRecipe.addition();

                List<Optional<Ingredient>> ingredients = new ArrayList<>();
                smithingTrimRecipe.base().getMatchingItems().filter(itemHolder -> inputStack.isOf(itemHolder.value())).forEach(itemHolder -> ingredients.add(Optional.of(Ingredient.ofItem(itemHolder.value()))));
                ingredients.add(smithingTrimRecipe.template());
                if (additionIngredient.isPresent() && armorTrim != null){
                    additionIngredient.get().getMatchingItems().filter(itemHolder -> {
                        RegistryKey<Item> itemResourceKey = itemHolder.getKey().orElse(null);
                        RegistryKey<ArmorTrimMaterial> armorTrimKey = armorTrim.material().getKey().orElse(null);
                        if (itemResourceKey != null && armorTrimKey != null){
                            return itemResourceKey.getValue().getPath().contains(armorTrimKey.getValue().getPath());
                        }
                        return false;
                    }).forEach(itemHolder -> ingredients.add(Optional.of(Ingredient.ofItem(itemHolder.value()))));
                }

                List<List<Pair<Item, ComponentChanges>>> allIngredientCombinations = getLeftllIngredientCombinations(ingredients, inputStack);
                ItemEnchantmentsComponent itemEnchantments = inputStack.get(DataComponentTypes.ENCHANTMENTS);

                // Create a recipe for each combination
                for (List<Pair<Item, ComponentChanges>> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(inputStack.copyWithCount(1));

                    for (Pair<Item, ComponentChanges> item : ingredientCombination) {
                        if (outputStack.contains(item)) {
                            ItemStack stack = outputStack.getStack(item);
                            if (item.getLeft().getDefaultStack().isOf(ingredients.getFirst().isPresent() ? ingredients.getFirst().get().getMatchingItems().toList().getFirst().value() : Items.AIR)){
                                stack.set(DataComponentTypes.ENCHANTMENTS, itemEnchantments);
                                stack.set(DataComponentTypes.DAMAGE, inputStack.get(DataComponentTypes.DAMAGE));
                            }
                            stack.increment(1);
                            outputStack.setOutput(outputStack.indexOf(item), stack);
                        } else {
                            ItemStack itemStack = new ItemStack(item.getLeft().getRegistryEntry(), 1, item.getRight());
                            if (item.getLeft().getDefaultStack().isOf(ingredients.getFirst().isPresent() ? ingredients.getFirst().get().getMatchingItems().toList().getFirst().value() : Items.AIR)){
                                itemStack.set(DataComponentTypes.ENCHANTMENTS, itemEnchantments);
                                itemStack.set(DataComponentTypes.DAMAGE, inputStack.get(DataComponentTypes.DAMAGE));
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

    public static List<Pair<Item, ComponentChanges>> getItemsFromIngredient(Ingredient ingredient, ItemStack inputStack) {
        List<Pair<Item, ComponentChanges>> items = new ArrayList<>();

        if (ingredient.getCustomIngredient() != null && !ingredient.getCustomIngredient().getMatchingItems().toList().isEmpty()) {
            if (ingredient.getCustomIngredient() instanceof ComponentsIngredient dataComponentIngredient){
                for (var holder : dataComponentIngredient.getMatchingItems().toList()) {
                    items.add(Pair.of(holder.value(), dataComponentIngredient.toDisplay().getFirst(new ContextParameterMap.Builder().build(new ContextType.Builder().build())).getComponentChanges()));
                }
            }
            else{
                for (var holder : ingredient.getCustomIngredient().getMatchingItems().toList()) {
                    items.add(Pair.of(holder.value(), ComponentChanges.EMPTY));
                }
            }
        }
        else {
            try {
                items = ingredient.getMatchingItems()
                        .map(holder -> Pair.of(holder.value(), ComponentChanges.EMPTY))
                        .distinct()
                        .toList();
            } catch (IllegalStateException e) {
                LogUtils.getLogger().warn("Skipping unsupported ingredient type: {}", ingredient);
                return Collections.emptyList();
            }
        }

        return items.stream()
                .filter(item -> filterIngredient(item, inputStack))
                .sorted(Comparator.comparing(tuple -> tuple.getLeft().getTranslationKey()))
                .toList();
    }

    private static boolean filterIngredient(Pair<Item, ComponentChanges> item, ItemStack inputStack){
        if (item.getLeft().getTranslationKey().contains("shulker_box") && inputStack.getItem().getRegistryEntry().registryKey().getValue().getPath().contains("_shulker_box")){
            return item.getLeft() == Items.SHULKER_BOX;
        }
        if (item.getLeft().getTranslationKey().contains("bundle") && inputStack.getItem().getRegistryEntry().registryKey().getValue().getPath().contains("_bundle")){
            return item.getLeft() == Items.BUNDLE;
        }
        if (item.getLeft().getTranslationKey().contains("wool") && inputStack.getItem().getRegistryEntry().registryKey().getValue().getPath().contains("_wool")){
            return item.getLeft() == Items.WHITE_WOOL;
        }
        if (item.getLeft().getTranslationKey().contains("bed") && inputStack.getItem().getRegistryEntry().registryKey().getValue().getPath().contains("_bed") && inputStack.getItem().getRegistryEntry().registryKey().getValue().getNamespace().contains("minecraft")){
            return item.getLeft() == Items.WHITE_BED;
        }
        if (item.getLeft().getTranslationKey().contains("carpet") && inputStack.getItem().getRegistryEntry().registryKey().getValue().getPath().contains("_carpet")){
            return item.getLeft() == Items.WHITE_CARPET;
        }
        if (item.getLeft().getTranslationKey().contains("harness") && inputStack.getItem().getRegistryEntry().registryKey().getValue().getPath().contains("_harness")){
            return inputStack.getItem() == Items.WHITE_HARNESS ? item.getLeft() == Items.GRAY_HARNESS : item.getLeft() == Items.WHITE_HARNESS;
        }
        return item.getLeft().getRecipeRemainder(item.getLeft().getDefaultStack()) == ItemStack.EMPTY || item.getLeft().getRecipeRemainder(item.getLeft().getDefaultStack()).getItem() != item.getLeft().getDefaultStack().getItem();
    }

    public static List<List<Pair<Item, ComponentChanges>>> getLeftllIngredientCombinations(List<Optional<Ingredient>> ingredients, ItemStack inputStack) {
        Map<String, UncraftingTableHelpers.Group> groupKeyToGroup = new HashMap<>();

        for (int i = 0; i < ingredients.size(); i++) {
            Optional<Ingredient> optIngredient = ingredients.get(i);
            List<Pair<Item, ComponentChanges>> items = optIngredient.map(ingredient -> {
                        List<Pair<Item, ComponentChanges>> ingredientItems = getItemsFromIngredient(ingredient, inputStack);
                        return ingredientItems.isEmpty() ? List.of(Pair.of(Items.AIR, ComponentChanges.EMPTY)) : ingredientItems;
                    })
                    .orElse(List.of(Pair.of(Items.AIR, ComponentChanges.EMPTY)));
            List<Pair<Item, ComponentChanges>> finalItems1 = items;
            items = items.stream().filter(item -> {
                boolean isVanillaInput = Registries.ITEM.getId(inputStack.getItem()).getNamespace().equals("minecraft");

                if (isVanillaInput && UncraftEverythingConfig.preventModdedIngredientRecipes()) {
                    return Registries.ITEM.getId(item.getLeft()).getNamespace().equals("minecraft");
                }
                else if(finalItems1.size() > 1){
                    Identifier ingredientRL = Registries.ITEM.getId(item.getLeft());
                    return !UncraftEverythingConfig.getRestrictedModIngredients().contains(ingredientRL.getNamespace());
                }
                return true;
            }).toList();

            String key = items.stream()
                    .map(Pair::getLeft)
                    .map(Item::getTranslationKey)
                    .sorted()
                    .collect(Collectors.joining(","));

            List<Pair<Item, ComponentChanges>> finalItems = items;
            UncraftingTableHelpers.Group group = groupKeyToGroup.computeIfAbsent(key, k -> new UncraftingTableHelpers.Group(new ArrayList<>(), finalItems));
            group.positions.add(i);
        }

        return getLists(groupKeyToGroup, ingredients.size());
    }

    public static List<List<Pair<Item, ComponentChanges>>> getLeftllShapelessIngredientCombinations(List<Ingredient> ingredients, ItemStack inputStack) {
        Map<String, UncraftingTableHelpers.Group> groupKeyToGroup = new HashMap<>();

        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ingredient = ingredients.get(i);
            List<Pair<Item, ComponentChanges>> items = getItemsFromIngredient(ingredient, inputStack);
            if (items.isEmpty()) items = List.of(Pair.of(Items.AIR, ComponentChanges.EMPTY));

            List<Pair<Item, ComponentChanges>> finalItems1 = items;
            items = items.stream().filter(item -> {
                boolean isVanillaInput = Registries.ITEM.getId(inputStack.getItem()).getNamespace().equals("minecraft");

                if (isVanillaInput && UncraftEverythingConfig.preventModdedIngredientRecipes()) {
                    return Registries.ITEM.getId(item.getLeft()).getNamespace().equals("minecraft");
                }
                else if(finalItems1.size() > 1){
                    Identifier ingredientRL = Registries.ITEM.getId(item.getLeft());
                    return !UncraftEverythingConfig.getRestrictedModIngredients().contains(ingredientRL.getNamespace());
                }
                return true;
            }).toList();

            String key = items.stream()
                    .map(Pair::getLeft)
                    .map(Item::getTranslationKey)
                    .sorted()
                    .collect(Collectors.joining(","));

            List<Pair<Item, ComponentChanges>> finalItems = items;
            UncraftingTableHelpers.Group group = groupKeyToGroup.computeIfAbsent(key, k -> new UncraftingTableHelpers.Group(new ArrayList<>(), finalItems));
            group.positions.add(i);
        }

        return getLists(groupKeyToGroup, ingredients.size());
    }

    @NotNull
    public static List<List<Pair<Item, ComponentChanges>>> getLists(Map<String, UncraftingTableHelpers.Group> groupKeyToGroup, int size) {
        List<UncraftingTableHelpers.Group> groups = new ArrayList<>(groupKeyToGroup.values());
        List<List<Pair<Item, ComponentChanges>>> groupChoices = groups.stream()
                .map(group -> group.items)
                .collect(Collectors.toList());

        List<List<Pair<Item, ComponentChanges>>> product = UncraftingTableHelpers.cartesianProduct(groupChoices);

        List<List<Pair<Item, ComponentChanges>>> combinations = new ArrayList<>();

        for (List<Pair<Item, ComponentChanges>> choiceList : product) {
            DefaultedList<Pair<Item, ComponentChanges>> itemsArray = UncraftingTableHelpers.getTuples(size, choiceList, groups);
            combinations.add(itemsArray);
        }

        return combinations;
    }

    public static @NotNull DefaultedList<Pair<Item, ComponentChanges>> getTuples(int size, List<Pair<Item, ComponentChanges>> choiceList, List<Group> groups) {
        DefaultedList<Pair<Item, ComponentChanges>> itemsArray = DefaultedList.ofSize(size, Pair.of(Items.AIR, ComponentChanges.EMPTY));

        for (int groupIdx = 0; groupIdx < groups.size(); groupIdx++) {
            Group group = groups.get(groupIdx);
            Pair<Item, ComponentChanges> chosenItem = choiceList.get(groupIdx);
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
        List<Optional<Ingredient>> ingredients;

        switch (recipe) {
            case ShapedRecipe shaped -> ingredients = shaped.getIngredients();
            case ShapelessRecipe shapeless -> ingredients = shapeless.ingredients.stream().map(Optional::of).toList();
            case SmithingTransformRecipe smithingTransformRecipe -> ingredients = List.of(
                    Optional.of(smithingTransformRecipe.base()),
                    smithingTransformRecipe.addition(),
                    smithingTransformRecipe.template()
            );
            case null, default -> {
                return true;
            }
        }

        for (Optional<Ingredient> ingredient : ingredients) {
            if (ingredient.isPresent()){
                if (ingredient.get().getCustomIngredient() != null && !ingredient.get().getCustomIngredient().getMatchingItems().toList().isEmpty()) {
                    if (!ingredient.get().getCustomIngredient().getMatchingItems().map(RegistryEntry::value).map(Registries.ITEM::getId).map(Identifier::getNamespace).toList().contains("minecraft")) {
                        return false;
                    }
                }
                else{
                    if (!ingredient.get().getMatchingItems().map(RegistryEntry::value).map(Registries.ITEM::getId).map(Identifier::getNamespace).toList().contains("minecraft")) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static class Group {
        public List<Integer> positions;
        public List<Pair<Item, ComponentChanges>> items;

        public Group(List<Integer> positions, List<Pair<Item, ComponentChanges>> items) {
            this.positions = positions;
            this.items = items;
        }
    }
}