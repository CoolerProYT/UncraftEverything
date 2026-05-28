package com.coolerpromc.uncrafteverything.config;

import com.coolerpromc.coolerconfig.config.ConfigBuilder;
import com.coolerpromc.coolerconfig.config.ConfigFormat;
import com.coolerpromc.coolerconfig.config.ConfigSpec;
import com.coolerpromc.coolerconfig.config.ConfigValue;
import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.platform.Services;
import com.coolerpromc.uncrafteverything.platform.util.PayloadContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class FTBQuestProgressionConfig {
    public static final FTBQuestProgressionConfig CONFIG = new FTBQuestProgressionConfig();
    private final ConfigSpec CONFIG_SPEC;

    public final ConfigValue<Map<String, String>> progressionMap;

    public static void init(){}

    public FTBQuestProgressionConfig(){
        ConfigBuilder builder = ConfigSpec.builder(Constants.MODID, ConfigFormat.JSON)
                .suffix("ftbquest-progression");

        progressionMap = builder.defineMap("Progressions", Map.of("item_id/item_tags/item_id_wildcard", "ftb_quest_id_here"), "");

        CONFIG_SPEC = builder.watchForChanges().build();

        if (!Services.PLATFORM.isClient()){
            CONFIG_SPEC.addReloadListener(() -> Services.NETWORK.sendToAllPlayer(PayloadContext.SYNC_CONFIG));
        }
    }

    public void save(){
        CONFIG_SPEC.save();
    }

    public Map<String, String> getProgressionMap() {
        return progressionMap.get();
    }

    public String getQuestId(ItemStack itemStack) {
        Map<String, String> questMap = getProgressionMap();
        String itemId = getItemLocation(itemStack).toString();

        for (Map.Entry<String, String> entry : questMap.entrySet()) {
            String key = entry.getKey();

            if (key.startsWith("#")) {
                String tagName = key.substring(1);
                Optional<TagKey<Item>> tagKey = UncraftEverythingConfig.CONFIG.tryParseTagKey(tagName);
                if (tagKey.isPresent() && itemStack.is(tagKey.get())) {
                    return entry.getValue();
                }
            } else if (key.contains("*")) {
                String regex = key.replace("*", ".*");
                if (Pattern.matches(regex, itemId)) {
                    return entry.getValue();
                }
            } else if (key.equals(itemId)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static Identifier getItemLocation(ItemStack itemStack){
        return BuiltInRegistries.ITEM.getKey(itemStack.getItem());
    }
}