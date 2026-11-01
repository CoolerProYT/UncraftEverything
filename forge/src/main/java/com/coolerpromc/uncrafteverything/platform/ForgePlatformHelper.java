package com.coolerpromc.uncrafteverything.platform;

import com.coolerpromc.uncrafteverything.platform.services.IPlatformHelper;

import java.nio.file.Path;

public final class ForgePlatformHelper implements IPlatformHelper {
    public boolean isModLoaded(String id) {
        return net.minecraftforge.fml.ModList.get() != null
                && net.minecraftforge.fml.ModList.get().isLoaded(id);
    }

    public boolean isClient() {
        return net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient();
    }

    public Path configPath() {
        return net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get();
    }

    public String commonConfigName() {
        return "uncrafteverything-common.toml";
    }
}
