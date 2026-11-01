package com.coolerpromc.uncrafteverything.platform;

import com.coolerpromc.uncrafteverything.platform.services.INetworkHelper;

import net.minecraft.resources.ResourceLocation;

public final class FabricNetworkHelper implements INetworkHelper {
    public Channel createChannel(ResourceLocation id) {
        return new FabricChannel(id);
    }
}
