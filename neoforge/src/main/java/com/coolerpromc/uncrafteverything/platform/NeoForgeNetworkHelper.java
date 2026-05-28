package com.coolerpromc.uncrafteverything.platform;

import com.coolerpromc.uncrafteverything.platform.services.INetworkHelper;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgeNetworkHelper implements INetworkHelper {
    @Override
    public <T extends CustomPacketPayload> void sendToPlayer(ServerPlayer player, T packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    @Override
    public <T extends CustomPacketPayload> void sendToAllPlayer(T packet) {
        PacketDistributor.sendToAllPlayers(packet);
    }

    @Override
    public <T extends CustomPacketPayload> void sendToServer(T packet) {
        ClientPacketDistributor.sendToServer(packet);
    }
}
