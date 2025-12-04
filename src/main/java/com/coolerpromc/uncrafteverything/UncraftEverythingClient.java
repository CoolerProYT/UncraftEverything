package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.blockentity.custom.AbstractUncraftingTableBE;
import com.coolerpromc.uncrafteverything.command.ModCommands;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingClientConfig;
import com.coolerpromc.uncrafteverything.networking.RecipeSyncPayload;
import com.coolerpromc.uncrafteverything.networking.ResponseConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionRequestPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import com.coolerpromc.uncrafteverything.screen.custom.AbstractUncraftingMenu;
import com.coolerpromc.uncrafteverything.screen.custom.AbstractUncraftingScreen;
import com.coolerpromc.uncrafteverything.screen.custom.AutoUncraftingTableScreen;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class UncraftEverythingClient implements ClientModInitializer {
    public static ResponseConfigPayload payloadFromServer;
    public static List<RecipeEntry<?>> recipesFromServer = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        HandledScreens.register(UEMenuTypes.UNCRAFTING_TABLE_MENU, UncraftingTableScreen::new);
        HandledScreens.register(UEMenuTypes.AUTO_UNCRAFTING_TABLE_MENU, AutoUncraftingTableScreen::new);

        UncraftEverythingClientConfig.load();
        UncraftEverythingClientConfig.save();

        ClientPlayNetworking.registerGlobalReceiver(UncraftingTableDataPayload.TYPE, (uncraftingTableDataPayload, context) -> {
            MinecraftClient minecraft = MinecraftClient.getInstance();
            World world = minecraft.world;
            Screen screen = minecraft.currentScreen;

            if (world != null && screen instanceof AbstractUncraftingScreen<? extends AbstractUncraftingTableBE, ? extends AbstractUncraftingMenu<? extends AbstractUncraftingTableBE>> uncraftingTableScreen){
                if (world.getBlockEntity(uncraftingTableDataPayload.blockPos()) instanceof AbstractUncraftingTableBE){
                    uncraftingTableScreen.updateFromBlockEntity(uncraftingTableDataPayload.recipes(), uncraftingTableDataPayload.size());
                }
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(ResponseConfigPayload.TYPE, (responseConfigPayload, context) -> {
            payloadFromServer = responseConfigPayload;
        });

        ClientPlayNetworking.registerGlobalReceiver(UncraftingRecipeSelectionRequestPayload.TYPE, (uncraftingRecipeSelectionRequestPayload, context) -> {
            MinecraftClient minecraft = MinecraftClient.getInstance();
            World world = minecraft.world;
            Screen screen = minecraft.currentScreen;

            if (world != null && screen instanceof AbstractUncraftingScreen<? extends AbstractUncraftingTableBE, ? extends AbstractUncraftingMenu<? extends AbstractUncraftingTableBE>> uncraftingTableScreen) {
                uncraftingTableScreen.getRecipeSelection();
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(RecipeSyncPayload.TYPE, (recipeSyncPayload, context) -> {
            context.client().execute(() -> {
                if (recipesFromServer.size() >= recipeSyncPayload.totalRecipe()){
                    recipesFromServer.clear();
                }
                recipesFromServer.addAll(recipeSyncPayload.recipes());
            });
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(minecraftServer -> {
            UncraftEverythingClientConfig.shutdown();
        });

        ClientCommandRegistrationCallback.EVENT.register((commandDispatcher, commandRegistryAccess) -> {
            ModCommands.register(commandDispatcher);
        });
    }
}
