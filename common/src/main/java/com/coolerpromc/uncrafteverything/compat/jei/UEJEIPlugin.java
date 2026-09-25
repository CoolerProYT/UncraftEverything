package com.coolerpromc.uncrafteverything.compat.jei;

import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingClientConfig;
import com.coolerpromc.uncrafteverything.screen.custom.AbstractUncraftingScreen;
import com.coolerpromc.uncrafteverything.util.JEIUncraftingTableRecipe;
import com.coolerpromc.uncrafteverything.util.RecipeViewerHelpers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@JeiPlugin
public class UEJEIPlugin implements IModPlugin {
    public static final IRecipeType<JEIUncraftingTableRecipe> UNCRAFTING_TYPE = IRecipeType.create(Constants.MODID, "uncrafting_table", JEIUncraftingTableRecipe.class);
    private static final AtomicBoolean RELOAD_LISTENER_REGISTERED = new AtomicBoolean(false);
    private static IJeiRuntime runtime;

    @Override
    public @NotNull Identifier getPluginUid() {
        return Constants.id("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new UncraftingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<JEIUncraftingTableRecipe> entries = RecipeViewerHelpers.getRecipes(Minecraft.getInstance().level.registryAccess(), false);
        registration.addRecipes(UNCRAFTING_TYPE, entries);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(AbstractUncraftingScreen.class, 59, 35, 22, 15, UNCRAFTING_TYPE);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(UNCRAFTING_TYPE, new ItemStack(UEBlocks.UNCRAFTING_TABLE));
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        if (RELOAD_LISTENER_REGISTERED.compareAndSet(false, true)) {
            UncraftEverythingClientConfig.CONFIG.addReloadListener(() -> Minecraft.getInstance().execute(UEJEIPlugin::updateCategoryVisibility));
        }
        updateCategoryVisibility();
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    private static void updateCategoryVisibility() {
        if (runtime == null) return;

        if (UncraftEverythingClientConfig.CONFIG.showJeiUncraftingCategory()) {
            runtime.getRecipeManager().unhideRecipeCategory(UNCRAFTING_TYPE);
        }
        else {
            runtime.getRecipeManager().hideRecipeCategory(UNCRAFTING_TYPE);
        }
    }
}
