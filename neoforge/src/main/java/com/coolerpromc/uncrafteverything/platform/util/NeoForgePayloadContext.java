package com.coolerpromc.uncrafteverything.platform.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record NeoForgePayloadContext(IPayloadContext context) implements PayloadContext{
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
        context.enqueueWork(runnable);
    }

    @Override
    public void disconnect(Component reason) {
        context.disconnect(reason);
    }
}
