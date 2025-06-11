package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientPayloadHandler {
    public static void handleBlockEntityData(UncraftingTableDataPayload payload, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            World level = minecraft.level;
            Screen screen = minecraft.screen;

            if (level != null && screen instanceof UncraftingTableScreen) {
                UncraftingTableScreen uncraftingTableScreen = (UncraftingTableScreen) screen;
                if (level.getBlockEntity(payload.blockPos()) instanceof UncraftingTableBlockEntity) {
                    UncraftingTableBlockEntity blockEntity = (UncraftingTableBlockEntity) level.getBlockEntity(payload.blockPos());
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
            World level = minecraft.level;
            Screen screen = minecraft.screen;

            if (level != null && screen instanceof UncraftingTableScreen) {
                UncraftingTableScreen uncraftingTableScreen = (UncraftingTableScreen) screen;
                uncraftingTableScreen.getRecipeSelection();
            }
        });
        context.get().setPacketHandled(true);
    }
}
