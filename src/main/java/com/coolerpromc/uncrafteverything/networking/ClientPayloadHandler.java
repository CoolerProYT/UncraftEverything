package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
    public static void handleBlockEntityData(UncraftingTableDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            Level level = minecraft.level;
            Screen screen = minecraft.screen;

            if (level != null && screen instanceof UncraftingTableScreen uncraftingTableScreen) {
                if (level.getBlockEntity(payload.blockPos()) instanceof UncraftingTableBlockEntity blockEntity) {
                    uncraftingTableScreen.updateFromBlockEntity(payload.recipes());
                }
            }
        });
    }

    public static ResponseConfigPayload payloadFromServer;

    public static void handleConfigSync(ResponseConfigPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            payloadFromServer = payload;
        });
    }
}
