package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.item.UEItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(UncraftEverything.MODID)
public class UncraftEverything
{
    public static final String MODID = "uncrafteverything";
    public UncraftEverything(IEventBus modEventBus, ModContainer modContainer)
    {
        NeoForge.EVENT_BUS.register(this);

        UEBlocks.register(modEventBus);
        UEItems.register(modEventBus);
        UEBlockEntities.register(modEventBus);
    }
}
