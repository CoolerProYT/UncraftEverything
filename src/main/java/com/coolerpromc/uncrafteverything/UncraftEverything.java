package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.config.PerItemExpCostConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.item.UECreativeTab;
import com.coolerpromc.uncrafteverything.item.UEItems;
import com.coolerpromc.uncrafteverything.networking.*;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(UncraftEverything.MODID)
public class UncraftEverything
{
    public static final String MODID = "uncrafteverything";

    public UncraftEverything(FMLJavaModLoadingContext context)
    {
        BusGroup modEventBus = context.getModBusGroup();

        UEBlocks.register(modEventBus);
        UEItems.register(modEventBus);
        UECreativeTab.register(modEventBus);
        UEBlockEntities.register(modEventBus);
        UEMenuTypes.register(modEventBus);

        UncraftingTableDataPayload.register(modEventBus);
        UncraftingTableCraftButtonClickPayload.register(modEventBus);
        UncraftingRecipeSelectionPayload.register(modEventBus);
        RequestConfigPayload.register(modEventBus);
        ResponseConfigPayload.register(modEventBus);
        UEConfigPayload.register(modEventBus);
        UEExpPayload.register(modEventBus);
        UncraftingRecipeSelectionRequestPayload.register(modEventBus);

        context.registerConfig(ModConfig.Type.COMMON, UncraftEverythingConfig.CONFIG_SPEC);
        PerItemExpCostConfig.load();
        PerItemExpCostConfig.startWatcher();
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(UEMenuTypes.UNCRAFTING_TABLE_MENU.get(), UncraftingTableScreen::new);
            });
        }
    }
}
