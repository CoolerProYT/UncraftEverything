package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record UncraftingTableCraftButtonClickPayload(BlockPos blockPos){
    public static final Identifier ID = new Identifier(UncraftEverything.MODID, "uncrafting_table_craft_button_click");

    public static final Codec<UncraftingTableCraftButtonClickPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("blockPos").forGetter(UncraftingTableCraftButtonClickPayload::blockPos)
    ).apply(instance, UncraftingTableCraftButtonClickPayload::new));
}