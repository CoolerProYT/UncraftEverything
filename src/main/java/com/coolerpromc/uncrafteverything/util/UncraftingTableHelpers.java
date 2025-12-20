package com.coolerpromc.uncrafteverything.util;

import com.coolerpromc.uncrafteverything.blockentity.custom.AbstractUncraftingTableBE;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
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
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
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
                || (inputStack.getDamageValue() > 0 && !UncraftEverythingConfig.CONFIG.allowDamaged())
                || UncraftEverythingConfig.CONFIG.isItemBlacklisted(inputStack)
                || UncraftEverythingConfig.CONFIG.isItemWhitelisted(inputStack)
                || (!UncraftEverythingConfig.CONFIG.isEnchantedItemsAllowed(inputStack) && !inputStack.has(DataComponents.TRIM))
                || (inputStack.getItem() == Items.SHULKER_BOX && inputStack.get(DataComponents.CONTAINER) != ItemContainerContents.EMPTY)
                || (inputStack.getItem() == Items.ENCHANTED_BOOK)
        ) {
            if (inputStack.getDamageValue() > 0 && !UncraftEverythingConfig.CONFIG.allowDamaged()){
                blockEntity.status = Status.DAMAGED_ITEM;
            }

            if (UncraftEverythingConfig.CONFIG.isItemBlacklisted(inputStack)){
                blockEntity.status = Status.RESTRICTED_BY_CONFIG;
            }

            if (UncraftEverythingConfig.CONFIG.isItemWhitelisted(inputStack)){
                blockEntity.status = Status.RESTRICTED_BY_CONFIG;
            }

            if (!UncraftEverythingConfig.CONFIG.isEnchantedItemsAllowed(inputStack) && !inputStack.has(DataComponents.TRIM)){
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

    public static <T extends AbstractUncraftingTableBE> List<RecipeHolder<?>> findRecipe(ServerLevel serverLevel, ItemStack inputStack, T blockEntity){
        return serverLevel.recipeAccess().getRecipes().stream().filter(recipeHolder -> {
            if (!recipeHolder.id().identifier().getNamespace().equals("minecraft") && BuiltInRegistries.ITEM.getKey(inputStack.getItem()).getNamespace().equals("minecraft") && UncraftEverythingConfig.CONFIG.preventModdedIngredientRecipes()){
                return false;
            }

            if (recipeHolder.value() instanceof ShapedRecipe shapedRecipe){
                return validateRecipe(shapedRecipe.result, inputStack, blockEntity);
            }

            if (recipeHolder.value() instanceof ShapelessRecipe shapelessRecipe){
                return validateRecipe(shapelessRecipe.result, inputStack, blockEntity);
            }

            if(recipeHolder.value() instanceof TransmuteRecipe transmuteRecipe){
                ItemStack stack = inputStack.copy();
                if (stack.has(DataComponents.CONTAINER)) stack.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
                if (stack.has(DataComponents.BUNDLE_CONTENTS)) stack.set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
                return ItemStack.isSameItemSameComponents(stack, new ItemStack(transmuteRecipe.result.item(), transmuteRecipe.result.count(), transmuteRecipe.result.components()));
            }

            if (recipeHolder.value() instanceof SmithingTransformRecipe smithingTransformRecipe){
                if (!UncraftEverythingConfig.CONFIG.allowUnSmithing() || (inputStack.get(DataComponents.ENCHANTMENTS) != ItemEnchantments.EMPTY && UncraftEverythingConfig.CONFIG.outputEnchantedBook())){
                    return false;
                }
                return validateSmithingRecipe(smithingTransformRecipe, inputStack);
            }

            if (recipeHolder.value() instanceof SmithingTrimRecipe smithingTrimRecipe){
                if (!UncraftEverythingConfig.CONFIG.allowUnSmithing()){
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
        if (inputStack.get(DataComponents.ENCHANTMENTS) != ItemEnchantments.EMPTY && UncraftEverythingConfig.CONFIG.outputEnchantedBook()){
            return false;
        }
        if (inputStack.isDamaged()){
            return result.getItem() == inputStack.getItem() && inputStack.getCount() >= result.getCount();
        }
        try{
            if (ModList.get().isLoaded("travelersbackpack")) {
                if (result.getItem() instanceof com.tiviacz.travelersbackpack.items.TravelersBackpackItem) {
                    return ItemStack.isSameItem(result, inputStack);
                }
            }
        }
        catch (Exception ignored){

        }
        if (inputStack.get(DataComponents.ENCHANTMENTS) != ItemEnchantments.EMPTY && UncraftEverythingConfig.CONFIG.allowEnchantedItems.getAsBoolean() && result.getItem() == inputStack.getItem()){
            return true;
        }
        return ItemStack.isSameItemSameComponents(result, inputStack) && inputStack.getCount() >= result.getCount();
    }

    public static boolean validateSmithingRecipe(SmithingTransformRecipe smithingTransformRecipe, ItemStack inputStack){
        if (inputStack.get(DataComponents.ENCHANTMENTS) != ItemEnchantments.EMPTY && UncraftEverythingConfig.CONFIG.outputEnchantedBook()){
            return false;
        }
        if (inputStack.isDamaged()){
            return inputStack.is(smithingTransformRecipe.result.item()) && inputStack.getCount() >= smithingTransformRecipe.result.count();
        }
        if (inputStack.get(DataComponents.ENCHANTMENTS) != ItemEnchantments.EMPTY && UncraftEverythingConfig.CONFIG.allowEnchantedItems.getAsBoolean() && smithingTransformRecipe.result.item().value() == inputStack.getItem()){
            return true;
        }
        return ItemStack.isSameItemSameComponents(inputStack, new ItemStack(smithingTransformRecipe.result.item(), smithingTransformRecipe.result.count(), smithingTransformRecipe.result.components()));
    }

    public static <T extends AbstractUncraftingTableBE> Tuple<List<UncraftingTableRecipe>, Boolean> getOutputs(ItemStack inputStack, List<RecipeHolder<?>> recipes, T blockEntity){
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

        if (inputStack.get(DataComponents.ENCHANTMENTS) != ItemEnchantments.EMPTY && recipes.isEmpty() && UncraftEverythingConfig.CONFIG.outputEnchantedBook()){
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
                List<List<Tuple<Item, DataComponentPatch>>> allIngredientCombinations = getAllShapelessIngredientCombinations(ingredients, inputStack);

                for (List<Tuple<Item, DataComponentPatch>> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(transmuteRecipe.result.item().value().builtInRegistryHolder(), 1, inputStack.getComponentsPatch()));

                    for (Tuple<Item, DataComponentPatch> item : ingredientCombination) {
                        if (outputStack.contains(item)) {
                            outputStack.getStack(item).grow(1);
                        } else {
                            ItemStack itemStack = new ItemStack(item.getA().builtInRegistryHolder(), 1, item.getB());
                            if (itemStack.has(DataComponents.CONTAINER)) itemStack.set(DataComponents.CONTAINER, inputStack.get(DataComponents.CONTAINER));
                            if (itemStack.has(DataComponents.BUNDLE_CONTENTS)) itemStack.set(DataComponents.BUNDLE_CONTENTS, inputStack.get(DataComponents.BUNDLE_CONTENTS));
                            outputStack.addOutput(itemStack);
                        }
                    }
                    outputs.add(outputStack);
                }
            }

            if (r.value() instanceof ShapedRecipe shapedRecipe) {
                // Get all possible combinations of ingredients
                List<List<Tuple<Item, DataComponentPatch>>> allIngredientCombinations = getAllIngredientCombinations(shapedRecipe.getIngredients(), inputStack);

                // Create a recipe for each combination
                for (List<Tuple<Item, DataComponentPatch>> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(shapedRecipe.result.getItem().builtInRegistryHolder(), shapedRecipe.result.getCount(), inputStack.getComponentsPatch()));
                    Map<Tuple<Item, DataComponentPatch>, Integer> allIngredients = new HashMap<>();

                    for (Tuple<Item, DataComponentPatch> item : ingredientCombination) {
                        if (outputStack.contains(item)) {
                            ItemStack stack = outputStack.getStack(item);
                            stack.grow(1);
                            outputStack.setOutput(outputStack.indexOf(item), stack);
                        } else {
                            outputStack.addOutput(new ItemStack(item.getA().builtInRegistryHolder(), 1, item.getB()));
                        }
                        allIngredients.put(item, allIngredients.getOrDefault(item, 0) + 1);
                    }
                    if (inputStack.isDamaged()){
                        Repairable repairableComponent = inputStack.get(DataComponents.REPAIRABLE);
                        if (repairableComponent != null){
                            for (var x : allIngredients.entrySet()){
                                if (repairableComponent.isValidRepairItem(new ItemStack(x.getKey().getA(), x.getValue()))){
                                    int damagedPercentage = (int) Math.ceil((double) inputStack.getDamageValue() / inputStack.getMaxDamage() * x.getValue());
                                    while (outputStack.getStack(x.getKey()).getCount() > 0 && damagedPercentage != 0){
                                        if (outputStack.getStack(x.getKey()).is(x.getKey().getA())){
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
                            return new Tuple<>(outputs, false);
                        }
                    }
                    outputs.add(outputStack);
                }
            }

            if (r.value() instanceof ShapelessRecipe shapelessRecipe) {
                List<Ingredient> ingredients = new ArrayList<>(shapelessRecipe.ingredients);

                if (inputStack.has(DataComponents.FIREWORKS)){
                    Fireworks fireworks = inputStack.getOrDefault(DataComponents.FIREWORKS, new Fireworks(1, List.of()));
                    for(int i = 1;i < fireworks.flightDuration();i++){
                        ingredients.add(Ingredient.of(Items.GUNPOWDER));
                    }
                }
                List<List<Tuple<Item, DataComponentPatch>>> allIngredientCombinations = getAllShapelessIngredientCombinations(ingredients, inputStack);

                // Create a recipe for each combination
                for (List<Tuple<Item, DataComponentPatch>> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(shapelessRecipe.result.getItem().builtInRegistryHolder(), shapelessRecipe.result.getCount(), inputStack.getComponentsPatch()));
                    Map<Tuple<Item, DataComponentPatch>, Integer> allIngredients = new HashMap<>();

                    for (Tuple<Item, DataComponentPatch> item : ingredientCombination) {
                        if (item.getA() != Items.AIR) {
                            if (outputStack.contains(item)) {
                                ItemStack stack = outputStack.getStack(item);
                                stack.grow(1);
                                outputStack.setOutput(outputStack.indexOf(item), stack);
                            } else {
                                outputStack.addOutput(new ItemStack(item.getA().builtInRegistryHolder(), 1, item.getB()));
                            }
                            allIngredients.put(item, allIngredients.getOrDefault(item, 0) + 1);
                        }
                    }
                    if (inputStack.isDamaged()){
                        Repairable repairableComponent = inputStack.get(DataComponents.REPAIRABLE);
                        if (repairableComponent != null){
                            for (var x : allIngredients.entrySet()){
                                if (repairableComponent.isValidRepairItem(new ItemStack(x.getKey().getA(), x.getValue()))){
                                    int damagedPercentage = (int) Math.ceil((double) inputStack.getDamageValue() / inputStack.getMaxDamage() * x.getValue());
                                    while (outputStack.getStack(x.getKey()).getCount() > 0 && damagedPercentage != 0){
                                        if (outputStack.getStack(x.getKey()).is(x.getKey().getA())){
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
                            return new Tuple<>(outputs, false);
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

                List<List<Tuple<Item, DataComponentPatch>>> allIngredientCombinations = getAllIngredientCombinations(ingredients, inputStack);

                // Create a recipe for each combination
                for (List<Tuple<Item, DataComponentPatch>> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(smithingTransformRecipe.result.item().value().builtInRegistryHolder(), 1, inputStack.getComponentsPatch()));

                    for (Tuple<Item, DataComponentPatch> item : ingredientCombination) {
                        if (outputStack.contains(item)) {
                            ItemStack stack = outputStack.getStack(item);
                            if (item.getA().getDefaultInstance().isDamageableItem()){
                                stack.set(DataComponents.DAMAGE, inputStack.get(DataComponents.DAMAGE));
                            }
                            stack.grow(1);
                            outputStack.setOutput(outputStack.indexOf(item), stack);
                        } else {
                            ItemStack itemStack = new ItemStack(item.getA().builtInRegistryHolder(), 1, item.getB());
                            // If the item is damageable, set the damage to the input stack's damage
                            if (item.getA().getDefaultInstance().isDamageableItem()){
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
                smithingTrimRecipe.baseIngredient().getValues().stream().filter(itemHolder -> inputStack.is(itemHolder.value())).forEach(itemHolder -> ingredients.add(Optional.of(Ingredient.of(itemHolder.value()))));
                ingredients.add(smithingTrimRecipe.templateIngredient());
                if (additionIngredient.isPresent() && armorTrim != null){
                    additionIngredient.get().getValues().stream().filter(itemHolder -> {
                        ResourceKey<Item> itemResourceKey = itemHolder.getKey();
                        ResourceKey<TrimMaterial> armorTrimKey = armorTrim.material().getKey();
                        if (itemResourceKey != null && armorTrimKey != null){
                            return itemResourceKey.identifier().getPath().contains(armorTrimKey.identifier().getPath());
                        }
                        return false;
                    }).forEach(itemHolder -> ingredients.add(Optional.of(Ingredient.of(itemHolder.value()))));
                }

                List<List<Tuple<Item, DataComponentPatch>>> allIngredientCombinations = getAllIngredientCombinations(ingredients, inputStack);
                ItemEnchantments itemEnchantments = inputStack.get(DataComponents.ENCHANTMENTS);

                // Create a recipe for each combination
                for (List<Tuple<Item, DataComponentPatch>> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(inputStack.copyWithCount(1));

                    for (Tuple<Item, DataComponentPatch> item : ingredientCombination) {
                        if (outputStack.contains(item)) {
                            ItemStack stack = outputStack.getStack(item);
                            if (item.getA().getDefaultInstance().is(ingredients.getFirst().isPresent() ? ingredients.getFirst().get().getValues().get(0).value() : Items.AIR)){
                                stack.set(DataComponents.ENCHANTMENTS, itemEnchantments);
                                stack.set(DataComponents.DAMAGE, inputStack.get(DataComponents.DAMAGE));
                            }
                            stack.grow(1);
                            outputStack.setOutput(outputStack.indexOf(item), stack);
                        } else {
                            ItemStack itemStack = new ItemStack(item.getA().builtInRegistryHolder(), 1, item.getB());
                            if (item.getA().getDefaultInstance().is(ingredients.getFirst().isPresent() ? ingredients.getFirst().get().getValues().get(0).value() : Items.AIR)){
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

        return new Tuple<>(outputs, true);
    }

    public static List<Tuple<Item, DataComponentPatch>> getItemsFromIngredient(Ingredient ingredient, ItemStack inputStack) {
        List<Tuple<Item, DataComponentPatch>> items = new ArrayList<>();

        if (ingredient.getCustomIngredient() != null && !ingredient.getCustomIngredient().items().toList().isEmpty()) {
            if (ingredient.getCustomIngredient() instanceof DataComponentIngredient dataComponentIngredient){
                for (var holder : dataComponentIngredient.itemSet()) {
                    items.add(new Tuple<>(holder.value(), dataComponentIngredient.components().asPatch()));
                }
            }
            else{
                for (var holder : ingredient.getCustomIngredient().items().toList()) {
                    items.add(new Tuple<>(holder.value(), DataComponentPatch.EMPTY));
                }
            }
        }
        else {
            try {
                items = ingredient.getValues().stream()
                        .map(holder -> new Tuple<>(holder.value(), DataComponentPatch.EMPTY))
                        .distinct()
                        .toList();
            } catch (IllegalStateException e) {
                LogUtils.getLogger().warn("Skipping unsupported ingredient type: {}", ingredient);
                return Collections.emptyList();
            }
        }

        return items.stream()
                .filter(item -> filterIngredient(item, inputStack))
                .sorted(Comparator.comparing(tuple -> tuple.getA().getDescriptionId()))
                .toList();
    }

    private static boolean filterIngredient(Tuple<Item, DataComponentPatch> item, ItemStack inputStack){
        if (item.getA().getDescriptionId().contains("shulker_box") && inputStack.getItem().builtInRegistryHolder().key().identifier().getPath().contains("_shulker_box")){
            return item.getA() == Items.SHULKER_BOX;
        }
        if (item.getA().getDescriptionId().contains("bundle") && inputStack.getItem().builtInRegistryHolder().key().identifier().getPath().contains("_bundle")){
            return item.getA() == Items.BUNDLE;
        }
        if (item.getA().getDescriptionId().contains("wool") && inputStack.getItem().builtInRegistryHolder().key().identifier().getPath().contains("_wool")){
            return item.getA() == Items.WHITE_WOOL;
        }
        if (item.getA().getDescriptionId().contains("bed") && inputStack.getItem().builtInRegistryHolder().key().identifier().getPath().contains("_bed") && inputStack.getItem().builtInRegistryHolder().key().identifier().getNamespace().contains("minecraft")){
            return item.getA() == Items.WHITE_BED;
        }
        if (item.getA().getDescriptionId().contains("carpet") && inputStack.getItem().builtInRegistryHolder().key().identifier().getPath().contains("_carpet")){
            return item.getA() == Items.WHITE_CARPET;
        }
        if (item.getA().getDescriptionId().contains("harness") && inputStack.getItem().builtInRegistryHolder().key().identifier().getPath().contains("_harness")){
            return inputStack.getItem() == Items.WHITE_HARNESS ? item.getA() == Items.GRAY_HARNESS : item.getA() == Items.WHITE_HARNESS;
        }
        try{
            if (ModList.get().isLoaded("travelersbackpack")) {
                if (item.getA() instanceof com.tiviacz.travelersbackpack.items.SleepingBagItem) {
                    return inputStack.getItem() == com.tiviacz.travelersbackpack.init.ModItems.WHITE_SLEEPING_BAG.get() ?
                            item.getA() == com.tiviacz.travelersbackpack.init.ModItems.GRAY_SLEEPING_BAG.get() : item.getA() == com.tiviacz.travelersbackpack.init.ModItems.WHITE_SLEEPING_BAG.get();
                }
            }
        }
        catch (Exception ignored){

        }
        return item.getA().getCraftingRemainder(item.getA().getDefaultInstance()) == ItemStack.EMPTY || item.getA().getCraftingRemainder(item.getA().getDefaultInstance()).getItem() != item.getA().getDefaultInstance().getItem();
    }

    public static List<List<Tuple<Item, DataComponentPatch>>> getAllIngredientCombinations(List<Optional<Ingredient>> ingredients, ItemStack inputStack) {
        Map<String, UncraftingTableHelpers.Group> groupKeyToGroup = new HashMap<>();

        for (int i = 0; i < ingredients.size(); i++) {
            Optional<Ingredient> optIngredient = ingredients.get(i);
            List<Tuple<Item, DataComponentPatch>> items = optIngredient.map(ingredient -> {
                        List<Tuple<Item, DataComponentPatch>> ingredientItems = getItemsFromIngredient(ingredient, inputStack);
                        return ingredientItems.isEmpty() ? List.of(new Tuple<>(Items.AIR, DataComponentPatch.EMPTY)) : ingredientItems;
                    })
                    .orElse(List.of(new Tuple<>(Items.AIR, DataComponentPatch.EMPTY)));
            List<Tuple<Item, DataComponentPatch>> finalItems1 = items;
            items = items.stream().filter(item -> {
                boolean isVanillaInput = BuiltInRegistries.ITEM.getKey(inputStack.getItem()).getNamespace().equals("minecraft");

                if (isVanillaInput && UncraftEverythingConfig.CONFIG.preventModdedIngredientRecipes()) {
                    return BuiltInRegistries.ITEM.getKey(item.getA()).getNamespace().equals("minecraft");
                }
                else if(finalItems1.size() > 1){
                    Identifier ingredientRL = BuiltInRegistries.ITEM.getKey(item.getA());
                    return !UncraftEverythingConfig.CONFIG.getRestrictedModIngredients().contains(ingredientRL.getNamespace());
                }
                return true;
            }).toList();

            String key = items.stream()
                    .map(Tuple::getA)
                    .map(Item::getDescriptionId)
                    .sorted()
                    .collect(Collectors.joining(","));

            List<Tuple<Item, DataComponentPatch>> finalItems = items;
            UncraftingTableHelpers.Group group = groupKeyToGroup.computeIfAbsent(key, k -> new UncraftingTableHelpers.Group(new ArrayList<>(), finalItems));
            group.positions.add(i);
        }

        return getLists(groupKeyToGroup, ingredients.size());
    }

    public static List<List<Tuple<Item, DataComponentPatch>>> getAllShapelessIngredientCombinations(List<Ingredient> ingredients, ItemStack inputStack) {
        Map<String, UncraftingTableHelpers.Group> groupKeyToGroup = new HashMap<>();

        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ingredient = ingredients.get(i);
            List<Tuple<Item, DataComponentPatch>> items = getItemsFromIngredient(ingredient, inputStack);
            if (items.isEmpty()) items = List.of(new Tuple<>(Items.AIR, DataComponentPatch.EMPTY));

            List<Tuple<Item, DataComponentPatch>> finalItems1 = items;
            items = items.stream().filter(item -> {
                boolean isVanillaInput = BuiltInRegistries.ITEM.getKey(inputStack.getItem()).getNamespace().equals("minecraft");

                if (isVanillaInput && UncraftEverythingConfig.CONFIG.preventModdedIngredientRecipes()) {
                    return BuiltInRegistries.ITEM.getKey(item.getA()).getNamespace().equals("minecraft");
                }
                else if(finalItems1.size() > 1){
                    Identifier ingredientRL = BuiltInRegistries.ITEM.getKey(item.getA());
                    return !UncraftEverythingConfig.CONFIG.getRestrictedModIngredients().contains(ingredientRL.getNamespace());
                }
                return true;
            }).toList();

            String key = items.stream()
                    .map(Tuple::getA)
                    .map(Item::getDescriptionId)
                    .sorted()
                    .collect(Collectors.joining(","));

            List<Tuple<Item, DataComponentPatch>> finalItems = items;
            UncraftingTableHelpers.Group group = groupKeyToGroup.computeIfAbsent(key, k -> new UncraftingTableHelpers.Group(new ArrayList<>(), finalItems));
            group.positions.add(i);
        }

        return getLists(groupKeyToGroup, ingredients.size());
    }

    @NotNull
    public static List<List<Tuple<Item, DataComponentPatch>>> getLists(Map<String, UncraftingTableHelpers.Group> groupKeyToGroup, int size) {
        List<UncraftingTableHelpers.Group> groups = new ArrayList<>(groupKeyToGroup.values());
        List<List<Tuple<Item, DataComponentPatch>>> groupChoices = groups.stream()
                .map(group -> group.items)
                .collect(Collectors.toList());

        List<List<Tuple<Item, DataComponentPatch>>> product = UncraftingTableHelpers.cartesianProduct(groupChoices);

        List<List<Tuple<Item, DataComponentPatch>>> combinations = new ArrayList<>();

        for (List<Tuple<Item, DataComponentPatch>> choiceList : product) {
            NonNullList<Tuple<Item, DataComponentPatch>> itemsArray = UncraftingTableHelpers.getTuples(size, choiceList, groups);
            combinations.add(itemsArray);
        }

        return combinations;
    }

    public static @NotNull NonNullList<Tuple<Item, DataComponentPatch>> getTuples(int size, List<Tuple<Item, DataComponentPatch>> choiceList, List<Group> groups) {
        NonNullList<Tuple<Item, DataComponentPatch>> itemsArray = NonNullList.withSize(size, new Tuple<>(Items.AIR, DataComponentPatch.EMPTY));

        for (int groupIdx = 0; groupIdx < groups.size(); groupIdx++) {
            Group group = groups.get(groupIdx);
            Tuple<Item, DataComponentPatch> chosenItem = choiceList.get(groupIdx);
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

    public static class Group {
        public List<Integer> positions;
        public List<Tuple<Item, DataComponentPatch>> items;

        public Group(List<Integer> positions, List<Tuple<Item, DataComponentPatch>> items) {
            this.positions = positions;
            this.items = items;
        }
    }
}
