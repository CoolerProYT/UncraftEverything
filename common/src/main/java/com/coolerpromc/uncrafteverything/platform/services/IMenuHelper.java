package com.coolerpromc.uncrafteverything.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

public interface IMenuHelper {
    void openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos);
}
