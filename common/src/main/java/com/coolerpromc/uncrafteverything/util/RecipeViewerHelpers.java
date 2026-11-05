package com.coolerpromc.uncrafteverything.util;

import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.ClientPayloadHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.armortrim.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import com.coolerpromc.uncrafteverything.platform.Services;

import java.util.*;

public class RecipeViewerHelpers {
    public static List<JEIUncraftingTableRecipe> getRecipes(RegistryAccess registryAccess, boolean isREI){
        // Config is synchronized on login. Registration must not initiate network work.
        List<JEIUncraftingTableRecipe> entries = new ArrayList<>();

        // Add Shulker Boxes (Prevent duplication, only normal shulker box will be outputted)
        Ingredient shulkerBoxIngredient = Ingredient.of(BuiltInRegistries.ITEM.getOrCreateTag(UETags.Items.SHULKER_BOXES).key());
        Arrays.stream(shulkerBoxIngredient.getItems()).forEach(itemStack -> {
            if (!itemStack.is(Items.SHULKER_BOX) && !(isItemBlacklisted(itemStack) || isItemWhitelisted(itemStack))){
                entries.add(new JEIUncraftingTableRecipe(itemStack, List.of(Ingredient.of(Blocks.SHULKER_BOX), Ingredient.of(DyeItem.byColor(Objects.requireNonNull(((ShulkerBoxBlock) ((BlockItem) itemStack.getItem()).getBlock()).getColor()))))));
            }
        });

        // Add Tipped Arrows
        if (!(isItemBlacklisted(Items.TIPPED_ARROW.getDefaultInstance()) || isItemWhitelisted(Items.TIPPED_ARROW.getDefaultInstance()))){
            registryAccess.lookup(Registries.POTION).stream()
                    .flatMap(HolderLookup::listElements)
                    .map(Holder::value)
                    .forEach(potion -> {
                        if (potion != Potions.WATER) {
                            ItemStack tippedArrow = PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), potion);
                            List<ItemStack> output = new ArrayList<>();
                            output.add(Items.ARROW.getDefaultInstance());
                            output.add(Items.ARROW.getDefaultInstance());
                            output.add(Items.ARROW.getDefaultInstance());
                            output.add(Items.ARROW.getDefaultInstance());
                            output.add(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), potion));
                            output.add(Items.ARROW.getDefaultInstance());
                            output.add(Items.ARROW.getDefaultInstance());
                            output.add(Items.ARROW.getDefaultInstance());
                            output.add(Items.ARROW.getDefaultInstance());

                            if (isREI){
                                entries.add(new JEIUncraftingTableRecipe(tippedArrow, output, true));
                            }
                            else{
                                entries.add(new JEIUncraftingTableRecipe(tippedArrow, output.stream().map(itemStack -> {
                                    if (!itemStack.hasTag()){
                                        return Ingredient.of(itemStack.getItem());
                                    }
                                    else{
                                        return Services.INGREDIENT.strict(itemStack);
                                    }
                                }).toList()));
                            }
                        }
                    });
        }

        // Add Enchanted Books
        if (ClientPayloadHandler.payloadFromServer.allowEnchantedItem()){
            registryAccess.lookup(Registries.ENCHANTMENT)
                    .stream()
                    .flatMap(HolderLookup::listElements)
                    .forEach(holder -> {
                        if (!holder.isBound()) return;
                        Enchantment enchantment = holder.value();

                        ItemStack diamondSword = new ItemStack(Items.DIAMOND_SWORD);
                        diamondSword.enchant(holder.value(), enchantment.getMaxLevel());

                        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                        book.enchant(holder.value(), enchantment.getMaxLevel());

                        List<ItemStack> output = new ArrayList<>();
                        output.add(Items.DIAMOND_SWORD.getDefaultInstance());
                        output.add(book);

                        if (isREI){
                            entries.add(new JEIUncraftingTableRecipe(diamondSword, output, true));
                        }
                        else{
                            entries.add(new JEIUncraftingTableRecipe(diamondSword, output.stream().map(itemStack -> {
                                if (!itemStack.hasTag()){
                                    return Ingredient.of(itemStack.getItem());
                                }
                                else{
                                    return Services.INGREDIENT.strict(itemStack);
                                }
                            }).toList()));
                        }
                    });

        }

        // Add all items that can be uncrafted
        int trimPreviewsRemaining = TrimRecipeViewerRecipes.MAX_TOTAL;
        for (Recipe<?> recipe : Minecraft.getInstance().level.getRecipeManager().getRecipes()) {
            if (recipe instanceof ShapedRecipe shapedRecipe){
                if (!(isItemBlacklisted(shapedRecipe.result) || isItemWhitelisted(shapedRecipe.result))){
                    entries.add(new JEIUncraftingTableRecipe(shapedRecipe.result, shapedRecipe.getIngredients()));
                }
            }

            if (recipe instanceof ShapelessRecipe shapelessRecipe){
                if (!(isItemBlacklisted(shapelessRecipe.result) || isItemWhitelisted(shapelessRecipe.result))){
                    entries.add(new JEIUncraftingTableRecipe(shapelessRecipe.result, shapelessRecipe.ingredients));
                }
            }

            if (recipe instanceof SmithingTransformRecipe smithingTransformRecipe && ClientPayloadHandler.payloadFromServer.allowUnsmithing()){
                NonNullList<Ingredient> ingredients = NonNullList.create();

                ingredients.add(smithingTransformRecipe.base);
                ingredients.add(smithingTransformRecipe.addition);
                ingredients.add(smithingTransformRecipe.template);
                entries.add(new JEIUncraftingTableRecipe(com.coolerpromc.uncrafteverything.platform.ItemStacks.create(smithingTransformRecipe.result.getItem(), 1, smithingTransformRecipe.result.getOrCreateTag()), ingredients));
            }

            if (recipe instanceof SmithingTrimRecipe smithingTrimRecipe && ClientPayloadHandler.payloadFromServer.allowUnsmithing()){
                var previews = TrimRecipeViewerRecipes.getRecipes(registryAccess, smithingTrimRecipe, trimPreviewsRemaining);
                entries.addAll(previews);
                trimPreviewsRemaining -= previews.size();
            }
        }

        return entries;
    }

    public static boolean isItemBlacklisted(ItemStack itemStack) {
        if (ClientPayloadHandler.payloadFromServer.restrictionType() != UncraftEverythingConfig.RestrictionType.BLACKLIST){
            return false;
        }

        ResourceLocation itemLocation = UncraftEverythingConfig.inputStackLocation(itemStack);
        String itemLocationString = itemLocation.toString();

        if (ClientPayloadHandler.payloadFromServer.restrictedItems().contains(itemLocationString)) {
            return true;
        }

        for (String entry : ClientPayloadHandler.payloadFromServer.restrictedItems()) {
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
        if (ClientPayloadHandler.payloadFromServer.restrictionType() != UncraftEverythingConfig.RestrictionType.WHITELIST){
            return false;
        }

        ResourceLocation itemLocation = UncraftEverythingConfig.inputStackLocation(itemStack);
        String itemLocationString = itemLocation.toString();

        if (ClientPayloadHandler.payloadFromServer.restrictedItems().contains(itemLocationString)) {
            return false;
        }

        for (String entry : ClientPayloadHandler.payloadFromServer.restrictedItems()) {
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
