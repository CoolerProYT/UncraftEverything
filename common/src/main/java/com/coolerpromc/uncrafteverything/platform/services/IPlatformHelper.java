package com.coolerpromc.uncrafteverything.platform.services;

import java.nio.file.Path;

public interface IPlatformHelper {
    String getPlatformName();
    boolean isModLoaded(String modId);
    boolean isDevelopmentEnvironment();
    Path configPath();
    boolean isClient();

    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }
}