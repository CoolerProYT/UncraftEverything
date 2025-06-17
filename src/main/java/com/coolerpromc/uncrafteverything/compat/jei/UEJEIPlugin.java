package com.coolerpromc.uncrafteverything.compat.jei;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.event.UERecipeReceivedEvent;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen;
import com.coolerpromc.uncrafteverything.util.JEIUncraftingTableRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.ProvidesTrimMaterial;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.equipment.trim.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("removal")
@JeiPlugin
public class UEJEIPlugin implements IModPlugin {
    public static final IRecipeType<JEIUncraftingTableRecipe> UNCRAFTING_TYPE = IRecipeType.create(UncraftEverything.MODID, "uncrafting_table", JEIUncraftingTableRecipe.class);

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new UncraftingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<JEIUncraftingTableRecipe> entries = new ArrayList<>();

        // Add Shulker Boxes (Prevent duplication, only normal shulker box will be outputted)
        Ingredient shulkerBoxIngredient = Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(Tags.Items.SHULKER_BOXES));
        shulkerBoxIngredient.getValues().forEach(itemStack -> {
            if (!itemStack.value().equals(Items.SHULKER_BOX)){
                entries.add(new JEIUncraftingTableRecipe(itemStack.value().getDefaultInstance(), List.of(Ingredient.of(Blocks.SHULKER_BOX), Ingredient.of(DyeItem.byColor(Objects.requireNonNull(((ShulkerBoxBlock) ((BlockItem) itemStack.value()).getBlock()).getColor()))))));
            }
        });

        // Add Tipped Arrows
        Minecraft.getInstance().level.registryAccess().lookup(Registries.POTION).stream().flatMap(HolderLookup::listElements).forEach(potion -> {
            if (potion != Potions.WATER) {
                ItemStack tippedArrow = PotionContents.createItemStack(Items.TIPPED_ARROW, potion);
                List<Ingredient> output = new ArrayList<>();
                output.add(Ingredient.of(Items.ARROW));
                output.add(Ingredient.of(Items.ARROW));
                output.add(Ingredient.of(Items.ARROW));
                output.add(Ingredient.of(Items.ARROW));
                output.add(DataComponentIngredient.of(false, PotionContents.createItemStack(Items.LINGERING_POTION, potion)));
                output.add(Ingredient.of(Items.ARROW));
                output.add(Ingredient.of(Items.ARROW));
                output.add(Ingredient.of(Items.ARROW));
                output.add(Ingredient.of(Items.ARROW));

                entries.add(new JEIUncraftingTableRecipe(tippedArrow, output));
            }
        });

        // Add Enchanted Books
        Minecraft.getInstance().level.registryAccess().lookup(Registries.ENCHANTMENT).stream().flatMap(HolderLookup::listElements).forEach(enchantment -> {
            if (enchantment != null) {
                for (int i = 1;i <= enchantment.value().getMaxLevel();i++){
                    ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
                    enchantedBook.enchant(enchantment, i);
                    ItemStack dirt = new ItemStack(Items.DIAMOND_SWORD);
                    dirt.enchant(enchantment, i);
                    List<Ingredient> output = new ArrayList<>();
                    output.add(Ingredient.of(Items.DIAMOND_SWORD));
                    output.add(DataComponentIngredient.of(false, enchantedBook));

                    entries.add(new JEIUncraftingTableRecipe(dirt, output));
                }
            }
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

                ingredients.add(smithingTransformRecipe.baseIngredient());
                ingredients.add(smithingTransformRecipe.additionIngredient().get());
                ingredients.add(smithingTransformRecipe.templateIngredient().get());
                entries.add(new JEIUncraftingTableRecipe(new ItemStack(smithingTransformRecipe.result.item(), 1, smithingTransformRecipe.result.components()), ingredients));
            }

            if (recipeHolder.value() instanceof SmithingTrimRecipe smithingTrimRecipe){
                RegistryAccess registryAccess = Minecraft.getInstance().level.registryAccess();

                List<Ingredient> output = new ArrayList<>();
                output.add(0, smithingTrimRecipe.baseIngredient());
                output.add(1, smithingTrimRecipe.additionIngredient().get());
                output.add(smithingTrimRecipe.templateIngredient().get());
                smithingTrimRecipe.baseIngredient().getValues().stream().forEach(itemHolder -> {
                    output.set(0, Ingredient.of(itemHolder.value().asItem()));
                    smithingTrimRecipe.additionIngredient().get().getValues().forEach(itemHolder1 -> {
                        output.set(1, Ingredient.of(itemHolder1.value()));
                        Optional<Holder<TrimMaterial>> trimMaterialReference = TrimMaterials.getFromIngredient(registryAccess, smithingTrimRecipe.additionIngredient().get().getValues().get(0).value().getDefaultInstance());
                        if (trimMaterialReference.isPresent()){
                            ItemStack stack = itemHolder1.value().getDefaultInstance();
                            stack.set(DataComponents.TRIM, new ArmorTrim(trimMaterialReference.get(), smithingTrimRecipe.pattern));
                            entries.add(new JEIUncraftingTableRecipe(stack, output));
                        }
                    });
                });
            }
        });
        registration.addRecipes(UNCRAFTING_TYPE, entries);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(UncraftingTableScreen.class, 59, 35, 22, 15, UNCRAFTING_TYPE);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(UEBlocks.UNCRAFTING_TABLE.get()), UNCRAFTING_TYPE);
    }
}
