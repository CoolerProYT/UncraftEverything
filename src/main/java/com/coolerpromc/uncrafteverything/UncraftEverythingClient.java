package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.networking.ResponseConfigPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import net.minecraft.world.World;

public class UncraftEverythingClient implements ClientModInitializer {
    public static ResponseConfigPayload payloadFromServer;

    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(UEMenuTypes.UNCRAFTING_TABLE_MENU, UncraftingTableScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(UncraftingTableDataPayload.ID, (minecraftClient, clientPlayNetworkHandler, packetByteBuf, packetSender) -> {
            try{
                MinecraftClient minecraft = MinecraftClient.getInstance();
                World world = minecraft.world;
                Screen screen = minecraft.currentScreen;

                if (world != null && screen instanceof UncraftingTableScreen){
                    UncraftingTableScreen uncraftingTableScreen = (UncraftingTableScreen) screen;
                    UncraftingTableDataPayload uncraftingTableDataPayload = packetByteBuf.decode(UncraftingTableDataPayload.CODEC);

                    if (world.getBlockEntity(uncraftingTableDataPayload.blockPos()) instanceof UncraftingTableBlockEntity){
                        uncraftingTableScreen.updateFromBlockEntity(uncraftingTableDataPayload.recipes());
                    }
                }
            } catch (Exception e) {
                System.out.println("Failed to decode UncraftingTableDataPayload: " + e.getMessage());
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(ResponseConfigPayload.TYPE, (minecraftClient, clientPlayNetworkHandler, packetByteBuf, packetSender) -> {
            payloadFromServer = ResponseConfigPayload.decode(packetByteBuf);
        });
    }
}
