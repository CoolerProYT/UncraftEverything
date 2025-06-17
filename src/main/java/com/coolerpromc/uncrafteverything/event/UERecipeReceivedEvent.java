package com.coolerpromc.uncrafteverything.event;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

@EventBusSubscriber(modid = UncraftEverything.MODID, value = Dist.CLIENT)
public class UERecipeReceivedEvent {
    public static RecipeMap recipeMap = RecipeMap.EMPTY;

    @SubscribeEvent
    public static void onRecipesReceived(RecipesReceivedEvent event) {
        recipeMap = event.getRecipeMap();
    }
}