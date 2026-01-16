package com.coolerpromc.uncrafteverything.util;

import com.coolerpromc.uncrafteverything.UncraftEverythingClient;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.ComponentsIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RecipeViewerHelpers {
    public static List<JEIUncraftingTableRecipe> getRecipes(RegistryAccess registryAccess, boolean isREI){
        ClientPlayNetworking.send(new RequestConfigPayload());
        List<JEIUncraftingTableRecipe> entries = new ArrayList<>();

        // Add Shulker Boxes (Prevent duplication, only normal shulker box will be outputted)
        Ingredient shulkerBoxIngredient = Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ItemTags.SHULKER_BOXES));
        shulkerBoxIngredient.items().forEach(itemStack -> {
            if (!itemStack.value().equals(Items.SHULKER_BOX) && !(isItemBlacklisted(itemStack.value().getDefaultInstance()) || isItemWhitelisted(itemStack.value().getDefaultInstance()))){
                entries.add(new JEIUncraftingTableRecipe(itemStack.value().getDefaultInstance(), List.of(Ingredient.of(Blocks.SHULKER_BOX), Ingredient.of(DyeItem.byColor(Objects.requireNonNull(((ShulkerBoxBlock) ((BlockItem) itemStack.value()).getBlock()).getColor()))))));
            }
        });

        // Add Tipped Arrows
        if (!(isItemBlacklisted(Items.TIPPED_ARROW.getDefaultInstance()) || isItemWhitelisted(Items.TIPPED_ARROW.getDefaultInstance()))){
            registryAccess.lookup(Registries.POTION).stream()
                    .flatMap(Registry::listElements)
                    .forEach(potion -> {
                        if (potion != Potions.WATER) {
                            ItemStack tippedArrow = PotionContents.createItemStack(Items.TIPPED_ARROW, potion);
                            List<ItemStack> output = new ArrayList<>();
                            output.add(Items.ARROW.getDefaultInstance());
                            output.add(Items.ARROW.getDefaultInstance());
                            output.add(Items.ARROW.getDefaultInstance());
                            output.add(Items.ARROW.getDefaultInstance());
                            output.add(PotionContents.createItemStack(Items.LINGERING_POTION, potion));
                            output.add(Items.ARROW.getDefaultInstance());
                            output.add(Items.ARROW.getDefaultInstance());
                            output.add(Items.ARROW.getDefaultInstance());
                            output.add(Items.ARROW.getDefaultInstance());

                            if (isREI){
                                entries.add(new JEIUncraftingTableRecipe(tippedArrow, output, true));
                            }
                            else{
                                entries.add(new JEIUncraftingTableRecipe(tippedArrow, output.stream().map(itemStack -> {
                                    if (itemStack.getComponents().isEmpty()){
                                        return Ingredient.of(itemStack.getItem());
                                    }
                                    else{
                                        return new ComponentsIngredient(Ingredient.of(itemStack.getItem()), itemStack.getComponentsPatch()).toVanilla();
                                    }
                                }).toList()));
                            }
                        }
                    });
        }

        // Add Enchanted Books
        if (UncraftEverythingClient.payloadFromServer.allowEnchantedItem()){
            registryAccess.lookup(Registries.ENCHANTMENT)
                    .stream()
                    .flatMap(Registry::listElements)
                    .forEach(holder -> {
                        if (!holder.isBound()) return;
                        Enchantment enchantment = holder.value();

                        ItemStack diamondSword = new ItemStack(Items.DIAMOND_SWORD);
                        diamondSword.enchant(holder, enchantment.getMaxLevel());

                        List<ItemStack> output = new ArrayList<>();
                        output.add(Items.DIAMOND_SWORD.getDefaultInstance());
                        output.add(EnchantmentHelper.createBook(new EnchantmentInstance(holder, enchantment.getMaxLevel())));

                        if (isREI){
                            entries.add(new JEIUncraftingTableRecipe(diamondSword, output, true));
                        }
                        else{
                            entries.add(new JEIUncraftingTableRecipe(diamondSword, output.stream().map(itemStack -> {
                                if (itemStack.getComponents().isEmpty()){
                                    return Ingredient.of(itemStack.getItem());
                                }
                                else{
                                    return new ComponentsIngredient(Ingredient.of(itemStack.getItem()), itemStack.getComponentsPatch()).toVanilla();
                                }
                            }).toList()));
                        }
                    });

        }

        // Add all items that can be uncrafted
        UncraftEverythingClient.recipesFromServer.forEach(recipeHolder -> {
            if (recipeHolder.value() instanceof ShapedRecipe shapedRecipe){
                if (!(isItemBlacklisted(shapedRecipe.result.create()) || isItemWhitelisted(shapedRecipe.result.create()))){
                    entries.add(new JEIUncraftingTableRecipe(shapedRecipe.result.create(), shapedRecipe.getIngredients().stream().map(ingredient -> ingredient.orElse(null)).toList()));
                }
            }

            if (recipeHolder.value() instanceof ShapelessRecipe shapelessRecipe){
                if (!(isItemBlacklisted(shapelessRecipe.result.create()) || isItemWhitelisted(shapelessRecipe.result.create()))){
                    entries.add(new JEIUncraftingTableRecipe(shapelessRecipe.result.create(), shapelessRecipe.ingredients));
                }
            }

            if (recipeHolder.value() instanceof SmithingTransformRecipe smithingTransformRecipe && UncraftEverythingClient.payloadFromServer.allowUnsmithing()){
                NonNullList<Ingredient> ingredients = NonNullList.create();

                ingredients.add(smithingTransformRecipe.baseIngredient());
                smithingTransformRecipe.additionIngredient().ifPresent(ingredients::add);
                smithingTransformRecipe.templateIngredient().ifPresent(ingredients::add);
                entries.add(new JEIUncraftingTableRecipe(new ItemStack(smithingTransformRecipe.result.item(), 1, smithingTransformRecipe.result.components()), ingredients));
            }

            if (recipeHolder.value() instanceof SmithingTrimRecipe smithingTrimRecipe && UncraftEverythingClient.payloadFromServer.allowUnsmithing()){
                List<Ingredient> output = new ArrayList<>();
                output.add(0, smithingTrimRecipe.baseIngredient());
                output.add(1, smithingTrimRecipe.additionIngredient().get());
                output.add(smithingTrimRecipe.templateIngredient().get());
                smithingTrimRecipe.baseIngredient().items().forEach(itemHolder -> {
                    output.set(0, Ingredient.of(itemHolder.value().asItem()));
                    smithingTrimRecipe.additionIngredient().get().items().forEach(itemHolder1 -> {
                        output.set(1, Ingredient.of(itemHolder1.value()));
                        Holder<TrimMaterial> trimMaterialReference = itemHolder1.value().getDefaultInstance().get(DataComponents.PROVIDES_TRIM_MATERIAL);
                        if (itemHolder.unwrapKey().get().identifier().getPath().contains("diamond")){
                            ItemStack stack = itemHolder.value().asItem().getDefaultInstance();
                            stack.set(DataComponents.TRIM, new ArmorTrim(trimMaterialReference, smithingTrimRecipe.pattern));
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
                if (tagKey.isPresent() && itemStack.is(tagKey.get())) {
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
                if (tagKey.isPresent() && itemStack.is(tagKey.get())) {
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