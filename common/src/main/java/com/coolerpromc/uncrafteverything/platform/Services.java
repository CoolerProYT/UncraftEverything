package com.coolerpromc.uncrafteverything.platform;

import com.coolerpromc.uncrafteverything.platform.services.*;

import java.util.ServiceLoader;

public final class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IRegistryHelper REGISTRY = load(IRegistryHelper.class);
    public static final INetworkHelper NETWORK = load(INetworkHelper.class);
    public static final IMenuHelper MENU = load(IMenuHelper.class);
    public static final IIngredientHelper INGREDIENT = load(IIngredientHelper.class);

    private Services() {}

    private static <T> T load(Class<T> type) {
        return ServiceLoader.load(type, Services.class.getClassLoader())
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Missing platform service: " + type.getName()));
    }
}
