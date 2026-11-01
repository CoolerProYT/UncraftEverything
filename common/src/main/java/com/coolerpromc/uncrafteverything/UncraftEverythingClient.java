package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.config.UncraftEverythingClientConfig;
import com.coolerpromc.uncrafteverything.platform.Services;
import com.coolerpromc.uncrafteverything.screen.custom.*;

public final class UncraftEverythingClient {
    public static void init() {
        UncraftEverythingClientConfig.CONFIG_SPEC.load(
                Services.PLATFORM.configPath().resolve("uncrafteverything-client.toml"),
                UncraftEverythingClientConfig.CONFIG::onChanged);
        Services.MENU.registerScreens();
    }
}
