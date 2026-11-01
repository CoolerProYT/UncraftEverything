package com.coolerpromc.uncrafteverything.platform;

import com.coolerpromc.uncrafteverything.platform.services.IPlatformHelper;

import java.nio.file.Path;

public final class FabricPlatformHelper implements IPlatformHelper {
    public boolean isModLoaded(String id) {
        return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(id);
    }

    public boolean isClient() {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType()
                == net.fabricmc.api.EnvType.CLIENT;
    }

    public Path configPath() {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir();
    }

    public String commonConfigName() {
        return "uncrafteverything_common.toml";
    }
}
