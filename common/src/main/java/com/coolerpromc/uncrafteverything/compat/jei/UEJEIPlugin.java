package com.coolerpromc.uncrafteverything.compat.jei;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.screen.custom.AbstractUncraftingScreen;
import com.coolerpromc.uncrafteverything.util.JEIUncraftingTableRecipe;
import com.mojang.logging.LogUtils;
import com.coolerpromc.uncrafteverything.util.RecipeViewerHelpers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class UEJEIPlugin implements IModPlugin {
    public static final RecipeType<JEIUncraftingTableRecipe> UNCRAFTING_TYPE = RecipeType.create(UncraftEverything.MODID, "uncrafting_table", JEIUncraftingTableRecipe.class);

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return new ResourceLocation(UncraftEverything.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new UncraftingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        long started = System.nanoTime();
        LogUtils.getLogger().info("Building UncraftEverything JEI recipes");
        List<JEIUncraftingTableRecipe> entries = RecipeViewerHelpers.getRecipes(level.registryAccess(), false);
        LogUtils.getLogger().info("Built {} UncraftEverything JEI recipes in {} ms; registering with JEI",
                entries.size(), (System.nanoTime() - started) / 1_000_000);
        registration.addRecipes(UNCRAFTING_TYPE, entries);
        LogUtils.getLogger().info("UncraftEverything JEI registration completed in {} ms",
                (System.nanoTime() - started) / 1_000_000);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(AbstractUncraftingScreen.class, 59, 35, 22, 15, UNCRAFTING_TYPE);
        registration.addGuiContainerHandler(AbstractUncraftingScreen.class, new IGuiContainerHandler<>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(AbstractUncraftingScreen screen) {
                List<Rect2i> areas = new ArrayList<>();
                areas.add(new Rect2i(0, 0, screen.getGuiLeft(), screen.height));
                return areas;
            }
        });
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(UEBlocks.UNCRAFTING_TABLE.get()), UNCRAFTING_TYPE);
    }
}
