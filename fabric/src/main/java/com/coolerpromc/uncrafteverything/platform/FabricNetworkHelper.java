package com.coolerpromc.uncrafteverything.platform;

import com.coolerpromc.uncrafteverything.FabricUncraftEverything;
import com.coolerpromc.uncrafteverything.platform.services.INetworkHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public class FabricNetworkHelper implements INetworkHelper {
    @Override
    public <T extends CustomPacketPayload> void sendToPlayer(ServerPlayer player, T packet) {
        ServerPlayNetworking.send(player, packet);
    }

    @Override
    public <T extends CustomPacketPayload> void sendToAllPlayer(T packet) {
        MinecraftServer server = FabricUncraftEverything.MINECRAFT_SERVER;
        if (server != null){
            PlayerLookup.all(server).forEach(p -> sendToPlayer(p, packet));
        }
    }

    @Override
    public <T extends CustomPacketPayload> void sendToServer(T packet) {
        ClientPlayNetworking.send(packet);
    }
}
