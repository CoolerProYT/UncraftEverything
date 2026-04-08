package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.blockentity.custom.AbstractUncraftingTableBE;
import com.coolerpromc.uncrafteverything.platform.util.PayloadContext;
import com.coolerpromc.uncrafteverything.screen.custom.AbstractUncraftingMenu;
import com.coolerpromc.uncrafteverything.screen.custom.AbstractUncraftingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;

public record ClientBoundUncraftingRecipeSelectionRequestPayload() implements CustomPacketPayload {
    public static final Type<ClientBoundUncraftingRecipeSelectionRequestPayload> TYPE = new Type<>(Constants.id("uncrafting_recipe_selection_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundUncraftingRecipeSelectionRequestPayload> STREAM_CODEC = StreamCodec.unit(new ClientBoundUncraftingRecipeSelectionRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class ClientHandler{
        public static void handle(ClientBoundUncraftingRecipeSelectionRequestPayload payload, PayloadContext context){
            context.execute(() -> {
                Minecraft minecraft = Minecraft.getInstance();
                Level world = minecraft.level;
                Screen screen = minecraft.gui.screen();

                if (world != null && screen instanceof AbstractUncraftingScreen<? extends AbstractUncraftingTableBE, ? extends AbstractUncraftingMenu<? extends AbstractUncraftingTableBE>> uncraftingTableScreen) {
                    uncraftingTableScreen.getRecipeSelection();
                }
            });
        }
    }
}