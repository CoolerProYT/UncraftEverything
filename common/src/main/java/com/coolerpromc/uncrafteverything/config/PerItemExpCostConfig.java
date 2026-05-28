package com.coolerpromc.uncrafteverything.config;

import com.coolerpromc.coolerconfig.config.ConfigBuilder;
import com.coolerpromc.coolerconfig.config.ConfigFormat;
import com.coolerpromc.coolerconfig.config.ConfigSpec;
import com.coolerpromc.coolerconfig.config.ConfigValue;
import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.platform.Services;
import com.coolerpromc.uncrafteverything.platform.util.PayloadContext;

import java.util.Map;

public class PerItemExpCostConfig {
    public static final PerItemExpCostConfig CONFIG = new PerItemExpCostConfig();
    private final ConfigSpec CONFIG_SPEC;

    public final ConfigValue<Map<String, Integer>> perItemExp;

    public static void init(){}

    private PerItemExpCostConfig(){
        ConfigBuilder builder = ConfigSpec.builder(Constants.MODID, ConfigFormat.JSON)
                .suffix("exp");

        perItemExp = builder.defineMap("PerItemExp", Map.of("minecraft:netherite_*", 2), "");

        CONFIG_SPEC = builder.watchForChanges().build();

        if (!Services.PLATFORM.isClient()){
            CONFIG_SPEC.addReloadListener(() -> Services.NETWORK.sendToAllPlayer(PayloadContext.SYNC_CONFIG));
        }
    }

    public void save(){
        CONFIG_SPEC.save();
    }

    public Map<String, Integer> getPerItemExp() {
        return perItemExp.get();
    }
}