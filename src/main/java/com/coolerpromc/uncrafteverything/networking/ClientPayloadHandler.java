package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.blockentity.custom.AbstractUncraftingTableBE;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.screen.custom.AbstractUncraftingMenu;
import com.coolerpromc.uncrafteverything.screen.custom.AbstractUncraftingScreen;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class ClientPayloadHandler {
    public static void handleBlockEntityData(UncraftingTableDataPayload payload, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            Level level = minecraft.level;
            Screen screen = minecraft.screen;

            if (level != null && screen instanceof AbstractUncraftingScreen<? extends AbstractUncraftingTableBE, ? extends AbstractUncraftingMenu<? extends AbstractUncraftingTableBE>> uncraftingTableScreen) {
                if (level.getBlockEntity(payload.blockPos()) instanceof AbstractUncraftingTableBE blockEntity) {
                    uncraftingTableScreen.updateFromBlockEntity(payload.recipes(), payload.size(), payload.shouldSendPacket());
                }
            }
        });
        context.setPacketHandled(true);
    }

    public static ResponseConfigPayload payloadFromServer;

    public static void handleConfigSync(ResponseConfigPayload payload, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            payloadFromServer = payload;
        });
        context.setPacketHandled(true);
    }

    public static void handleRecipeSelectionRequest(UncraftingRecipeSelectionRequestPayload payload, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            Level level = minecraft.level;
            Screen screen = minecraft.screen;

            if (level != null && screen instanceof AbstractUncraftingScreen<? extends AbstractUncraftingTableBE, ? extends AbstractUncraftingMenu<? extends AbstractUncraftingTableBE>> uncraftingTableScreen) {
                uncraftingTableScreen.getRecipeSelection();
            }
        });
        context.setPacketHandled(true);
    }
}
