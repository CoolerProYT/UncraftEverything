package com.coolerpromc.uncrafteverything.platform.services;

import com.coolerpromc.uncrafteverything.platform.PacketContext;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.*;

public interface INetworkHelper {
    Channel createChannel(ResourceLocation id);

    interface Channel {
        <T> void registerMessage(
                int discriminator,
                Class<T> type,
                BiConsumer<T, FriendlyByteBuf> encoder,
                Function<FriendlyByteBuf, T> decoder,
                BiConsumer<T, Supplier<PacketContext>> handler);

        void send(Supplier<ServerPlayer> recipient, Object payload);

        void sendToServer(Object payload);
    }
}
