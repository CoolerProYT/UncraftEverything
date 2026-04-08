package com.coolerpromc.uncrafteverything.platform.util;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public record FabricServerPayloadContext(ServerPlayNetworking.Context context) implements PayloadContext {
    @Override
    public Player player() {
        return context.player();
    }

    @Override
    public Level level() {
        return context.player().level();
    }

    @Override
    public void execute(Runnable runnable) {
        context.server().execute(runnable);
    }

    @Override
    public void disconnect(Component reason) {
        context.responseSender().disconnect(reason);
    }
}
