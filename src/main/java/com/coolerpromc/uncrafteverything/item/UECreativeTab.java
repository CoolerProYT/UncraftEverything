package com.coolerpromc.uncrafteverything.item;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.block.UEBlocks;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class UECreativeTab {
    public static final CreativeModeTab UNCRAFTEVERYTHING_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(UncraftEverything.MODID, "uncrafteverything"),
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(UEBlocks.UNCRAFTING_TABLE))
                    .title(Component.translatable("creativetab.uncrafteverything"))
                    .displayItems((_, entries) -> {
                        entries.accept(UEBlocks.UNCRAFTING_TABLE);
                        entries.accept(UEBlocks.AUTO_UNCRAFTING_TABLE);
                    }).build());

    public static void register() {

    }
}
