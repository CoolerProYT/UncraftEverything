package com.coolerpromc.uncrafteverything.item;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.block.UEBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class UECreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MOD_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, UncraftEverything.MODID);

    public static final RegistryObject<CreativeModeTab> PRODUCTIVE_SLIMES_TAB = CREATIVE_MOD_TABS.register("uncrafteverything",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(UEBlocks.UNCRAFTING_TABLE.get()))
                    .title(Component.translatable("creativetab.uncrafteverything"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(UEBlocks.UNCRAFTING_TABLE.get());
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MOD_TABS.register(eventBus);
    }
}