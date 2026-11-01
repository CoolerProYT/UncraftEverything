package com.coolerpromc.uncrafteverything.platform;

import com.coolerpromc.uncrafteverything.platform.services.IMenuHelper;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkHooks;

public final class ForgeMenuHelper implements IMenuHelper {
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
        NetworkHooks.openScreen(player, (MenuProvider) table, table.getBlockPos());
    }
}
