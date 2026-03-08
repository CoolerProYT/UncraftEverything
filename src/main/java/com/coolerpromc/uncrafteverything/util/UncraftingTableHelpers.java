package com.coolerpromc.uncrafteverything.util;

import com.coolerpromc.uncrafteverything.blockentity.custom.AbstractUncraftingTableBE;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.fml.ModList;
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
                || (!UncraftEverythingConfig.CONFIG.isEnchantedItemsAllowed(inputStack) && inputStack.getTag() != null && !inputStack.getTag().contains("Trim"))
                || (inputStack.getItem() == Items.SHULKER_BOX && inputStack.hasTag())
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

            if (!UncraftEverythingConfig.CONFIG.isEnchantedItemsAllowed(inputStack) && inputStack.getTag() != null && !inputStack.getTag().contains("Trim")){
                blockEntity.status = Status.ENCHANTED_ITEM;
            }

            if(inputStack.getItem() == Items.SHULKER_BOX && inputStack.hasTag()){
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

    public static <T extends AbstractUncraftingTableBE> List<Recipe<?>> findRecipe(ServerLevel serverLevel, ItemStack input, T blockEntity, @Nullable ServerPlayer player){
        ItemStack inputStack = input.copy();
        inputStack.resetHoverName();
        inputStack.removeTagKey("RepairCost");
        return serverLevel.getRecipeManager().getRecipes().stream().filter(recipe -> {
            if (!recipe.getId().getNamespace().equals("minecraft") && BuiltInRegistries.ITEM.getKey(inputStack.getItem()).getNamespace().equals("minecraft") && UncraftEverythingConfig.CONFIG.preventModdedIngredientRecipes()){
                return false;
            }

            if (recipe instanceof ShapedRecipe shapedRecipe){
                return validateRecipe(shapedRecipe.result, inputStack, blockEntity);
            }

            if (recipe instanceof ShapelessRecipe shapelessRecipe){
                return validateRecipe(shapelessRecipe.result, inputStack, blockEntity);
            }

            if(recipe instanceof ShulkerBoxColoring){
                return inputStack.is(UETags.Items.SHULKER_BOXES) && !inputStack.is(Items.SHULKER_BOX);
            }

            if (recipe instanceof SmithingTransformRecipe smithingTransformRecipe){
                if (!UncraftEverythingConfig.CONFIG.allowUnSmithing() || (!EnchantmentHelper.getEnchantments(inputStack).isEmpty() && UncraftEverythingConfig.CONFIG.outputEnchantedBook())){
                    return false;
                }
                return validateSmithingRecipe(smithingTransformRecipe, inputStack);
            }

            if (recipe instanceof SmithingTrimRecipe smithingTrimRecipe){
                if (!UncraftEverythingConfig.CONFIG.allowUnSmithing()){
                    return false;
                }
                Optional<ArmorTrim> armorTrim = ArmorTrim.getTrim(serverLevel.registryAccess(), inputStack);
                if (armorTrim.isPresent()){
                    Ingredient ingredient = smithingTrimRecipe.addition;
                    Optional<Holder.Reference<TrimPattern>> trimPatternReference = TrimPatterns.getFromTemplate(serverLevel.registryAccess(), smithingTrimRecipe.template.getItems()[0]);
                    if (ingredient != Ingredient.EMPTY && trimPatternReference.isPresent() && armorTrim.get().pattern().equals(trimPatternReference.get())){
                        return true;
                    }
                }
            }

            if (ModList.get().isLoaded("recipestages")) {
                try {
                    Class<?> shapedRecipeStageClass = Class.forName("com.blamejared.recipestages.recipes.ShapedRecipeStage");
                    Class<?> recipeStageClass = Class.forName("com.blamejared.recipestages.recipes.RecipeStage");
                    Class<?> gameStageHelperClass = Class.forName("net.darkhax.gamestages.GameStageHelper");

                    if (player != null){
                        if (shapedRecipeStageClass.isInstance(recipe)) {
                            String stage = (String) shapedRecipeStageClass.getMethod("getStage").invoke(recipe);
                            Boolean hasStage = (Boolean) gameStageHelperClass.getMethod("hasAnyOf", Player.class, Collection.class).invoke(null, player, Collections.singleton(stage));

                            if (hasStage) {
                                Object getRecipe = shapedRecipeStageClass.getMethod("getRecipe").invoke(recipe);

                                if (getRecipe instanceof ShapedRecipe shapedRecipe) {
                                    return validateRecipe(shapedRecipe.result, inputStack, blockEntity);
                                }
                            }
                        }

                        if (recipeStageClass.isInstance(recipe)) {
                            String stage = (String) recipeStageClass.getMethod("getStage").invoke(recipe);

                            Boolean hasStage = (Boolean) gameStageHelperClass.getMethod("hasAnyOf", Player.class, Collection.class).invoke(null, player, Collections.singleton(stage));

                            if (hasStage) {
                                Object getRecipe = recipeStageClass.getMethod("getRecipe").invoke(recipe);

                                if (getRecipe instanceof ShapelessRecipe shapelessRecipe) {
                                    return validateRecipe(shapelessRecipe.result, inputStack, blockEntity);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("[UncraftEverything] Failed to get recipe from RecipeStages: " + e.getMessage());
                }
            }

            if (blockEntity.status == Status.BLANK && !inputStack.isEmpty()){
                blockEntity.status = Status.NO_RECIPE_FOUND;
            }
            return false;
        }).map(recipe -> {
            if (ModList.get().isLoaded("recipestages")) {
                try {
                    Class<?> shapedRecipeStageClass = Class.forName("com.blamejared.recipestages.recipes.ShapedRecipeStage");
                    Class<?> recipeStageClass = Class.forName("com.blamejared.recipestages.recipes.RecipeStage");

                    if (shapedRecipeStageClass.isInstance(recipe)) {
                        Object innerRecipe = shapedRecipeStageClass.getMethod("getRecipe").invoke(recipe);
                        if (innerRecipe instanceof ShapedRecipe shapedRecipe) {
                            return shapedRecipe;
                        }
                    }

                    if (recipeStageClass.isInstance(recipe)) {
                        Object innerRecipe = recipeStageClass.getMethod("getRecipe").invoke(recipe);
                        if (innerRecipe instanceof ShapelessRecipe shapelessRecipe) {
                            return shapelessRecipe;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("[UncraftEverything] Failed to get recipe from RecipeStages: " + e.getMessage());
                }
            }

            return recipe instanceof Recipe<?> ? recipe : null;
        }).toList();
    }

    public static <T extends AbstractUncraftingTableBE> boolean validateRecipe(ItemStack result, ItemStack inputStack, T blockEntity){
        if (result.getItem() == inputStack.getItem() && inputStack.getCount() < result.getCount()){
            blockEntity.status = Status.NOT_ENOUGH_INPUT_ITEM;
        }
        if (!EnchantmentHelper.getEnchantments(inputStack).isEmpty() && UncraftEverythingConfig.CONFIG.outputEnchantedBook()){
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
        if (!EnchantmentHelper.getEnchantments(inputStack).isEmpty() && UncraftEverythingConfig.CONFIG.allowEnchantedItems.get() && result.getItem() == inputStack.getItem()){
            return true;
        }
        return (canStack(result, inputStack) && inputStack.getCount() >= result.getCount()) || (inputStack.is(Items.FIREWORK_ROCKET) && result.is(Items.FIREWORK_ROCKET));
    }

    public static boolean validateSmithingRecipe(SmithingTransformRecipe smithingTransformRecipe, ItemStack inputStack){
        if (!EnchantmentHelper.getEnchantments(inputStack).isEmpty() && UncraftEverythingConfig.CONFIG.outputEnchantedBook()){
            return false;
        }
        if (inputStack.isDamaged()){
            return inputStack.is(smithingTransformRecipe.result.getItem()) && inputStack.getCount() >= smithingTransformRecipe.result.getCount();
        }
        if (!EnchantmentHelper.getEnchantments(inputStack).isEmpty() && UncraftEverythingConfig.CONFIG.allowEnchantedItems.get() && smithingTransformRecipe.result.getItem() == inputStack.getItem()){
            return true;
        }
        return canStack(inputStack, smithingTransformRecipe.result);
    }

    public static boolean canStack(ItemStack stack1, ItemStack stack2) {
        if (!ItemStack.isSameItem(stack1, stack2)) return false;

        CompoundTag tag1 = stack1.getTag();
        CompoundTag tag2 = stack2.getTag();

        boolean empty1 = tag1 == null || tag1.isEmpty();
        boolean empty2 = tag2 == null || tag2.isEmpty();

        if (empty1 && empty2) return true;
        return Objects.equals(tag1, tag2);
    }

    public static <T extends AbstractUncraftingTableBE> Tuple<List<UncraftingTableRecipe>, Boolean> getOutputs(ItemStack inputStack, List<Recipe<?>> recipes, ServerLevel serverLevel){
        List<UncraftingTableRecipe> outputs = new ArrayList<>();

        if (inputStack.is(Items.TIPPED_ARROW)){
            Potion potion = PotionUtils.getPotion(inputStack);
            UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(inputStack.getItem(), 8, inputStack.getTag()));
            ItemStack lingeringPotion = new ItemStack(Items.LINGERING_POTION);
            PotionUtils.setPotion(lingeringPotion, potion);

            ItemStack output = new ItemStack(Items.ARROW, 8);
            outputStack.addOutput(output);
            outputStack.addOutput(lingeringPotion);
            outputs.add(outputStack);
        }

        if (!EnchantmentHelper.getEnchantments(inputStack).isEmpty() && recipes.isEmpty() && !inputStack.getItem().equals(Items.ENCHANTED_BOOK) && UncraftEverythingConfig.CONFIG.outputEnchantedBook()){
            UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(inputStack.getItem(), 1, inputStack.getTag()));
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(inputStack);
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantmentHelper.setEnchantments(enchantments, book);
            ItemStack output = new ItemStack(inputStack.getItem(), 1);
            output.setDamageValue(inputStack.getDamageValue());

            outputStack.addOutput(output);
            outputStack.addOutput(book);

            outputs.add(outputStack);
        }

        for (Recipe<?> r : recipes) {
            if (r instanceof ShulkerBoxColoring){
                List<Ingredient> ingredients = new ArrayList<>();

                Ingredient shulkerBoxIngredient = Ingredient.of(UETags.Items.SHULKER_BOXES);
                ingredients.add(shulkerBoxIngredient);

                Ingredient dyeIngredient = Ingredient.of(DyeItem.byColor(Objects.requireNonNull(((ShulkerBoxBlock) ((BlockItem) inputStack.getItem()).getBlock()).getColor())));
                ingredients.add(dyeIngredient);

                List<List<Tuple<Item, CompoundTag>>> allIngredientCombinations = getAllShapelessIngredientCombinations(ingredients, inputStack);
                CompoundTag itemContainerContents = inputStack.getTag();

                for (List<Tuple<Item, CompoundTag>> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(inputStack.copyWithCount(1));

                    for (Tuple<Item, CompoundTag> item : ingredientCombination) {
                        if (outputStack.contains(item)) {
                            outputStack.getStack(item).grow(1);
                        } else {
                            ItemStack itemStack = new ItemStack(item.getA(), 1, item.getB());
                            if (itemStack.is(UETags.Items.SHULKER_BOXES)){
                                itemStack.setTag(itemContainerContents);
                            }
                            outputStack.addOutput(itemStack);
                        }
                    }
                    outputs.add(outputStack);
                }
            }

            if (r instanceof ShapedRecipe shapedRecipe) {
                // Get all possible combinations of ingredients
                List<List<Tuple<Item, CompoundTag>>> allIngredientCombinations = getAllIngredientCombinations(shapedRecipe.getIngredients().stream().map(Optional::of).toList(), inputStack);

                // Create a recipe for each combination
                for (List<Tuple<Item, CompoundTag>> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(shapedRecipe.result.getItem(), shapedRecipe.result.getCount(), inputStack.getTag()));
                    Map<Tuple<Item, CompoundTag>, Integer> allIngredients = new HashMap<>();

                    for (Tuple<Item, CompoundTag> item : ingredientCombination) {
                        if (outputStack.contains(item)) {
                            ItemStack stack = outputStack.getStack(item);
                            stack.grow(1);
                            outputStack.setOutput(outputStack.indexOf(item), stack);
                        } else {
                            ItemStack stack = new ItemStack(item.getA(), 1, item.getB());
                            outputStack.addOutput(stack);
                        }
                        allIngredients.put(item, allIngredients.getOrDefault(item, 0) + 1);
                    }
                    if (inputStack.isDamaged()){
                        for (var x : allIngredients.entrySet()){
                            if (inputStack.getItem().isValidRepairItem(inputStack, new ItemStack(x.getKey().getA(), x.getValue()))){
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
                    outputs.add(outputStack);
                }
            }

            if (r instanceof ShapelessRecipe shapelessRecipe) {
                List<Ingredient> ingredients = new ArrayList<>(shapelessRecipe.ingredients);

                if (inputStack.hasTag() && inputStack.getTagElement("Fireworks") != null){
                    CompoundTag compoundTag = inputStack.getTagElement("Fireworks");
                    if (compoundTag != null){
                        byte fireworks = compoundTag.getByte("Flight");
                        for(int i = 1;i < fireworks;i++){
                            ingredients.add(Ingredient.of(Items.GUNPOWDER));
                        }
                    }
                }
                List<List<Tuple<Item, CompoundTag>>> allIngredientCombinations = getAllShapelessIngredientCombinations(ingredients, inputStack);

                // Create a recipe for each combination
                for (List<Tuple<Item, CompoundTag>> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(shapelessRecipe.result.getItem(), shapelessRecipe.result.getCount(), inputStack.getTag()));
                    Map<Tuple<Item, CompoundTag>, Integer> allIngredients = new HashMap<>();

                    for (Tuple<Item, CompoundTag> item : ingredientCombination) {
                        if (item.getA() != Items.AIR) {
                            if (outputStack.contains(item)) {
                                ItemStack stack = outputStack.getStack(item);
                                stack.grow(1);
                                outputStack.setOutput(outputStack.indexOf(item), stack);
                            } else {
                                ItemStack stack = new ItemStack(item.getA(), 1, item.getB());
                                outputStack.addOutput(stack);
                            }
                            allIngredients.put(item, allIngredients.getOrDefault(item, 0) + 1);
                        }
                    }
                    if (inputStack.isDamaged()){
                        for (var x : allIngredients.entrySet()){
                            if (inputStack.getItem().isValidRepairItem(inputStack, new ItemStack(x.getKey().getA(), x.getValue()))){
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
                    outputs.add(outputStack);
                }
            }

            if (r instanceof SmithingTransformRecipe smithingTransformRecipe){
                List<Optional<Ingredient>> ingredients = new ArrayList<>();

                ingredients.add(Optional.of(smithingTransformRecipe.base));
                ingredients.add(Optional.of(smithingTransformRecipe.addition));
                ingredients.add(Optional.of(smithingTransformRecipe.template));

                List<List<Tuple<Item, CompoundTag>>> allIngredientCombinations = getAllIngredientCombinations(ingredients, inputStack);

                // Create a recipe for each combination
                for (List<Tuple<Item, CompoundTag>> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(new ItemStack(smithingTransformRecipe.result.getItem(), 1, inputStack.getTag()));

                    for (Tuple<Item, CompoundTag> item : ingredientCombination) {
                        if (outputStack.contains(item)) {
                            ItemStack stack = outputStack.getStack(item);
                            if (item.getA().getDefaultInstance().isDamageableItem()){
                                stack.setDamageValue(inputStack.getDamageValue());
                            }
                            stack.grow(1);
                            outputStack.setOutput(outputStack.indexOf(item), stack);
                        } else {
                            ItemStack itemStack = new ItemStack(item.getA(), 1, item.getB());
                            // If the item is damageable, set the damage to the input stack's damage
                            if (item.getA().getDefaultInstance().isDamageableItem()){
                                itemStack.setDamageValue(inputStack.getDamageValue());
                                if (itemStack.getDamageValue() >= itemStack.getMaxDamage()){
                                    itemStack = ItemStack.EMPTY;
                                }
                            }
                            outputStack.addOutput(itemStack);
                        }
                    }
                    outputs.add(outputStack);
                }
            }

            if (r instanceof SmithingTrimRecipe smithingTrimRecipe){
                Optional<ArmorTrim> armorTrim = ArmorTrim.getTrim(serverLevel.registryAccess(), inputStack);
                Optional<Ingredient> additionIngredient = Optional.of(smithingTrimRecipe.addition);

                List<Optional<Ingredient>> ingredients = new ArrayList<>();
                Arrays.stream(smithingTrimRecipe.base.getItems()).filter(itemHolder -> inputStack.is(itemHolder.getItem())).forEach(itemHolder -> ingredients.add(Optional.of(Ingredient.of(itemHolder.getItem()))));
                ingredients.add(Optional.of(smithingTrimRecipe.template));
                if (armorTrim.isPresent()){
                    Arrays.stream(additionIngredient.get().getItems()).filter(itemHolder -> {
                        Optional<ResourceKey<Item>> itemResourceKey = itemHolder.getItemHolder().unwrapKey();
                        Optional<ResourceKey<TrimMaterial>> armorTrimKey = armorTrim.get().material().unwrapKey();
                        if (itemResourceKey.isPresent() && armorTrimKey.isPresent()){
                            return itemResourceKey.get().location().getPath().contains(armorTrimKey.get().location().getPath());
                        }
                        return false;
                    }).forEach(itemHolder -> ingredients.add(Optional.of(Ingredient.of(itemHolder.getItem()))));
                }

                List<List<Tuple<Item, CompoundTag>>> allIngredientCombinations = getAllIngredientCombinations(ingredients, inputStack);
                Map<Enchantment, Integer> itemEnchantments = EnchantmentHelper.getEnchantments(inputStack);

                // Create a recipe for each combination
                for (List<Tuple<Item, CompoundTag>> ingredientCombination : allIngredientCombinations) {
                    UncraftingTableRecipe outputStack = new UncraftingTableRecipe(inputStack.copyWithCount(1));

                    for (Tuple<Item, CompoundTag> item : ingredientCombination) {
                        if (outputStack.contains(item)) {
                            ItemStack stack = outputStack.getStack(item);
                            if (item.getA().getDefaultInstance().is(ingredients.get(0).isPresent() ? ingredients.get(0).get().getItems()[0].getItem() : Items.AIR)){
                                EnchantmentHelper.setEnchantments(itemEnchantments, stack);
                                stack.setDamageValue(inputStack.getDamageValue());
                            }
                            stack.grow(1);
                            outputStack.setOutput(outputStack.indexOf(item), stack);
                        } else {
                            ItemStack itemStack = new ItemStack(item.getA(), 1, item.getB());
                            if (item.getA().getDefaultInstance().is(ingredients.get(0).isPresent() ? ingredients.get(0).get().getItems()[0].getItem() : Items.AIR)){
                                EnchantmentHelper.setEnchantments(itemEnchantments, itemStack);
                                itemStack.setDamageValue(inputStack.getDamageValue());
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

    public static List<Tuple<Item, CompoundTag>> getItemsFromIngredient(Ingredient ingredient, ItemStack inputStack) {
        List<Tuple<Item, CompoundTag>> items = new ArrayList<>();

        if (!ingredient.isSimple()) {
            if (ingredient instanceof PartialNBTIngredient partialNBTIngredient){
                for (var holder : partialNBTIngredient.getItems()) {
                    items.add(new Tuple<>(holder.getItem(), holder.getTag()));
                }
            }
            else if (ingredient instanceof StrictNBTIngredient strictNBTIngredient){
                for (var holder : strictNBTIngredient.getItems()) {
                    items.add(new Tuple<>(holder.getItem(), holder.getTag()));
                }
            }
            else{
                for (var holder : ingredient.getItems()) {
                    items.add(new Tuple<>(holder.getItem(), holder.getTag()));
                }
            }
        }
        else {
            try {
                items = Arrays.stream(ingredient.getItems())
                        .map(holder -> new Tuple<>(holder.getItem(), holder.getTag()))
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

    private static boolean filterIngredient(Tuple<Item, CompoundTag> item, ItemStack inputStack){
        if (item.getA().getDescriptionId().contains("shulker_box") && inputStack.getItem().builtInRegistryHolder().key().location().getPath().contains("_shulker_box")){
            return item.getA() == Items.SHULKER_BOX;
        }
        if (item.getA().getDescriptionId().contains("bundle") && inputStack.getItem().builtInRegistryHolder().key().location().getPath().contains("_bundle")){
            return item.getA() == Items.BUNDLE;
        }
        if (item.getA().getDescriptionId().contains("wool") && inputStack.getItem().builtInRegistryHolder().key().location().getPath().contains("_wool")){
            return item.getA() == Items.WHITE_WOOL;
        }
        if (item.getA().getDescriptionId().contains("bed") && inputStack.getItem().builtInRegistryHolder().key().location().getPath().contains("_bed") && inputStack.getItem().builtInRegistryHolder().key().location().getNamespace().contains("minecraft")){
            return item.getA() == Items.WHITE_BED;
        }
        if (item.getA().getDescriptionId().contains("carpet") && inputStack.getItem().builtInRegistryHolder().key().location().getPath().contains("_carpet")){
            return item.getA() == Items.WHITE_CARPET;
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
        return item.getA().getCraftingRemainingItem(item.getA().getDefaultInstance()) == ItemStack.EMPTY || item.getA().getCraftingRemainingItem(item.getA().getDefaultInstance()).getItem() != item.getA().getDefaultInstance().getItem();
    }

    public static List<List<Tuple<Item, CompoundTag>>> getAllIngredientCombinations(List<Optional<Ingredient>> ingredients, ItemStack inputStack) {
        Map<String, UncraftingTableHelpers.Group> groupKeyToGroup = new HashMap<>();

        for (int i = 0; i < ingredients.size(); i++) {
            Optional<Ingredient> optIngredient = ingredients.get(i);
            List<Tuple<Item, CompoundTag>> items = optIngredient.map(ingredient -> {
                        List<Tuple<Item, CompoundTag>> ingredientItems = getItemsFromIngredient(ingredient, inputStack);
                        return ingredientItems.isEmpty() ? List.of(new Tuple<>(Items.AIR, new CompoundTag())) : ingredientItems;
                    })
                    .orElse(List.of(new Tuple<>(Items.AIR, new CompoundTag())));
            List<Tuple<Item, CompoundTag>> finalItems1 = items;
            items = items.stream().filter(item -> {
                boolean isVanillaInput = BuiltInRegistries.ITEM.getKey(inputStack.getItem()).getNamespace().equals("minecraft");

                if (isVanillaInput && UncraftEverythingConfig.CONFIG.preventModdedIngredientRecipes()) {
                    return BuiltInRegistries.ITEM.getKey(item.getA()).getNamespace().equals("minecraft");
                }
                else if(finalItems1.size() > 1){
                    ResourceLocation ingredientRL = BuiltInRegistries.ITEM.getKey(item.getA());
                    return !UncraftEverythingConfig.CONFIG.getRestrictedModIngredients().contains(ingredientRL.getNamespace());
                }
                return true;
            }).toList();

            String key = items.stream()
                    .map(Tuple::getA)
                    .map(Item::getDescriptionId)
                    .sorted()
                    .collect(Collectors.joining(","));

            List<Tuple<Item, CompoundTag>> finalItems = items;
            UncraftingTableHelpers.Group group = groupKeyToGroup.computeIfAbsent(key, k -> new UncraftingTableHelpers.Group(new ArrayList<>(), finalItems));
            group.positions.add(i);
        }

        return getLists(groupKeyToGroup, ingredients.size());
    }

    public static List<List<Tuple<Item, CompoundTag>>> getAllShapelessIngredientCombinations(List<Ingredient> ingredients, ItemStack inputStack) {
        Map<String, UncraftingTableHelpers.Group> groupKeyToGroup = new HashMap<>();

        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ingredient = ingredients.get(i);
            List<Tuple<Item, CompoundTag>> items = getItemsFromIngredient(ingredient, inputStack);
            if (items.isEmpty()) items = List.of(new Tuple<>(Items.AIR, new CompoundTag()));

            List<Tuple<Item, CompoundTag>> finalItems1 = items;
            items = items.stream().filter(item -> {
                boolean isVanillaInput = BuiltInRegistries.ITEM.getKey(inputStack.getItem()).getNamespace().equals("minecraft");

                if (isVanillaInput && UncraftEverythingConfig.CONFIG.preventModdedIngredientRecipes()) {
                    return BuiltInRegistries.ITEM.getKey(item.getA()).getNamespace().equals("minecraft");
                }
                else if(finalItems1.size() > 1){
                    ResourceLocation ingredientRL = BuiltInRegistries.ITEM.getKey(item.getA());
                    return !UncraftEverythingConfig.CONFIG.getRestrictedModIngredients().contains(ingredientRL.getNamespace());
                }
                return true;
            }).toList();

            String key = items.stream()
                    .map(Tuple::getA)
                    .map(Item::getDescriptionId)
                    .sorted()
                    .collect(Collectors.joining(","));

            List<Tuple<Item, CompoundTag>> finalItems = items;
            UncraftingTableHelpers.Group group = groupKeyToGroup.computeIfAbsent(key, k -> new UncraftingTableHelpers.Group(new ArrayList<>(), finalItems));
            group.positions.add(i);
        }

        return getLists(groupKeyToGroup, ingredients.size());
    }

    @NotNull
    public static List<List<Tuple<Item, CompoundTag>>> getLists(Map<String, UncraftingTableHelpers.Group> groupKeyToGroup, int size) {
        List<UncraftingTableHelpers.Group> groups = new ArrayList<>(groupKeyToGroup.values());
        List<List<Tuple<Item, CompoundTag>>> groupChoices = groups.stream()
                .map(group -> group.items)
                .collect(Collectors.toList());

        List<List<Tuple<Item, CompoundTag>>> product = UncraftingTableHelpers.cartesianProduct(groupChoices);

        List<List<Tuple<Item, CompoundTag>>> combinations = new ArrayList<>();

        for (List<Tuple<Item, CompoundTag>> choiceList : product) {
            NonNullList<Tuple<Item, CompoundTag>> itemsArray = UncraftingTableHelpers.getTuples(size, choiceList, groups);
            combinations.add(itemsArray);
        }

        return combinations;
    }

    public static @NotNull NonNullList<Tuple<Item, CompoundTag>> getTuples(int size, List<Tuple<Item, CompoundTag>> choiceList, List<Group> groups) {
        NonNullList<Tuple<Item, CompoundTag>> itemsArray = NonNullList.withSize(size, new Tuple<>(Items.AIR, new CompoundTag()));

        for (int groupIdx = 0; groupIdx < groups.size(); groupIdx++) {
            Group group = groups.get(groupIdx);
            Tuple<Item, CompoundTag> chosenItem = choiceList.get(groupIdx);
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

        List<T> firstList = lists.get(0);
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

        if (recipe instanceof ShapedRecipe shaped) {
            ingredients = shaped.getIngredients()
                    .stream()
                    .map(Optional::of)
                    .toList();
        } else if (recipe instanceof ShapelessRecipe shapeless) {
            ingredients = shapeless.ingredients
                    .stream()
                    .map(Optional::of)
                    .toList();
        } else if (recipe instanceof SmithingTransformRecipe smithingTransformRecipe) {
            ingredients = List.of(
                    Optional.of(smithingTransformRecipe.base),
                    Optional.of(smithingTransformRecipe.addition),
                    Optional.of(smithingTransformRecipe.template)
            );
        } else {
            return true;
        }

        for (Optional<Ingredient> ingredient : ingredients) {
            if (ingredient.isPresent()){
                if (!ingredient.get().isSimple() && !(ingredient.get().getItems().length == 0)) {
                    if (!Arrays.stream(ingredient.get().getItems()).map(ItemStack::getItem).map(BuiltInRegistries.ITEM::getKey).map(ResourceLocation::getNamespace).toList().contains("minecraft")) {
                        return false;
                    }
                }
                else{
                    if (!Arrays.stream(ingredient.get().getItems()).map(ItemStack::getItem).map(BuiltInRegistries.ITEM::getKey).map(ResourceLocation::getNamespace).toList().contains("minecraft")) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static class Group {
        public List<Integer> positions;
        public List<Tuple<Item, CompoundTag>> items;

        public Group(List<Integer> positions, List<Tuple<Item, CompoundTag>> items) {
            this.positions = positions;
            this.items = items;
        }
    }
}
