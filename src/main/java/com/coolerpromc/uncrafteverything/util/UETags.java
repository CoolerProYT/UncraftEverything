package com.coolerpromc.uncrafteverything.util;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class UETags {
    public static class Items{
        public static final Tag<Item> SHULKER_BOXES = modTag("shulker_boxes");

        private static Tag<Item> modTag(String name){
            return TagRegistry.item(new Identifier(UncraftEverything.MODID, name));
        }
    }
}
