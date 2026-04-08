package com.coolerpromc.uncrafteverything.platform;

import com.coolerpromc.uncrafteverything.config.FabricUncraftEverythingClientConfig;
import com.coolerpromc.uncrafteverything.platform.services.IConfigHelper;

public class FabricConfigHelper implements IConfigHelper {
    @Override
    public void updateClientConfig() {
        FabricUncraftEverythingClientConfig.save();
    }
}
