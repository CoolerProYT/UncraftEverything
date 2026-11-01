package com.coolerpromc.uncrafteverything.item;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.platform.Services;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

public final class UECreativeTab {
    public static void register() {
        Services.REGISTRY.registerCreativeTab(
                "uncrafteverything",
                () ->
                        Services.REGISTRY
                                .creativeTabBuilder()
                                .icon(() -> new ItemStack(UEBlocks.UNCRAFTING_TABLE.get()))
                                .title(Component.translatable("creativetab.uncrafteverything"))
                                .displayItems(
                                        (parameters, output) -> {
                                            output.accept(UEBlocks.UNCRAFTING_TABLE.get());
                                            output.accept(UEBlocks.AUTO_UNCRAFTING_TABLE.get());
                                        })
                                .build());
    }
}
