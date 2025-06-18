package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.networking.RecipeSyncPayload;
import com.coolerpromc.uncrafteverything.networking.ResponseConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionRequestPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.world.World;

import java.util.List;

public class UncraftEverythingClient implements ClientModInitializer {
    public static ResponseConfigPayload payloadFromServer;
    public static List<RecipeEntry<?>> recipesFromServer;

    @Override
    public void onInitializeClient() {
        HandledScreens.register(UEMenuTypes.UNCRAFTING_TABLE_MENU, UncraftingTableScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(UncraftingTableDataPayload.TYPE, (uncraftingTableDataPayload, context) -> {
            MinecraftClient minecraft = MinecraftClient.getInstance();
            World world = minecraft.world;
            Screen screen = minecraft.currentScreen;

            if (world != null && screen instanceof UncraftingTableScreen uncraftingTableScreen){
                if (world.getBlockEntity(uncraftingTableDataPayload.blockPos()) instanceof UncraftingTableBlockEntity blockEntity){
                    uncraftingTableScreen.updateFromBlockEntity(uncraftingTableDataPayload.recipes());
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

            if (world != null && screen instanceof UncraftingTableScreen uncraftingTableScreen) {
                uncraftingTableScreen.getRecipeSelection();
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(RecipeSyncPayload.TYPE, (recipeSyncPayload, context) -> {
            context.client().execute(() -> {
                recipesFromServer = recipeSyncPayload.recipes();
            });
        });
    }
}
