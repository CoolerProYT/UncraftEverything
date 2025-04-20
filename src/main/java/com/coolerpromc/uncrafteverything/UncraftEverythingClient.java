package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.world.World;

public class UncraftEverythingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(UEMenuTypes.UNCRAFTING_TABLE_MENU, UncraftingTableScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(UncraftingTableDataPayload.ID, (minecraftClient, clientPlayNetworkHandler, packetByteBuf, packetSender) -> {
            MinecraftClient minecraft = MinecraftClient.getInstance();
            World world = minecraft.world;
            Screen screen = minecraft.currentScreen;

            if (world != null && screen instanceof UncraftingTableScreen uncraftingTableScreen){
                UncraftingTableDataPayload uncraftingTableDataPayload = packetByteBuf.decodeAsJson(UncraftingTableDataPayload.CODEC);
                if (world.getBlockEntity(uncraftingTableDataPayload.blockPos()) instanceof UncraftingTableBlockEntity blockEntity){
                    uncraftingTableScreen.updateFromBlockEntity(uncraftingTableDataPayload.recipes());
                }
            }
        });
    }
}
