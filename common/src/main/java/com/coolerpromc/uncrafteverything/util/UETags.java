package com.coolerpromc.uncrafteverything.util;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class UETags {
    public static class Items{
        public static final TagKey<Item> SHULKER_BOXES = modTag("shulker_boxes");

        private static TagKey<Item> modTag(String name){
            return TagKey.create(net.minecraft.core.registries.Registries.ITEM, new ResourceLocation(UncraftEverything.MODID, name));
        }
    }
}
