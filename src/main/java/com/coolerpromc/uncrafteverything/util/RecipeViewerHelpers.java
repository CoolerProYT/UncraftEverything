package com.coolerpromc.uncrafteverything.util;

import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.ClientPayloadHandler;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
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
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import java.util.*;

public class RecipeViewerHelpers {
    public static List<JEIUncraftingTableRecipe> getRecipes(RegistryAccess registryAccess, boolean isREI){
        RequestConfigPayload.INSTANCE.sendToServer(new RequestConfigPayload());
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
                                        return StrictNBTIngredient.of(itemStack);
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
                                    return StrictNBTIngredient.of(itemStack);
                                }
                            }).toList()));
                        }
                    });

        }

        // Add all items that can be uncrafted
        Minecraft.getInstance().level.getRecipeManager().getRecipes().forEach(recipe -> {
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
                entries.add(new JEIUncraftingTableRecipe(new ItemStack(smithingTransformRecipe.result.getItem(), 1, smithingTransformRecipe.result.getOrCreateTag()), ingredients));
            }

            if (recipe instanceof SmithingTrimRecipe smithingTrimRecipe && ClientPayloadHandler.payloadFromServer.allowUnsmithing()){
                List<Ingredient> output = new ArrayList<>();
                output.add(0, smithingTrimRecipe.base);
                output.add(1, smithingTrimRecipe.addition);
                output.add(smithingTrimRecipe.template);
                Arrays.stream(smithingTrimRecipe.base.getItems()).forEach(itemStack -> {
                    output.set(0, Ingredient.of(itemStack.getItem()));
                    Arrays.stream(smithingTrimRecipe.addition.getItems()).forEach(itemStack1 -> {
                        output.set(1, Ingredient.of(itemStack1));
                        Optional<Holder.Reference<TrimMaterial>> trimMaterialReference = TrimMaterials.getFromIngredient(registryAccess, smithingTrimRecipe.addition.getItems()[0]);
                        Optional<Holder.Reference<TrimPattern>> trimPatternReference = TrimPatterns.getFromTemplate(registryAccess, smithingTrimRecipe.template.getItems()[0]);
                        if (trimPatternReference.isPresent() && trimMaterialReference.isPresent()){
                            ArmorTrim.setTrim(registryAccess, itemStack, new ArmorTrim(trimMaterialReference.get(), trimPatternReference.get()));
                            entries.add(new JEIUncraftingTableRecipe(itemStack, output));
                        }
                    });
                });
            }
        });

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
