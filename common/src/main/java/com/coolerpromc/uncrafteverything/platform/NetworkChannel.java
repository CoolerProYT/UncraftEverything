package com.coolerpromc.uncrafteverything.platform;

import com.coolerpromc.uncrafteverything.platform.services.INetworkHelper;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.*;

public final class NetworkChannel {
    private final INetworkHelper.Channel channel;

    public NetworkChannel(ResourceLocation id) {
        channel = Services.NETWORK.createChannel(id);
    }

    public <T> void registerMessage(
            int discriminator,
            Class<T> type,
            BiConsumer<T, FriendlyByteBuf> encoder,
            Function<FriendlyByteBuf, T> decoder,
            BiConsumer<T, Supplier<PacketContext>> handler) {
        channel.registerMessage(discriminator, type, encoder, decoder, handler);
    }

    public void send(Supplier<ServerPlayer> recipient, Object payload) {
        channel.send(recipient, payload);
    }

    public void sendToServer(Object payload) {
        channel.sendToServer(payload);
    }
}
