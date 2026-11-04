package com.coolerpromc.uncrafteverything.util;

import com.mojang.serialization.Lifecycle;
import net.minecraft.SharedConstants;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TrimRecipeViewerRecipesTest {
    private static RegistryAccess access;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        BuiltInRegistries.ITEM.bindTags(Map.of(ItemTags.TRIMMABLE_ARMOR, List.of(
                BuiltInRegistries.ITEM.wrapAsHolder(Items.DIAMOND_CHESTPLATE),
                BuiltInRegistries.ITEM.wrapAsHolder(Items.IRON_CHESTPLATE))));
        var materials = new MappedRegistry<TrimMaterial>(Registries.TRIM_MATERIAL, Lifecycle.stable());
        for (Item item : List.of(Items.IRON_INGOT, Items.GOLD_INGOT)) {
            String name = BuiltInRegistries.ITEM.getKey(item).getPath();
            Registry.register(materials, new ResourceLocation(name),
                    TrimMaterial.create(name, item, 0.1F, Component.literal(name), Map.of()));
        }
        var patterns = new MappedRegistry<TrimPattern>(Registries.TRIM_PATTERN, Lifecycle.stable());
        for (Item item : List.of(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE)) {
            var id = BuiltInRegistries.ITEM.getKey(item);
            Registry.register(patterns, id, new TrimPattern(id,
                    BuiltInRegistries.ITEM.wrapAsHolder(item), Component.literal(id.toString())));
        }
        access = new RegistryAccess.ImmutableRegistryAccess(List.of(materials.freeze(), patterns.freeze(), BuiltInRegistries.ITEM));
    }

    private static SmithingTrimRecipe recipe(Ingredient base, Ingredient addition, Ingredient template) {
        return new SmithingTrimRecipe(new ResourceLocation("test", "trim"), template, base, addition);
    }

    @Test
    void broadIngredientsOnlyProduceValidTrimCombinationsWithoutMutatingSharedStacks() {
        Ingredient base = Ingredient.of(Items.DIAMOND_CHESTPLATE, Items.IRON_CHESTPLATE, Items.STICK);
        // Non-materials/templates must not multiply the number of entries.
        Ingredient broad = Ingredient.of(BuiltInRegistries.ITEM.stream().map(ItemStack::new));
        var originals = java.util.Arrays.stream(base.getItems()).map(ItemStack::copy).toList();
        var entries = TrimRecipeViewerRecipes.getRecipes(access, recipe(base, broad, broad));
        assertEquals(8, entries.size()); // 2 armor items x 2 materials x 2 patterns
        for (int i = 0; i < originals.size(); i++) {
            assertTrue(ItemStack.matches(originals.get(i), base.getItems()[i]));
        }
        for (var entry : entries) {
            var trim = ArmorTrim.getTrim(access, entry.getInput()).orElseThrow();
            assertTrue(entry.getOutputs().get(1).test(new ItemStack(trim.material().value().ingredient())));
            assertTrue(entry.getOutputs().get(2).test(new ItemStack(trim.pattern().value().templateItem())));
        }
        assertEquals(8, entries.stream().map(entry -> entry.getInput().toString() + entry.getInput().getTag()).distinct().count());
        entries.get(0).getInput().getOrCreateTag().putBoolean("test", true);
        assertFalse(entries.get(1).getInput().getOrCreateTag().contains("test"));
    }

    @Test
    void emptyOrUnrelatedIngredientsProduceNoPreviews() {
        Ingredient armor = Ingredient.of(Items.DIAMOND_CHESTPLATE);
        Ingredient material = Ingredient.of(Items.IRON_INGOT);
        Ingredient template = Ingredient.of(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE);
        assertTrue(TrimRecipeViewerRecipes.getRecipes(access, recipe(armor, Ingredient.EMPTY, template)).isEmpty());
        assertTrue(TrimRecipeViewerRecipes.getRecipes(access, recipe(armor, material, Ingredient.EMPTY)).isEmpty());
        assertTrue(TrimRecipeViewerRecipes.getRecipes(access, recipe(Ingredient.EMPTY, material, template)).isEmpty());
        assertTrue(TrimRecipeViewerRecipes.getRecipes(access, recipe(armor, Ingredient.of(Items.STICK), template)).isEmpty());
    }
}
