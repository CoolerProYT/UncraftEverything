package com.coolerpromc.uncrafteverything.item;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.block.UEBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class UECreativeTab {
    public static final ItemGroup UNCRAFTEVERYTHING_TAB = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(UncraftEverything.MODID, "uncrafteverything"),
            FabricItemGroup.builder().icon(() -> new ItemStack(UEBlocks.UNCRAFTING_TABLE))
                    .displayName(Text.translatable("creativetab.uncrafteverything"))
                    .entries((displayContext, entries) -> {
                        entries.add(UEBlocks.UNCRAFTING_TABLE);
                    }).build());

    public static void register() {

    }
}
