package com.coolerpromc.uncrafteverything.platform.services;

import java.nio.file.Path;

public interface IPlatformHelper {
    boolean isModLoaded(String id);

    boolean isClient();

    Path configPath();

    String commonConfigName();
}
