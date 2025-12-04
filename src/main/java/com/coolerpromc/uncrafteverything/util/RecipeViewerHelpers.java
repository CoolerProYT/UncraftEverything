package com.coolerpromc.uncrafteverything.util;

import com.coolerpromc.uncrafteverything.UncraftEverythingClient;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.ComponentsIngredient;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.*;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.item.equipment.trim.ArmorTrimMaterials;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RecipeViewerHelpers {
    public static List<JEIUncraftingTableRecipe> getRecipes(DynamicRegistryManager registryAccess, boolean isREI){
        ClientPlayNetworking.send(new RequestConfigPayload());
        List<JEIUncraftingTableRecipe> entries = new ArrayList<>();

        // Add Shulker Boxes (Prevent duplication, only normal shulker box will be outputted)
        Ingredient shulkerBoxIngredient = Ingredient.ofTag(Registries.ITEM.getOrThrow(ItemTags.SHULKER_BOXES));
        shulkerBoxIngredient.getMatchingItems().forEach(itemStack -> {
            if (!itemStack.value().equals(Items.SHULKER_BOX) && !(isItemBlacklisted(itemStack.value().getDefaultStack()) || isItemWhitelisted(itemStack.value().getDefaultStack()))){
                entries.add(new JEIUncraftingTableRecipe(itemStack.value().getDefaultStack(), List.of(Ingredient.ofItem(Blocks.SHULKER_BOX), Ingredient.ofItem(DyeItem.byColor(Objects.requireNonNull(((ShulkerBoxBlock) ((BlockItem) itemStack.value()).getBlock()).getColor()))))));
            }
        });

        // Add Tipped Arrows
        if (!(isItemBlacklisted(Items.TIPPED_ARROW.getDefaultStack()) || isItemWhitelisted(Items.TIPPED_ARROW.getDefaultStack()))){
            registryAccess.getOptional(RegistryKeys.POTION).stream()
                    .flatMap(Registry::streamEntries)
                    .forEach(potion -> {
                        if (potion != Potions.WATER) {
                            ItemStack tippedArrow = PotionContentsComponent.createStack(Items.TIPPED_ARROW, potion);
                            List<ItemStack> output = new ArrayList<>();
                            output.add(Items.ARROW.getDefaultStack());
                            output.add(Items.ARROW.getDefaultStack());
                            output.add(Items.ARROW.getDefaultStack());
                            output.add(Items.ARROW.getDefaultStack());
                            output.add(PotionContentsComponent.createStack(Items.LINGERING_POTION, potion));
                            output.add(Items.ARROW.getDefaultStack());
                            output.add(Items.ARROW.getDefaultStack());
                            output.add(Items.ARROW.getDefaultStack());
                            output.add(Items.ARROW.getDefaultStack());

                            if (isREI){
                                entries.add(new JEIUncraftingTableRecipe(tippedArrow, output, true));
                            }
                            else{
                                entries.add(new JEIUncraftingTableRecipe(tippedArrow, output.stream().map(itemStack -> {
                                    if (itemStack.getComponents().isEmpty()){
                                        return Ingredient.ofItem(itemStack.getItem());
                                    }
                                    else{
                                        return new ComponentsIngredient(Ingredient.ofItem(itemStack.getItem()), itemStack.getComponentChanges()).toVanilla();
                                    }
                                }).toList()));
                            }
                        }
                    });
        }

        // Add Enchanted Books
        if (UncraftEverythingClient.payloadFromServer.allowEnchantedItem()){
            registryAccess.getOptional(RegistryKeys.ENCHANTMENT)
                    .stream()
                    .flatMap(Registry::streamEntries)
                    .forEach(holder -> {
                        if (!holder.hasKeyAndValue()) return;
                        Enchantment enchantment = holder.value();

                        ItemStack diamondSword = new ItemStack(Items.DIAMOND_SWORD);
                        diamondSword.addEnchantment(holder, enchantment.getMaxLevel());

                        List<ItemStack> output = new ArrayList<>();
                        output.add(Items.DIAMOND_SWORD.getDefaultStack());
                        output.add(EnchantmentHelper.getEnchantedBookWith(new EnchantmentLevelEntry(holder, enchantment.getMaxLevel())));

                        if (isREI){
                            entries.add(new JEIUncraftingTableRecipe(diamondSword, output, true));
                        }
                        else{
                            entries.add(new JEIUncraftingTableRecipe(diamondSword, output.stream().map(itemStack -> {
                                if (itemStack.getComponents().isEmpty()){
                                    return Ingredient.ofItem(itemStack.getItem());
                                }
                                else{
                                    return new ComponentsIngredient(Ingredient.ofItem(itemStack.getItem()), itemStack.getComponentChanges()).toVanilla();
                                }
                            }).toList()));
                        }
                    });

        }

        // Add all items that can be uncrafted
        UncraftEverythingClient.recipesFromServer.forEach(recipeHolder -> {
            if (recipeHolder.value() instanceof ShapedRecipe shapedRecipe){
                if (!(isItemBlacklisted(shapedRecipe.result) || isItemWhitelisted(shapedRecipe.result))){
                    entries.add(new JEIUncraftingTableRecipe(shapedRecipe.result, shapedRecipe.getIngredients().stream().map(ingredient -> ingredient.orElse(null)).toList()));
                }
            }

            if (recipeHolder.value() instanceof ShapelessRecipe shapelessRecipe){
                if (!(isItemBlacklisted(shapelessRecipe.result) || isItemWhitelisted(shapelessRecipe.result))){
                    entries.add(new JEIUncraftingTableRecipe(shapelessRecipe.result, shapelessRecipe.ingredients));
                }
            }

            if (recipeHolder.value() instanceof SmithingTransformRecipe smithingTransformRecipe && UncraftEverythingClient.payloadFromServer.allowUnsmithing()){
                DefaultedList<Ingredient> ingredients = DefaultedList.of();

                ingredients.add(smithingTransformRecipe.base());
                smithingTransformRecipe.addition().ifPresent(ingredients::add);
                smithingTransformRecipe.template().ifPresent(ingredients::add);
                entries.add(new JEIUncraftingTableRecipe(new ItemStack(smithingTransformRecipe.result.itemEntry(), 1, smithingTransformRecipe.result.components()), ingredients));
            }

            if (recipeHolder.value() instanceof SmithingTrimRecipe smithingTrimRecipe && UncraftEverythingClient.payloadFromServer.allowUnsmithing()){
                List<Ingredient> output = new ArrayList<>();
                output.add(0, smithingTrimRecipe.base());
                output.add(1, smithingTrimRecipe.addition().get());
                output.add(smithingTrimRecipe.template().get());
                smithingTrimRecipe.base().getMatchingItems().forEach(itemHolder -> {
                    output.set(0, Ingredient.ofItem(itemHolder.value().asItem()));
                    smithingTrimRecipe.addition().get().getMatchingItems().forEach(itemHolder1 -> {
                        output.set(1, Ingredient.ofItem(itemHolder1.value()));
                        Optional<RegistryEntry<ArmorTrimMaterial>> trimMaterialReference = ArmorTrimMaterials.get(registryAccess, itemHolder1.value().getDefaultStack());
                        if (trimMaterialReference.isPresent() && itemHolder.getKey().get().getValue().getPath().contains("diamond")){
                            ItemStack stack = itemHolder.value().asItem().getDefaultStack();
                            stack.set(DataComponentTypes.TRIM, new ArmorTrim(trimMaterialReference.get(), smithingTrimRecipe.pattern));
                            entries.add(new JEIUncraftingTableRecipe(stack, output));
                        }
                    });
                });
            }
        });

        return entries;
    }

    public static boolean isItemBlacklisted(ItemStack itemStack) {
        if (UncraftEverythingClient.payloadFromServer.restrictionType() != UncraftEverythingConfig.RestrictionType.BLACKLIST){
            return false;
        }

        Identifier itemLocation = UncraftEverythingConfig.inputStackLocation(itemStack);
        String itemLocationString = itemLocation.toString();

        if (UncraftEverythingClient.payloadFromServer.restrictedItems().contains(itemLocationString)) {
            return true;
        }

        for (String entry : UncraftEverythingClient.payloadFromServer.restrictedItems()) {
            if (entry.startsWith("#")){
                String tagName = entry.substring(1);
                Optional<TagKey<Item>> tagKey = UncraftEverythingConfig.tryParseTagKey(tagName);
                if (tagKey.isPresent() && itemStack.isIn(tagKey.get())) {
                    return true;
                }
            }

            if (entry.contains("*")) {
                String regex = entry.replace("*", ".*");
                if (itemLocationString.matches(regex)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isItemWhitelisted(ItemStack itemStack) {
        if (UncraftEverythingClient.payloadFromServer.restrictionType() != UncraftEverythingConfig.RestrictionType.WHITELIST){
            return false;
        }

        Identifier itemLocation = UncraftEverythingConfig.inputStackLocation(itemStack);
        String itemLocationString = itemLocation.toString();

        if (UncraftEverythingClient.payloadFromServer.restrictedItems().contains(itemLocationString)) {
            return false;
        }

        for (String entry : UncraftEverythingClient.payloadFromServer.restrictedItems()) {
            if (entry.startsWith("#")){
                String tagName = entry.substring(1);
                Optional<TagKey<Item>> tagKey = UncraftEverythingConfig.tryParseTagKey(tagName);
                if (tagKey.isPresent() && itemStack.isIn(tagKey.get())) {
                    return false;
                }
            }

            if (entry.contains("*")) {
                String regex = entry.replace("*", ".*");
                if (itemLocationString.matches(regex)) {
                    return false;
                }
            }
        }

        return true;
    }
}