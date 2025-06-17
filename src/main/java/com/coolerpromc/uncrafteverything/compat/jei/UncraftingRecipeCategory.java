package com.coolerpromc.uncrafteverything.compat.jei;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.util.JEIUncraftingTableRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("removal")
public record UncraftingRecipeCategory(IGuiHelper guiHelper) implements IRecipeCategory<JEIUncraftingTableRecipe> {
    @Override
    public @NotNull RecipeType<JEIUncraftingTableRecipe> getRecipeType() {
        return UEJEIPlugin.UNCRAFTING_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("block.uncrafteverything.uncrafting_table");
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return guiHelper.createDrawable(ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "textures/gui/uncrafting_table_gui.png"), 20, 15, 137, 56);
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return guiHelper.createDrawableItemStack(new ItemStack(UEBlocks.UNCRAFTING_TABLE.get()));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder iRecipeLayoutBuilder, JEIUncraftingTableRecipe recipe, @NotNull IFocusGroup iFocusGroup) {
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT, 6, 20).addItemStack(recipe.getInput());
        for (int i = 0; i < recipe.getOutputs().size(); i ++){
            iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, 78 + 18 * (i % 3), 2 + (i / 3) * 18).addIngredients(recipe.getOutputs().get(i));
        }
    }
}
