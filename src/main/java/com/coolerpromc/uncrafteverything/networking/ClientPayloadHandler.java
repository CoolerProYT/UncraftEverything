package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientPayloadHandler {
    public static void handleBlockEntityData(UncraftingTableDataPayload payload, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            Level level = minecraft.level;
            Screen screen = minecraft.screen;

            if (level != null && screen instanceof UncraftingTableScreen uncraftingTableScreen) {
                if (level.getBlockEntity(payload.blockPos()) instanceof UncraftingTableBlockEntity blockEntity) {
                    uncraftingTableScreen.updateFromBlockEntity(payload.recipes());
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static ResponseConfigPayload payloadFromServer;

    public static void handleConfigSync(ResponseConfigPayload payload, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            payloadFromServer = payload;
        });
        context.get().setPacketHandled(true);
    }

    public static void handleRecipeSelectionRequest(UncraftingRecipeSelectionRequestPayload payload, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            Level level = minecraft.level;
            Screen screen = minecraft.screen;

            if (level != null && screen instanceof UncraftingTableScreen uncraftingTableScreen) {
                uncraftingTableScreen.getRecipeSelection();
            }
        });
        context.get().setPacketHandled(true);
    }
}
