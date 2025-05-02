package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
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

            if (level != null && screen instanceof UncraftingTableScreen uncraftingTableScreen) {
                if (level.getBlockEntity(payload.blockPos()) instanceof UncraftingTableBlockEntity blockEntity) {
                    uncraftingTableScreen.updateFromBlockEntity(payload.recipes());
                }
            }
        });
        context.setPacketHandled(true);
    }
}
