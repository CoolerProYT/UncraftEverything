package com.coolerpromc.uncrafteverything.compat.rei;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.screen.custom.AbstractScrollableScreen;
import com.coolerpromc.uncrafteverything.screen.custom.AbstractUncraftingScreen;
import com.coolerpromc.uncrafteverything.util.JEIUncraftingTableRecipe;
import com.coolerpromc.uncrafteverything.util.RecipeViewerHelpers;
import java.util.List;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.OverlayDecider;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionResult;

public class UEREIPlugin implements REIClientPlugin {
  @Override
  public void registerCategories(CategoryRegistry registry) {
    registry.add(
        new UncraftingRecipeCategory(),
        configuration ->
            configuration.addWorkstations(EntryStacks.of(UEBlocks.UNCRAFTING_TABLE.get())));
  }

  @Override
  public void registerDisplays(DisplayRegistry registry) {
    List<JEIUncraftingTableRecipe> entries =
        RecipeViewerHelpers.getRecipes(BasicDisplay.registryAccess(), true);

    entries.forEach(
        jeiUncraftingTableRecipe -> {
          registry.add(
              new UncraftingRecipeDisplay(
                  List.of(EntryIngredients.of(jeiUncraftingTableRecipe.getInput())),
                  EntryIngredients.ofIngredients(jeiUncraftingTableRecipe.getOutputs())));
        });
  }

  @Override
  public void registerScreens(ScreenRegistry registry) {
    // Recipe viewers must not consume clicks belonging to our custom controls.
    registry.registerDecider(
        new OverlayDecider() {
          @Override
          public <R extends Screen> boolean isHandingScreen(Class<R> type) {
            return AbstractUncraftingScreen.class.isAssignableFrom(type)
                || AbstractScrollableScreen.class.isAssignableFrom(type);
          }

          @Override
          public double getPriority() {
            return 1000;
          }

          @Override
          public <R extends Screen> InteractionResult shouldScreenBeOverlaid(R screen) {
            return screen instanceof AbstractScrollableScreen
                ? InteractionResult.FAIL
                : InteractionResult.PASS;
          }
        });
    registry.registerClickArea(
        screen -> new Rectangle(screen.getGuiLeft() + 59, screen.getGuiTop() + 35, 22, 15),
        AbstractUncraftingScreen.class,
        UncraftingRecipeDisplay.CATEGORY_IDENTIFIER);
  }

  @Override
  public void registerExclusionZones(ExclusionZones zones) {
    zones.register(
        AbstractUncraftingScreen.class,
        screen -> List.of(new Rectangle(0, 0, screen.getGuiRight(), screen.height)));
  }
}
