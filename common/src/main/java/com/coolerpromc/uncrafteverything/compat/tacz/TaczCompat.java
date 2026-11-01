package com.coolerpromc.uncrafteverything.compat.tacz;

import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.util.UncraftingTableHelpers;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import com.coolerpromc.uncrafteverything.platform.Services;

import java.lang.reflect.Method;
import java.util.*;

/** Optional bridge: no TaCZ classes appear in descriptors, so TaCZ is not required at runtime. */
public final class TaczCompat {
    private static boolean warned;

    private TaczCompat() {}

    public static boolean isTaczItem(ItemStack stack) {
        return Services.PLATFORM.isModLoaded("tacz") && !stack.isEmpty()
                && "tacz".equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace());
    }

    public static boolean isGun(ItemStack stack) {
        return isTaczItem(stack) && modelId(stack, "GunId") != null;
    }

    private static ResourceLocation modelId(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(key, Tag.TAG_STRING)
                ? ResourceLocation.tryParse(tag.getString(key)) : null;
    }

    // Models share item IDs. Match the model, while ignoring names and mutable item state.
    static boolean matches(ItemStack result, ItemStack input) {
        if (result.isEmpty() || !ItemStack.isSameItem(result, input) || input.getCount() < result.getCount()) return false;
        boolean hasModel = false;
        for (String key : List.of("GunId", "AmmoId", "AttachmentId")) {
            if ((result.getTag() != null && result.getTag().contains(key))
                    || (input.getTag() != null && input.getTag().contains(key))) {
                ResourceLocation id = modelId(result, key);
                if (id == null || !id.equals(modelId(input, key))) return false;
                hasModel = true;
            }
        }
        return hasModel || NbtUtils.compareNbt(result.getTag(), input.getTag(), true);
    }

    public static List<Recipe<?>> findRecipes(ServerLevel level, ItemStack input) {
        return findRecipes(level, input, false);
    }

    /** Include identity matches with insufficient input only when diagnosing a missing batch. */
    public static List<Recipe<?>> findRecipes(ServerLevel level, ItemStack input, boolean includeIncompleteBatches) {
        if (!isTaczItem(input)) return List.of();
        try {
            Map<ResourceLocation, Recipe<?>> candidates = new LinkedHashMap<>();
            for (Recipe<?> recipe : level.getRecipeManager().getRecipes()) {
                if (Api.RECIPE.isInstance(recipe)) candidates.put(recipe.getId(), recipe);
            }
            // Before TaCZ moved recipes into RecipeManager, gun packs used this separate registry.
            if (Api.LEGACY_RECIPES != null) {
                for (Object value : ((Map<?, ?>) Api.LEGACY_RECIPES.invoke(null)).values()) {
                    if (value instanceof Recipe<?> recipe && Api.RECIPE.isInstance(recipe)) {
                        candidates.putIfAbsent(recipe.getId(), recipe);
                    }
                }
            }
            List<Recipe<?>> recipes = new ArrayList<>();
            for (Recipe<?> recipe : candidates.values()) {
                ItemStack result = (ItemStack) Api.OUTPUT.invoke(recipe);
                int divisor = batchDivisor(recipe, result, input);
                int required = includeIncompleteBatches ? 1 : result.getCount() / divisor;
                if (matches(result.copyWithCount(required), input)) recipes.add(recipe);
            }
            return recipes;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
            warn(e);
            return List.of();
        }
    }

    public static boolean isRecipe(Recipe<?> recipe) {
        return Services.PLATFORM.isModLoaded("tacz")
                && recipe.getClass().getName().equals("com.tacz.guns.crafting.GunSmithTableRecipe");
    }

    public static List<UncraftingTableRecipe> getOutputs(Recipe<?> recipe, ItemStack input) {
        try {
            ItemStack result = (ItemStack) Api.OUTPUT.invoke(recipe);
            int divisor = batchDivisor(recipe, result, input);
            result = result.copyWithCount(result.getCount() / divisor);
            if (!matches(result, input)) return List.of();
            List<Material> materials = new ArrayList<>();
            for (Object entry : (List<?>) Api.INPUTS.invoke(recipe)) {
                Ingredient ingredient = (Ingredient) Api.INGREDIENT.invoke(entry);
                int count = (int) Api.COUNT.invoke(entry) / divisor;
                if (count <= 0 || ingredient.isEmpty()) return List.of();
                List<ItemStack> alternatives = UncraftingTableHelpers.getItemsFromIngredient(ingredient, input).stream()
                        .filter(item -> ingredient.getItems().length <= 1 || !UncraftEverythingConfig.CONFIG
                                .getRestrictedModIngredients().contains(BuiltInRegistries.ITEM.getKey(item.getA()).getNamespace()))
                        .map(item -> com.coolerpromc.uncrafteverything.platform.ItemStacks.create(item.getA(), 1, item.getB())).toList();
                if (alternatives.isEmpty()) return List.of();
                materials.add(new Material(alternatives, count));
            }
            if (materials.isEmpty()) return List.of();
            return expand(input.copyWithCount(result.getCount()), materials, isGun(input) ? contents(input) : List.of());
        } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
            warn(e);
            return List.of();
        }
    }

    // Some ammo recipes make 100 rounds, which cannot fit a 64-item input slot.
    // Reduce only oversized batches, and only by a divisor shared by every material.
    private static int batchDivisor(Recipe<?> recipe, ItemStack result, ItemStack input) throws ReflectiveOperationException {
        if (modelId(result, "AmmoId") == null || result.getCount() <= Math.min(64, input.getMaxStackSize())) return 1;
        int divisor = result.getCount();
        for (Object entry : (List<?>) Api.INPUTS.invoke(recipe)) {
            divisor = gcd(divisor, (int) Api.COUNT.invoke(entry));
        }
        return Math.max(1, divisor);
    }

    private static int gcd(int a, int b) {
        while (b != 0) {
            int remainder = a % b;
            a = b;
            b = remainder;
        }
        return a;
    }

    record Material(List<ItemStack> alternatives, int count) {}

    /** Expand alternatives once per counted material, rather than once per individual ingot. */
    static List<UncraftingTableRecipe> expand(ItemStack input, List<Material> materials, List<ItemStack> contents) {
        List<List<ItemStack>> combinations = new ArrayList<>();
        combinations.add(new ArrayList<>());
        for (Material material : materials) {
            List<List<ItemStack>> next = new ArrayList<>();
            for (List<ItemStack> combination : combinations) {
                for (ItemStack alternative : material.alternatives()) {
                    List<ItemStack> stacks = new ArrayList<>();
                    combination.forEach(stack -> stacks.add(stack.copy()));
                    if (append(stacks, alternative, material.count())) next.add(stacks);
                }
            }
            combinations = next;
        }
        List<UncraftingTableRecipe> recipes = new ArrayList<>();
        for (List<ItemStack> stacks : combinations) {
            boolean fits = true;
            for (ItemStack content : contents) {
                long count = (long) content.getCount() * input.getCount();
                if (count > Integer.MAX_VALUE || !append(stacks, content, (int) count)) {
                    fits = false;
                    break;
                }
            }
            if (fits && !stacks.isEmpty()) recipes.add(new UncraftingTableRecipe(input.copy(), stacks));
        }
        return recipes;
    }

    // Both tables and their recipe selection UI have nine output slots. Never truncate a recipe.
    private static boolean append(List<ItemStack> outputs, ItemStack item, int count) {
        if (item.isEmpty() || count <= 0) return false;
        int limit = item.getMaxStackSize();
        if (limit <= 0) return false;
        for (ItemStack stack : outputs) {
            if (ItemStack.isSameItemSameTags(stack, item)) {
                int added = Math.min(count, Math.max(0, limit - stack.getCount()));
                stack.grow(added);
                count -= added;
            }
        }
        while (count > 0) {
            if (outputs.size() == 9) return false;
            int added = Math.min(count, limit);
            outputs.add(item.copyWithCount(added));
            count -= added;
        }
        return true;
    }

    /** Return installed attachments; loaded ammunition is not refunded. */
    static List<ItemStack> contents(ItemStack input) {
        List<ItemStack> contents = new ArrayList<>();
        CompoundTag tag = input.getTag();
        if (tag == null) return contents;
        for (String key : tag.getAllKeys()) {
            if (key.startsWith("Attachment") && tag.contains(key, Tag.TAG_COMPOUND)) {
                ItemStack attachment = ItemStack.of(tag.getCompound(key));
                if (!attachment.isEmpty()) contents.add(attachment);
            }
        }
        return contents;
    }

    private static void warn(Throwable e) {
        if (!warned) {
            warned = true;
            LogUtils.getLogger().warn("Unable to read TaCZ uncrafting recipes; skipping incompatible recipes", e);
        }
    }

    private static final class Api {
        static final Class<?> RECIPE;
        static final Method OUTPUT, INPUTS, INGREDIENT, COUNT, LEGACY_RECIPES;

        static {
            try {
                RECIPE = Class.forName("com.tacz.guns.crafting.GunSmithTableRecipe");
                OUTPUT = RECIPE.getMethod("getOutput");
                INPUTS = RECIPE.getMethod("getInputs");
                Class<?> material = Class.forName("com.tacz.guns.crafting.GunSmithTableIngredient");
                INGREDIENT = material.getMethod("getIngredient");
                COUNT = material.getMethod("getCount");
                Method legacy;
                try {
                    legacy = Class.forName("com.tacz.guns.api.TimelessAPI").getMethod("getAllRecipes");
                } catch (NoSuchMethodException e) {
                    legacy = null;
                }
                LEGACY_RECIPES = legacy;
            } catch (ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }
}
