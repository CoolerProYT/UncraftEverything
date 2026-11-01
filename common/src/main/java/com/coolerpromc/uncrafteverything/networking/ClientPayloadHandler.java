package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.blockentity.custom.AbstractUncraftingTableBE;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.screen.custom.AbstractUncraftingMenu;
import com.coolerpromc.uncrafteverything.screen.custom.AbstractUncraftingScreen;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.Level;
import com.coolerpromc.uncrafteverything.platform.PacketContext;

import java.util.function.Supplier;

public class ClientPayloadHandler {
    public static void handleBlockEntityData(UncraftingTableDataPayload payload, Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            Level level = minecraft.level;
            Screen screen = minecraft.screen;

            if (level != null && screen instanceof AbstractUncraftingScreen<? extends AbstractUncraftingTableBE, ? extends AbstractUncraftingMenu<? extends AbstractUncraftingTableBE>> uncraftingTableScreen) {
                if (level.getBlockEntity(payload.blockPos()) instanceof AbstractUncraftingTableBE) {
                    uncraftingTableScreen.updateFromBlockEntity(payload.recipes(), payload.size(), payload.shouldSendPacket());
                }
            }
        });
    }

    public static ResponseConfigPayload payloadFromServer;

    public static void handleConfigSync(ResponseConfigPayload payload, Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> {
            payloadFromServer = payload;
        });
    }

    public static void handleRecipeSelectionRequest(UncraftingRecipeSelectionRequestPayload payload, Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            Level level = minecraft.level;
            Screen screen = minecraft.screen;

            if (level != null && screen instanceof AbstractUncraftingScreen<? extends AbstractUncraftingTableBE, ? extends AbstractUncraftingMenu<? extends AbstractUncraftingTableBE>> uncraftingTableScreen) {
                uncraftingTableScreen.getRecipeSelection();
            }
        });
    }
}
