package com.coolerpromc.uncrafteverything.util;

import com.coolerpromc.uncrafteverything.CommonClientClass;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.networking.ServerBoundRequestConfigPayload;
import com.coolerpromc.uncrafteverything.platform.Services;
import com.coolerpromc.uncrafteverything.platform.util.ingredient.ComponentIngredient;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecipeViewerHelpers {
    public static List<JEIUncraftingTableRecipe> getRecipes(RegistryAccess registryAccess, boolean isREI){
        Services.NETWORK.sendToServer(new ServerBoundRequestConfigPayload());
        List<JEIUncraftingTableRecipe> entries = new ArrayList<>();

        // Add Shulker Boxes (Prevent duplication, only normal shulker box will be outputted)
        Ingredient shulkerBoxIngredient = Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ItemTags.SHULKER_BOXES));
        shulkerBoxIngredient.items().forEach(itemHolder -> {
            if (!itemHolder.value().equals(Items.SHULKER_BOX) && !(isItemBlacklisted(itemHolder.value().getDefaultInstance()) || isItemWhitelisted(itemHolder.value().getDefaultInstance()))){
                DyeColor dyeColor = itemHolder.value().components().get(DataComponents.DYE);
                if (dyeColor != null){
                    HolderSet.Named<Item> itemNamed = BuiltInRegistries.ITEM.getOrThrow(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dyes/" + dyeColor.getName())));
                    itemNamed.forEach(dyeItem -> entries.add(new JEIUncraftingTableRecipe(itemHolder.value().getDefaultInstance(), List.of(Ingredient.of(Blocks.SHULKER_BOX), Ingredient.of(dyeItem.value())))));
                }
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
                                        return Services.INGREDIENT.componentIngredient(new ComponentIngredient(Ingredient.of(itemStack.getItem()), itemStack.getComponentsPatch()));
                                    }
                                }).toList()));
                            }
                        }
                    });
        }

        // Add Enchanted Books
        if (CommonClientClass.payloadFromServer.allowEnchantedItem()){
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
                                    return Services.INGREDIENT.componentIngredient(new ComponentIngredient(Ingredient.of(itemStack.getItem()), itemStack.getComponentsPatch()));
                                }
                            }).toList()));
                        }
                    });

        }

        // Add all items that can be uncrafted
        CommonClientClass.recipesFromServer.forEach(recipeHolder -> {
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

            if (recipeHolder.value() instanceof SmithingTransformRecipe smithingTransformRecipe && CommonClientClass.payloadFromServer.allowUnsmithing()){
                NonNullList<Ingredient> ingredients = NonNullList.create();

                ingredients.add(smithingTransformRecipe.baseIngredient());
                smithingTransformRecipe.additionIngredient().ifPresent(ingredients::add);
                smithingTransformRecipe.templateIngredient().ifPresent(ingredients::add);
                entries.add(new JEIUncraftingTableRecipe(new ItemStack(smithingTransformRecipe.result.item(), 1, smithingTransformRecipe.result.components()), ingredients));
            }

            if (recipeHolder.value() instanceof SmithingTrimRecipe smithingTrimRecipe && CommonClientClass.payloadFromServer.allowUnsmithing()){
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
        if (CommonClientClass.payloadFromServer.restrictionType() != UncraftEverythingConfig.RestrictionType.BLACKLIST){
            return false;
        }

        Identifier itemLocation = UncraftEverythingConfig.inputStackLocation(itemStack);
        String itemLocationString = itemLocation.toString();

        if (CommonClientClass.payloadFromServer.restrictedItems().contains(itemLocationString)) {
            return true;
        }

        for (String entry : CommonClientClass.payloadFromServer.restrictedItems()) {
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
        if (CommonClientClass.payloadFromServer.restrictionType() != UncraftEverythingConfig.RestrictionType.WHITELIST){
            return false;
        }

        Identifier itemLocation = UncraftEverythingConfig.inputStackLocation(itemStack);
        String itemLocationString = itemLocation.toString();

        if (CommonClientClass.payloadFromServer.restrictedItems().contains(itemLocationString)) {
            return false;
        }

        for (String entry : CommonClientClass.payloadFromServer.restrictedItems()) {
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