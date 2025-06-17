package com.coolerpromc.uncrafteverything.compat.rei;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.event.UERecipeReceivedEvent;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen;
import com.coolerpromc.uncrafteverything.util.JEIUncraftingTableRecipe;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.equipment.trim.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.neoforged.neoforge.common.Tags;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@REIPluginClient
public class UEREIPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new UncraftingRecipeCategory(), configuration -> configuration.addWorkstations(EntryStacks.of(UEBlocks.UNCRAFTING_TABLE.get())));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        List<JEIUncraftingTableRecipe> entries = new ArrayList<>();

        // Add Shulker Boxes (Prevent duplication, only normal shulker box will be outputted)
        Ingredient shulkerBoxIngredient = Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(Tags.Items.SHULKER_BOXES));
        shulkerBoxIngredient.getValues().forEach(itemStack -> {
            if (!itemStack.value().equals(Items.SHULKER_BOX)){
                entries.add(new JEIUncraftingTableRecipe(itemStack.value().getDefaultInstance(), List.of(Ingredient.of(Blocks.SHULKER_BOX), Ingredient.of(DyeItem.byColor(Objects.requireNonNull(((ShulkerBoxBlock) ((BlockItem) itemStack.value()).getBlock()).getColor()))))));
            }
        });

        // Add Tipped Arrows
        BasicDisplay.registryAccess().lookup(Registries.POTION).stream()
                .flatMap(HolderLookup::listElements)
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

                        entries.add(new JEIUncraftingTableRecipe(tippedArrow, output, true));
                    }
                });

        // Add Enchanted Books
        BasicDisplay.registryAccess().lookup(Registries.ENCHANTMENT)
                .stream()
                .flatMap(HolderLookup::listElements)
                .forEach(holder -> {
                    if (!holder.isBound()) return;
                    Enchantment enchantment = holder.value();

                    ItemStack diamondSword = new ItemStack(Items.DIAMOND_SWORD);
                    diamondSword.enchant(holder, enchantment.getMaxLevel());

                    List<ItemStack> output = new ArrayList<>();
                    output.add(Items.DIAMOND_SWORD.getDefaultInstance());
                    output.add(EnchantmentHelper.createBook(new EnchantmentInstance(holder, enchantment.getMaxLevel())));

                    entries.add(new JEIUncraftingTableRecipe(diamondSword, output, true));
                });

        // Add all items that can be uncrafted
        UERecipeReceivedEvent.recipeMap.values().forEach(recipeHolder -> {
            if (recipeHolder.value() instanceof ShapedRecipe shapedRecipe){
                entries.add(new JEIUncraftingTableRecipe(shapedRecipe.result, shapedRecipe.getIngredients().stream().map(ingredient -> ingredient.orElse(null)).toList()));
            }

            if (recipeHolder.value() instanceof ShapelessRecipe shapelessRecipe){
                entries.add(new JEIUncraftingTableRecipe(shapelessRecipe.result, shapelessRecipe.ingredients));
            }

            if (recipeHolder.value() instanceof SmithingTransformRecipe smithingTransformRecipe){
                NonNullList<Ingredient> ingredients = NonNullList.create();

                ingredients.add(smithingTransformRecipe.baseIngredient().get());
                ingredients.add(smithingTransformRecipe.additionIngredient().get());
                ingredients.add(smithingTransformRecipe.templateIngredient().get());
                entries.add(new JEIUncraftingTableRecipe(smithingTransformRecipe.result, ingredients));
            }

            if (recipeHolder.value() instanceof SmithingTrimRecipe smithingTrimRecipe){
                RegistryAccess registryAccess = Minecraft.getInstance().level.registryAccess();
                List<Ingredient> output = new ArrayList<>();
                output.add(0, smithingTrimRecipe.baseIngredient().get());
                output.add(1, smithingTrimRecipe.additionIngredient().get());
                output.add(smithingTrimRecipe.templateIngredient().get());
                smithingTrimRecipe.baseIngredient().get().getValues().forEach(itemStack -> {
                    output.set(0, Ingredient.of(itemStack.value().asItem()));
                    smithingTrimRecipe.additionIngredient().get().getValues().forEach(itemStack1 -> {
                        output.set(1, Ingredient.of(itemStack1.value()));
                        Optional<Holder.Reference<TrimMaterial>> trimMaterialReference = TrimMaterials.getFromIngredient(registryAccess, smithingTrimRecipe.additionIngredient().get().getValues().get(0).value().getDefaultInstance());
                        Optional<Holder.Reference<TrimPattern>> trimPatternReference = TrimPatterns.getFromTemplate(registryAccess, smithingTrimRecipe.templateIngredient().get().getValues().get(0).value().getDefaultInstance());
                        if (trimPatternReference.isPresent() && trimMaterialReference.isPresent()){
                            ItemStack stack = itemStack.value().getDefaultInstance();
                            stack.set(DataComponents.TRIM, new ArmorTrim(trimMaterialReference.get(), trimPatternReference.get()));
                            entries.add(new JEIUncraftingTableRecipe(stack, output));
                        }
                    });
                });
            }
        });

        entries.forEach(jeiUncraftingTableRecipe -> {
            registry.add(new UncraftingRecipeDisplay(List.of(EntryIngredients.of(jeiUncraftingTableRecipe.getInput())), jeiUncraftingTableRecipe.getEntryIngredientOutput()));
        });
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerClickArea(screen -> new Rectangle(((screen.width - 176) / 2) + 59, ((screen.height - 166) / 2) + 27, 22, 15), UncraftingTableScreen.class, UncraftingRecipeDisplay.CATEGORY_IDENTIFIER);
    }

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(UncraftingTableScreen.class, screen -> List.of(
            new Rectangle(0, 0, screen.getGuiLeft(), screen.height)
        ));
    }
}
