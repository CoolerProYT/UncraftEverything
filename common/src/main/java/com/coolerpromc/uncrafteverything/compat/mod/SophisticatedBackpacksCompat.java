package com.coolerpromc.uncrafteverything.compat.mod;

import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.blockentity.custom.AbstractUncraftingTableBE;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.util.Status;
import com.coolerpromc.uncrafteverything.util.UncraftingTableHelpers;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.*;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class SophisticatedBackpacksCompat {
    private static final String BACKPACK_ITEM = "net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem";
    private static final String UPGRADE_ITEM = "net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeItem";
    private static final Set<String> WRAPPED_RECIPES = Set.of(
            "net.p3pp3rf1y.sophisticatedbackpacks.crafting.BasicBackpackRecipe",
            "net.p3pp3rf1y.sophisticatedbackpacks.crafting.BackpackUpgradeRecipe",
            "net.p3pp3rf1y.sophisticatedcore.crafting.UpgradeNextTierRecipe");
    private static final String SMITHING_BACKPACK_UPGRADE_RECIPE = "net.p3pp3rf1y.sophisticatedbackpacks.crafting.SmithingBackpackUpgradeRecipe";
    private static boolean loggedFailure;

    public static boolean isSupportedItem(ItemStack inputStack){
        return BuiltInRegistries.ITEM.getKey(inputStack.getItem()).getNamespace().equals("sophisticatedbackpacks")
                || hasType(inputStack.getItem().getClass(), BACKPACK_ITEM)
                || hasType(inputStack.getItem().getClass(), UPGRADE_ITEM);
    }

    private static boolean hasType(Class<?> type, String name){
        if (type == null){
            return false;
        }
        if (type.getName().equals(name)){
            return true;
        }
        for (Class<?> implemented : type.getInterfaces()){
            if (hasType(implemented, name)){
                return true;
            }
        }
        return hasType(type.getSuperclass(), name);
    }

    public record RecipeInfo(ItemStack result, List<Optional<Ingredient>> ingredients, boolean smithing){
    }

    public static Recipe<?> unwrapRecipe(Recipe<?> recipe){
        if (!WRAPPED_RECIPES.contains(recipe.getClass().getName())){
            return recipe;
        }
        try {
            return (Recipe<?>) invokeMethod(recipe, "getCompose");
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e){
            logFailure(e);
            return recipe;
        }
    }

    public static Optional<RecipeInfo> getRecipeInfo(Recipe<?> recipe){
        Recipe<?> wrappedRecipe = unwrapRecipe(recipe);
        if (wrappedRecipe instanceof ShapedRecipe shapedRecipe){
            return Optional.of(new RecipeInfo(shapedRecipe.result.create(), shapedRecipe.getIngredients(), false));
        }
        if (wrappedRecipe instanceof ShapelessRecipe shapelessRecipe){
            return Optional.of(new RecipeInfo(shapelessRecipe.result.create(), shapelessRecipe.ingredients.stream().map(Optional::of).toList(), false));
        }
        if (wrappedRecipe instanceof SmithingTransformRecipe smithingRecipe){
            return Optional.of(new RecipeInfo(smithingRecipe.result.create(),
                    List.of(Optional.of(smithingRecipe.baseIngredient()), smithingRecipe.additionIngredient(), smithingRecipe.templateIngredient()), true));
        }
        if (recipe.getClass().getName().equals(SMITHING_BACKPACK_UPGRADE_RECIPE)){
            try {
                return Optional.of(new RecipeInfo((ItemStack) invokeMethod(recipe, "result"),
                        List.of(Optional.of((Ingredient) invokeMethod(recipe, "baseIngredient")),
                                getOptionalIngredient(recipe, "additionIngredient"), getOptionalIngredient(recipe, "templateIngredient")), true));
            } catch (ReflectiveOperationException | RuntimeException | LinkageError e){
                logFailure(e);
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private static Optional<Ingredient> getOptionalIngredient(Object recipe, String name) throws ReflectiveOperationException {
        return (Optional<Ingredient>) invokeMethod(recipe, name);
    }

    public static boolean hasRecipe(Recipe<?> recipe, ItemStack inputStack, AbstractUncraftingTableBE blockEntity){
        Optional<RecipeInfo> recipeInfo = getRecipeInfo(recipe);
        if (recipeInfo.isEmpty() || !inputStack.is(recipeInfo.get().result().getItem())){
            return false;
        }
        if (recipeInfo.get().smithing() && !UncraftEverythingConfig.CONFIG.allowUnSmithing()){
            return false;
        }
        if (UncraftEverythingConfig.CONFIG.restrictAmbiguouslyCraftedItems()){
            for (Optional<Ingredient> ingredient : recipeInfo.get().ingredients()){
                if (ingredient.isPresent() && UncraftingTableHelpers.getItemsFromIngredient(ingredient.get(), inputStack).size() > 1){
                    setStatus(blockEntity, Status.RESTRICTED_BY_CONFIG);
                    return false;
                }
            }
        }
        ItemStack recipeInput = inputStack.copy();
        recipeInput.remove(DataComponents.CUSTOM_NAME);
        // Keep runtime data on the lower-tier item.
        for (DataComponentType<?> type : inputStack.getComponents().keySet()){
            Identifier id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
            if (id != null && isStateNamespace(id.getNamespace())){
                // Explicit recipe result components can identify different recipes for one item.
                if ((recipeInfo.get().result().getComponentsPatch().split().added().has(type)
                        || recipeInfo.get().result().getComponentsPatch().split().removed().contains(type))
                        && !Objects.equals(inputStack.get(type), recipeInfo.get().result().get(type))){
                    return false;
                }
                resetComponent(recipeInput, recipeInfo.get().result(), type);
            }
        }
        resetComponent(recipeInput, recipeInfo.get().result(), DataComponents.CONTAINER);
        return UncraftingTableHelpers.validateRecipe(recipeInfo.get().result(), recipeInput, blockEntity);
    }

    public static List<UncraftingTableRecipe> getOutputs(Recipe<?> recipe, ItemStack inputStack, AbstractUncraftingTableBE blockEntity){
        if (!hasRecipe(recipe, inputStack, blockEntity)){
            return List.of();
        }
        RecipeInfo recipeInfo = getRecipeInfo(recipe).orElseThrow();
        List<UncraftingTableRecipe> results = new ArrayList<>();
        for (List<Pair<Item, DataComponentPatch>> combination : UncraftingTableHelpers.getIngredientCombinations(recipeInfo.ingredients(), inputStack)){
            try {
                List<ItemStack> outputs = new ArrayList<>();
                int lowerTierIndex = -1;
                boolean backpack = hasType(inputStack.getItem().getClass(), BACKPACK_ITEM);
                for (Pair<Item, DataComponentPatch> ingredient : combination){
                    if (ingredient.getLeft() == Items.AIR){
                        continue;
                    }
                    ItemStack output = new ItemStack(ingredient.getLeft().builtInRegistryHolder(), 1, ingredient.getRight());
                    boolean isLowerTier = hasType(output.getItem().getClass(), backpack ? BACKPACK_ITEM : UPGRADE_ITEM);
                    if (isLowerTier){
                        // Never clone a storage UUID or upgrade payload onto two ingredients.
                        if (lowerTierIndex != -1 || recipeInfo.result().getCount() != 1 || output.is(inputStack.getItem())){
                            throw new UnsafeContentsException();
                        }
                        lowerTierIndex = outputs.size();
                    }
                    outputs.add(output);
                }
                if (backpack){
                    if (hasComponent(inputStack, "sophisticatedcore", "linked_storage_endpoint")
                            || hasComponent(inputStack, "sophisticatedbackpacks", "loot_table")){
                        throw new UnsafeContentsException();
                    }
                    ItemStack lowerTierStack = lowerTierIndex < 0 ? ItemStack.EMPTY : outputs.get(lowerTierIndex);
                    checkBackpackCapacity(inputStack, lowerTierStack);
                    if (!lowerTierStack.isEmpty()){
                        ItemStack outputStack = copyComponents(inputStack, lowerTierStack);
                        setIntegerComponent(outputStack, "number_of_inventory_slots", getIntegerValue(lowerTierStack.getItem(), "getNumberOfSlots"));
                        setIntegerComponent(outputStack, "number_of_upgrade_slots", getIntegerValue(lowerTierStack.getItem(), "getNumberOfUpgradeSlots"));
                        outputs.set(lowerTierIndex, outputStack);
                    }
                }
                else if (lowerTierIndex >= 0){
                    outputs.set(lowerTierIndex, copyComponents(inputStack, outputs.get(lowerTierIndex)));
                }
                else if (hasStoredState(inputStack, recipeInfo.result())){
                    // A base ingredient cannot expose the inventory/fluid/energy of a functional upgrade.
                    throw new UnsafeContentsException();
                }
                UncraftingTableRecipe output = new UncraftingTableRecipe(inputStack.copyWithCount(recipeInfo.result().getCount()));
                for (ItemStack stack : outputs){
                    addOutput(output, stack);
                }
                results.add(output);
            } catch (UnsafeContentsException e){
                setStatus(blockEntity, Status.CONTENTS_CANNOT_BE_PRESERVED);
            } catch (ReflectiveOperationException | RuntimeException | LinkageError e){
                // Unknown optional-mod APIs must never turn a filled backpack into an empty one.
                logFailure(e);
                setStatus(blockEntity, Status.CONTENTS_CANNOT_BE_PRESERVED);
            }
        }
        return results;
    }

    private static ItemStack copyComponents(ItemStack inputStack, ItemStack target){
        ItemStack outputStack = new ItemStack(target.getItem(), 1);
        outputStack.applyComponents(inputStack.getComponentsPatch());
        outputStack.applyComponents(target.getComponentsPatch());
        return outputStack;
    }

    private static void checkBackpackCapacity(ItemStack inputStack, ItemStack target) throws ReflectiveOperationException, UnsafeContentsException {
        if (inputStack.getCount() > 1 && hasComponent(inputStack, "sophisticatedcore", "storage_uuid")){
            throw new UnsafeContentsException();
        }
        if (inputStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyItemCopyStream().findAny().isPresent()
                && target.isEmpty()){
            throw new UnsafeContentsException();
        }
        if (!hasComponent(inputStack, "sophisticatedcore", "storage_uuid")){
            return;
        }
        Class<?> wrapperClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper");
        // Read contents without changing the input's cached wrapper.
        Object wrapper = getUncachedWrapper(wrapperClass, inputStack);
        int slots = target.isEmpty() ? 0 : getIntegerValue(target.getItem(), "getNumberOfSlots");
        int upgradeSlots = target.isEmpty() ? 0 : getIntegerValue(target.getItem(), "getNumberOfUpgradeSlots");
        int columnsTaken = getIntegerValue(wrapper, "getColumnsTaken");
        int rows = (slots + (slots <= 81 ? 8 : 11)) / (slots <= 81 ? 9 : 12);
        int availableSlots = Math.max(0, slots - rows * columnsTaken);
        if (hasOccupiedSlots(invokeMethod(wrapper, "getInventoryHandler"), availableSlots)
                || hasOccupiedSlots(invokeMethod(wrapper, "getUpgradeHandler"), upgradeSlots)){
            throw new UnsafeContentsException();
        }
    }

    private static Object getUncachedWrapper(Class<?> wrapperClass, ItemStack inputStack) throws ReflectiveOperationException {
        try {
            return wrapperClass.getMethod("fromStackNoUuidCache", ItemStack.class).invoke(null, inputStack.copy());
        } catch (NoSuchMethodException e){
            // The Fabric port predates UUID caching and exposes a public constructor instead.
            return wrapperClass.getConstructor(ItemStack.class).newInstance(inputStack.copy());
        }
    }

    public static boolean canStillUncraft(ItemStack inputStack, UncraftingTableRecipe recipe, AbstractUncraftingTableBE blockEntity){
        if (!isSupportedItem(inputStack)){
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(inputStack, recipe.getInput())){
            return false;
        }
        if (!hasType(inputStack.getItem().getClass(), BACKPACK_ITEM)){
            return true;
        }
        try {
            List<ItemStack> backpacks = recipe.getOutputs().stream()
                    .filter(stack -> hasType(stack.getItem().getClass(), BACKPACK_ITEM)).toList();
            if (backpacks.size() > 1){
                throw new UnsafeContentsException();
            }
            ItemStack target = backpacks.isEmpty() ? ItemStack.EMPTY : backpacks.getFirst();
            // Extracting enchantments returns the same backpack, including its complete state.
            if (!target.isEmpty() && target.is(inputStack.getItem())){
                return true;
            }
            checkBackpackCapacity(inputStack, target);
            return true;
        } catch (UnsafeContentsException e){
            setStatus(blockEntity, Status.CONTENTS_CANNOT_BE_PRESERVED);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e){
            logFailure(e);
            setStatus(blockEntity, Status.CONTENTS_CANNOT_BE_PRESERVED);
        }
        return false;
    }

    public static boolean canFitOutputs(List<ItemStack> slots, List<ItemStack> outputs){
        List<ItemStack> simulated = slots.stream().map(ItemStack::copy).collect(Collectors.toCollection(ArrayList::new));
        for (ItemStack output : outputs){
            boolean inserted = false;
            for (int slot = 0; slot < simulated.size(); slot++){
                ItemStack existing = simulated.get(slot);
                if (existing.isEmpty()){
                    simulated.set(slot, output.copy());
                    inserted = true;
                    break;
                }
                if (ItemStack.isSameItemSameComponents(existing, output) && existing.getCount() + output.getCount() <= existing.getMaxStackSize()){
                    existing.grow(output.getCount());
                    inserted = true;
                    break;
                }
            }
            if (!inserted){
                return false;
            }
        }
        return true;
    }

    public static void onUncrafted(List<ItemStack> outputs){
        // Refresh cached handlers after consuming the input.
        for (ItemStack output : outputs){
            if (!hasType(output.getItem().getClass(), BACKPACK_ITEM) || !hasComponent(output, "sophisticatedcore", "storage_uuid")){
                continue;
            }
            try {
                Class<?> wrapperClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper");
                Object wrapper = wrapperClass.getMethod("fromStack", ItemStack.class).invoke(null, output);
                invokeMethod(wrapper, "onContentsUpdated");
            } catch (ReflectiveOperationException | RuntimeException | LinkageError e){
                logFailure(e);
            }
        }
    }

    private static boolean hasOccupiedSlots(Object handler, int capacity) throws ReflectiveOperationException {
        Method size;
        try {
            size = handler.getClass().getMethod("size"); // NeoForge 26.x transfer API
        } catch (NoSuchMethodException e){
            size = handler.getClass().getMethod("getSlots"); // Fabric port / older item handler API
        }
        int slots = ((Number) size.invoke(handler)).intValue();
        Method getStack = handler.getClass().getMethod("getStackInSlot", int.class);
        for (int slot = capacity; slot < slots; slot++){
            if (!((ItemStack) getStack.invoke(handler, slot)).isEmpty()){
                return true;
            }
        }
        return false;
    }

    private static boolean hasStoredState(ItemStack inputStack, ItemStack template){
        for (DataComponentType<?> type : inputStack.getComponents().keySet()){
            Identifier id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
            if (id != null && (isStateNamespace(id.getNamespace()) || type == DataComponents.CONTAINER)
                    && !Objects.equals(inputStack.get(type), template.get(type))){
                return true;
            }
        }
        return false;
    }

    private static boolean isStateNamespace(String namespace){
        return namespace.equals("sophisticatedcore") || namespace.equals("sophisticatedbackpacks");
    }

    private static boolean hasComponent(ItemStack stack, String namespace, String path){
        return getComponent(namespace, path).map(stack::has).orElse(false);
    }

    private static Optional<DataComponentType<?>> getComponent(String namespace, String path){
        return BuiltInRegistries.DATA_COMPONENT_TYPE.getOptional(Identifier.fromNamespaceAndPath(namespace, path));
    }

    @SuppressWarnings("unchecked")
    private static void setIntegerComponent(ItemStack stack, String path, int value) throws UnsafeContentsException {
        DataComponentType<?> type = getComponent("sophisticatedcore", path).orElseThrow(UnsafeContentsException::new);
        stack.set((DataComponentType<Integer>) type, value);
    }

    private static <T> void resetComponent(ItemStack stack, ItemStack template, DataComponentType<T> type){
        stack.set(type, template.get(type));
    }

    private static Object invokeMethod(Object instance, String methodName) throws ReflectiveOperationException {
        return instance.getClass().getMethod(methodName).invoke(instance);
    }

    private static int getIntegerValue(Object instance, String methodName) throws ReflectiveOperationException {
        return ((Number) invokeMethod(instance, methodName)).intValue();
    }

    private static void addOutput(UncraftingTableRecipe recipe, ItemStack stack){
        for (ItemStack existing : recipe.getOutputs()){
            if (ItemStack.isSameItemSameComponents(existing, stack) && existing.getCount() + stack.getCount() <= existing.getMaxStackSize()){
                existing.grow(stack.getCount());
                return;
            }
        }
        recipe.addOutput(stack);
    }

    private static void setStatus(AbstractUncraftingTableBE blockEntity, Status status){
        if (blockEntity != null){
            blockEntity.status = status;
        }
    }

    private static void logFailure(Throwable failure){
        if (!loggedFailure){
            loggedFailure = true;
            Constants.LOGGER.warn("Could not safely reverse a Sophisticated Backpacks recipe; leaving the input untouched", failure);
        }
    }

    private static class UnsafeContentsException extends Exception {
    }
}
