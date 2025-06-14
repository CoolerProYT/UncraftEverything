package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class UncraftingTableCraftButtonClickPayload{
    public static final Identifier ID = new Identifier(UncraftEverything.MODID, "uncrafting_table_craft_button_click");
    private final BlockPos blockPos;
    private final boolean hasShiftDown;

    public UncraftingTableCraftButtonClickPayload(BlockPos blockPos, boolean hasShiftDown) {
        this.blockPos = blockPos;
        this.hasShiftDown = hasShiftDown;
    }

    public static final Codec<UncraftingTableCraftButtonClickPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("blockPos").forGetter(UncraftingTableCraftButtonClickPayload::blockPos),
            Codec.BOOL.fieldOf("hasShiftDown").forGetter(UncraftingTableCraftButtonClickPayload::hasShiftDown)
    ).apply(instance, UncraftingTableCraftButtonClickPayload::new));

    public BlockPos blockPos() {
        return blockPos;
    }

    public boolean hasShiftDown() {
        return hasShiftDown;
    }
}