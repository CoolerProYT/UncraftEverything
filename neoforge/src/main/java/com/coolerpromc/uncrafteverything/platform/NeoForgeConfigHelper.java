package com.coolerpromc.uncrafteverything.platform;

import com.coolerpromc.uncrafteverything.config.NeoForgeUncraftEverythingClientConfig;
import com.coolerpromc.uncrafteverything.platform.services.IConfigHelper;

public class NeoForgeConfigHelper implements IConfigHelper {
    @Override
    public void updateClientConfig() {
        NeoForgeUncraftEverythingClientConfig.updateConfig();
    }
}
