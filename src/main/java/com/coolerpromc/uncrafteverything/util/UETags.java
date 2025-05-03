package com.coolerpromc.uncrafteverything.util;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

public class UETags {
    public static class Items{
        public static final Tags.IOptionalNamedTag<Item> SHULKER_BOXES = modTag("shulker_boxes");

        private static Tags.IOptionalNamedTag<Item> modTag(String name){
            return ItemTags.createOptional(new ResourceLocation(UncraftEverything.MODID, name));
        }
    }
}
