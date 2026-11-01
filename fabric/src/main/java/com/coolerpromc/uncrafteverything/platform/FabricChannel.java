package com.coolerpromc.uncrafteverything.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
import java.util.function.*;

/**
 * Loader-owned transport; packet records and codecs remain identical to the current implementation.
 */
public final class FabricChannel
        implements com.coolerpromc.uncrafteverything.platform.services.INetworkHelper.Channel {
    private static final Set<String> CLIENT_BOUND =
            Set.of(
                    "uncrafting_table_data",
                    "response_config",
                    "uncrafting_table_recipe_selection_request");
    private final ResourceLocation id;
    private BiConsumer<Object, FriendlyByteBuf> encoder;

    public FabricChannel(ResourceLocation id) {
        this.id = id;
    }

    @SuppressWarnings("unchecked")
    public <T> void registerMessage(
            int discriminator,
            Class<T> type,
            BiConsumer<T, FriendlyByteBuf> encode,
            Function<FriendlyByteBuf, T> decode,
            BiConsumer<T, Supplier<PacketContext>> handler) {
        encoder = (value, buffer) -> encode.accept((T) value, buffer);
        if (CLIENT_BOUND.contains(id.getPath())) {
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
                Client.register(id, decode, handler);
        } else
            ServerPlayNetworking.registerGlobalReceiver(
                    id,
                    (server, player, network, buffer, response) -> {
                        T payload = decode.apply(buffer);
                        PacketContext context =
                                new PacketContext(player, server, network::disconnect);
                        handler.accept(payload, () -> context);
                    });
    }

    public void send(Supplier<ServerPlayer> recipient, Object payload) {
        ServerPlayer player = recipient.get();
        if (player == null || player.connection == null) return;
        FriendlyByteBuf buffer = PacketByteBufs.create();
        encoder.accept(payload, buffer);
        ServerPlayNetworking.send(player, id, buffer);
    }

    public void sendToServer(Object payload) {
        Client.send(id, payload, encoder);
    }

    private static final class Client {
        static <T> void register(
                ResourceLocation id,
                Function<FriendlyByteBuf, T> decode,
                BiConsumer<T, Supplier<PacketContext>> handler) {
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
                    .registerGlobalReceiver(
                            id,
                            (client, network, buffer, response) -> {
                                T payload = decode.apply(buffer);
                                PacketContext context =
                                        new PacketContext(
                                                null, client, network.getConnection()::disconnect);
                                handler.accept(payload, () -> context);
                            });
        }

        static void send(
                ResourceLocation id, Object payload, BiConsumer<Object, FriendlyByteBuf> encoder) {
            FriendlyByteBuf buffer = PacketByteBufs.create();
            encoder.accept(payload, buffer);
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(id, buffer);
        }
    }
}
