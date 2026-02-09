package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.blockentity.custom.AbstractUncraftingTableBE;
import com.coolerpromc.uncrafteverything.command.ModCommands;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingClientConfig;
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
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import java.util.ArrayList;
import java.util.List;

public class UncraftEverythingClient implements ClientModInitializer {
    public static ResponseConfigPayload payloadFromServer;
    public static List<RecipeHolder<?>> recipesFromServer = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        MenuScreens.register(UEMenuTypes.UNCRAFTING_TABLE_MENU, UncraftingTableScreen::new);
        MenuScreens.register(UEMenuTypes.AUTO_UNCRAFTING_TABLE_MENU, AutoUncraftingTableScreen::new);

        UncraftEverythingClientConfig.load();
        UncraftEverythingClientConfig.save();

        ClientPlayNetworking.registerGlobalReceiver(UncraftingTableDataPayload.TYPE, (uncraftingTableDataPayload, context) -> {
            Minecraft minecraft = Minecraft.getInstance();
            Level world = minecraft.level;
            Screen screen = minecraft.screen;

            if (world != null && screen instanceof AbstractUncraftingScreen<? extends AbstractUncraftingTableBE, ? extends AbstractUncraftingMenu<? extends AbstractUncraftingTableBE>> uncraftingTableScreen){
                if (world.getBlockEntity(uncraftingTableDataPayload.blockPos()) instanceof AbstractUncraftingTableBE){
                    uncraftingTableScreen.updateFromBlockEntity(uncraftingTableDataPayload.recipes(), uncraftingTableDataPayload.size(), uncraftingTableDataPayload.shouldSendPacket());
                }
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(ResponseConfigPayload.TYPE, (responseConfigPayload, context) -> {
            payloadFromServer = responseConfigPayload;
        });

        ClientPlayNetworking.registerGlobalReceiver(UncraftingRecipeSelectionRequestPayload.TYPE, (uncraftingRecipeSelectionRequestPayload, context) -> {
            Minecraft minecraft = Minecraft.getInstance();
            Level world = minecraft.level;
            Screen screen = minecraft.screen;

            if (world != null && screen instanceof AbstractUncraftingScreen<? extends AbstractUncraftingTableBE, ? extends AbstractUncraftingMenu<? extends AbstractUncraftingTableBE>> uncraftingTableScreen) {
                uncraftingTableScreen.getRecipeSelection();
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(minecraftServer -> {
            UncraftEverythingClientConfig.shutdown();
        });

        ClientCommandRegistrationCallback.EVENT.register((commandDispatcher, commandRegistryAccess) -> {
            ModCommands.register(commandDispatcher);
        });

        ClientRecipeSynchronizedEvent.EVENT.register((minecraft, synchronizedRecipes) -> {
            RecipeMap recipes = RecipeMap.create(synchronizedRecipes.recipes().stream().toList());
            recipesFromServer.clear();
            recipesFromServer.addAll(recipes.byType(RecipeType.CRAFTING));
            recipesFromServer.addAll(recipes.byType(RecipeType.SMITHING));
        });
    }
}
