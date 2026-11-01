package com.coolerpromc.uncrafteverything.platform;

import com.coolerpromc.uncrafteverything.platform.services.INetworkHelper;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.*;

public final class ForgeNetworkHelper implements INetworkHelper {
    public Channel createChannel(ResourceLocation id) {
        return new ForgeChannel(id);
    }

    private static final class ForgeChannel implements Channel {
        private final SimpleChannel channel;

        ForgeChannel(ResourceLocation id) {
            String version = id.getPath().equals("uncrafting_table_recipe_selection") ? "2" : "1";
            channel =
                    NetworkRegistry.newSimpleChannel(
                            id, () -> version, version::equals, version::equals);
        }

        public <T> void registerMessage(
                int discriminator,
                Class<T> type,
                BiConsumer<T, FriendlyByteBuf> encoder,
                Function<FriendlyByteBuf, T> decoder,
                BiConsumer<T, Supplier<PacketContext>> handler) {
            channel.registerMessage(
                    discriminator,
                    type,
                    encoder,
                    decoder,
                    (payload, supplier) -> {
                        NetworkEvent.Context context = supplier.get();
                        PacketContext common =
                                new PacketContext(
                                        context.getSender(),
                                        action -> context.enqueueWork(action),
                                        context.getNetworkManager()::disconnect);
                        handler.accept(payload, () -> common);
                        context.setPacketHandled(true);
                    });
        }

        public void send(Supplier<ServerPlayer> recipient, Object payload) {
            ServerPlayer player = recipient.get();
            if (player != null && player.connection != null)
                channel.send(PacketDistributor.PLAYER.with(() -> player), payload);
        }

        public void sendToServer(Object payload) {
            channel.sendToServer(payload);
        }
    }
}
