package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.command.ModCommands;
import com.coolerpromc.uncrafteverything.config.FabricUncraftEverythingClientConfig;
import com.coolerpromc.uncrafteverything.networking.ClientBoundResponseConfigPayload;
import com.coolerpromc.uncrafteverything.networking.ClientBoundUncraftingRecipeSelectionRequestPayload;
import com.coolerpromc.uncrafteverything.networking.ClientBoundUncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.platform.util.FabricClientPayloadContext;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import com.coolerpromc.uncrafteverything.screen.custom.AutoUncraftingTableScreen;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;

public class FabricUncraftEverythingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(UEMenuTypes.UNCRAFTING_TABLE_MENU.get(), UncraftingTableScreen::new);
        MenuScreens.register(UEMenuTypes.AUTO_UNCRAFTING_TABLE_MENU.get(), AutoUncraftingTableScreen::new);

        FabricUncraftEverythingClientConfig.load();
        FabricUncraftEverythingClientConfig.save();

        ClientPlayNetworking.registerGlobalReceiver(ClientBoundUncraftingTableDataPayload.TYPE, (payload, context) -> ClientBoundUncraftingTableDataPayload.ClientHandler.handle(payload, new FabricClientPayloadContext(context)));
        ClientPlayNetworking.registerGlobalReceiver(ClientBoundResponseConfigPayload.TYPE, (payload, context) -> ClientBoundResponseConfigPayload.ClientHandler.handle(payload, new FabricClientPayloadContext(context)));
        ClientPlayNetworking.registerGlobalReceiver(ClientBoundUncraftingRecipeSelectionRequestPayload.TYPE, (payload, context) -> ClientBoundUncraftingRecipeSelectionRequestPayload.ClientHandler.handle(payload, new FabricClientPayloadContext(context)));

        ClientLifecycleEvents.CLIENT_STOPPING.register(_ -> FabricUncraftEverythingClientConfig.shutdown());
        ClientCommandRegistrationCallback.EVENT.register((commandDispatcher, _) -> ModCommands.register(commandDispatcher));

        ClientRecipeSynchronizedEvent.EVENT.register((minecraft, synchronizedRecipes) -> {
            RecipeMap recipes = RecipeMap.create(synchronizedRecipes.recipes().stream().toList());
            UncraftEverythingClient.recipesFromServer.clear();
            UncraftEverythingClient.recipesFromServer.addAll(recipes.byType(RecipeType.CRAFTING));
            UncraftEverythingClient.recipesFromServer.addAll(recipes.byType(RecipeType.SMITHING));
        });
    }
}
