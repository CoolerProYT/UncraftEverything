package com.coolerpromc.uncrafteverything.platform;

import com.coolerpromc.uncrafteverything.platform.services.IMenuHelper;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class FabricMenuHelper implements IMenuHelper {
    public void registerScreens() {
        Client.register();
    }

    private static final class Client {
        static void register() {
            net.minecraft.client.gui.screens.MenuScreens.register(
                    com.coolerpromc.uncrafteverything.screen.UEMenuTypes.UNCRAFTING_TABLE_MENU
                            .get(),
                    com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen::new);
            net.minecraft.client.gui.screens.MenuScreens.register(
                    com.coolerpromc.uncrafteverything.screen.UEMenuTypes.AUTO_UNCRAFTING_TABLE_MENU
                            .get(),
                    com.coolerpromc.uncrafteverything.screen.custom.AutoUncraftingTableScreen::new);
        }
    }

    public void openMenu(ServerPlayer player, BlockEntity table) {
        MenuProvider provider = (MenuProvider) table;
        player.openMenu(
                new ExtendedScreenHandlerFactory() {
                    public Component getDisplayName() {
                        return provider.getDisplayName();
                    }

                    public AbstractContainerMenu createMenu(
                            int id, Inventory inventory, Player owner) {
                        return provider.createMenu(id, inventory, owner);
                    }

                    public void writeScreenOpeningData(ServerPlayer owner, FriendlyByteBuf data) {
                        data.writeBlockPos(table.getBlockPos());
                    }
                });
    }
}
