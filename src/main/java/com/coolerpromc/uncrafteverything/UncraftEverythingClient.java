package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.networking.ResponseConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionRequestPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class UncraftEverythingClient implements ClientModInitializer {
    public static ResponseConfigPayload payloadFromServer;

    @Override
    public void onInitializeClient() {
        HandledScreens.register(UEMenuTypes.UNCRAFTING_TABLE_MENU, UncraftingTableScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(UncraftingTableDataPayload.ID, (minecraftClient, clientPlayNetworkHandler, packetByteBuf, packetSender) -> {
            MinecraftClient minecraft = MinecraftClient.getInstance();
            World world = minecraft.world;
            Screen screen = minecraft.currentScreen;

            if (world != null && screen instanceof UncraftingTableScreen uncraftingTableScreen){
                UncraftingTableDataPayload payload = UncraftingTableDataPayload.decode(packetByteBuf);
                uncraftingTableScreen.updateFromBlockEntity(payload.recipes());
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(ResponseConfigPayload.TYPE, (minecraftClient, clientPlayNetworkHandler, packetByteBuf, packetSender) -> {
            payloadFromServer = ResponseConfigPayload.decode(packetByteBuf);
        });

        ClientPlayNetworking.registerGlobalReceiver(UncraftingRecipeSelectionRequestPayload.TYPE, (uncraftingRecipeSelectionRequestPayload, context) -> {
            MinecraftClient minecraft = MinecraftClient.getInstance();
            World world = minecraft.world;
            Screen screen = minecraft.currentScreen;

            if (world != null && screen instanceof UncraftingTableScreen uncraftingTableScreen) {
                uncraftingTableScreen.getRecipeSelection();
            }
        });
    }
}
