package com.coolerpromc.uncrafteverything.platform.services;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IMenuHelper {
    void registerScreens();

    void openMenu(ServerPlayer player, BlockEntity table);
}
