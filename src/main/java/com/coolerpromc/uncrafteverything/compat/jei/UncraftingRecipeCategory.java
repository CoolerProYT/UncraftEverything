package com.coolerpromc.uncrafteverything.compat.jei;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.util.JEIUncraftingTableRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UncraftingRecipeCategory implements IRecipeCategory<JEIUncraftingTableRecipe> {
    public static ResourceLocation UID = new ResourceLocation(UncraftEverything.MODID, "uncrafting_table");
    private final IGuiHelper guiHelper;

    public UncraftingRecipeCategory(IGuiHelper guiHelper){
        this.guiHelper = guiHelper;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends JEIUncraftingTableRecipe> getRecipeClass() {
        return JEIUncraftingTableRecipe.class;
    }

    @Override
    public String getTitle() {
        return new TranslationTextComponent("block.uncrafteverything.uncrafting_table").getString();
    }

    @Override
    public IDrawable getBackground() {
        return guiHelper.createDrawable(new ResourceLocation(UncraftEverything.MODID, "textures/gui/uncrafting_table_gui.png"), 20, 15, 137, 56);
    }

    @Override
    public IDrawable getIcon() {
        return guiHelper.createDrawableIngredient(new ItemStack(UEBlocks.UNCRAFTING_TABLE.get()));
    }

    @Override
    public void setIngredients(JEIUncraftingTableRecipe jeiUncraftingTableRecipe, IIngredients iIngredients) {
        iIngredients.setInput(VanillaTypes.ITEM, jeiUncraftingTableRecipe.getInput());
        this.setOutputIngredients(iIngredients, jeiUncraftingTableRecipe.getOutputs());
    }

    public void setOutputIngredients(IIngredients iIngredients, List<Ingredient> inputs) {
        List<List<ItemStack>> inputLists = new ArrayList<>();

        for(Ingredient input : inputs) {
            ItemStack[] stacks = input.getItems();
            List<ItemStack> expandedInput = Arrays.asList(stacks);
            inputLists.add(expandedInput);
        }

        iIngredients.setOutputLists(VanillaTypes.ITEM, inputLists);
    }

    @Override
    public void setRecipe(IRecipeLayout iRecipeLayout, JEIUncraftingTableRecipe jeiUncraftingTableRecipe, IIngredients iIngredients) {
        iRecipeLayout.getItemStacks().init(0, true,5, 19);
        for (int i = 0; i < jeiUncraftingTableRecipe.getOutputs().size(); i ++){
            iRecipeLayout.getItemStacks().init(i + 1, false, 77 + 18 * (i % 3), 1 + (i / 3) * 18);
        }
        iRecipeLayout.getItemStacks().set(iIngredients);
    }
}
