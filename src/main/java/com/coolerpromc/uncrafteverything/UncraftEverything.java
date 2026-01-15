package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.command.ModCommands;
import com.coolerpromc.uncrafteverything.config.FTBQuestProgressionConfig;
import com.coolerpromc.uncrafteverything.config.PerItemExpCostConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingClientConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.item.UECreativeTab;
import com.coolerpromc.uncrafteverything.item.UEItems;
import com.coolerpromc.uncrafteverything.networking.*;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import com.coolerpromc.uncrafteverything.screen.custom.AutoUncraftingTableScreen;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

@Mod(UncraftEverything.MODID)
public class UncraftEverything
{
    public static final String MODID = "uncrafteverything";

    public UncraftEverything()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        UEBlocks.register(modEventBus);
        UEItems.register(modEventBus);
        UECreativeTab.register(modEventBus);
        UEBlockEntities.register(modEventBus);
        UEMenuTypes.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, UncraftEverythingConfig.CONFIG_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, UncraftEverythingClientConfig.CONFIG_SPEC);
        PerItemExpCostConfig.load();
        FTBQuestProgressionConfig.load();
        PerItemExpCostConfig.startWatcher();
        FTBQuestProgressionConfig.startWatcher();
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            UncraftingTableDataPayload.register();
            UncraftingRecipeSelectionPayload.register();
            UncraftingTableCraftButtonClickPayload.register();
            RequestConfigPayload.register();
            ResponseConfigPayload.register();
            UEConfigPayload.register();
            UEExpPayload.register();
            UncraftingRecipeSelectionRequestPayload.register();
            UncraftingPageChangePayload.register();
            UEProgressionPayload.register();
            AmountToAddPayload.register();
            ClientConfigSyncPayload.register();
            CloseMenuPayload.register();
            ExpTransferPayload.register();
            SelectedIndexSyncPayload.register();
            TypeChangePayload.register();
        });
    }
    
    @SubscribeEvent
    public void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onOnDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            ServerPlayer player = event.getPlayer();
            UncraftEverythingConfig config = UncraftEverythingConfig.CONFIG;
            ResponseConfigPayload configPayload = new ResponseConfigPayload(
                    config.restrictionType.get(),
                    (List<String>) config.restrictions.get(),
                    config.allowEnchantedItems.get(),
                    config.experienceType.get(),
                    config.experience.get(),
                    config.allowUnSmithing.get(),
                    config.allowDamaged.get(),
                    config.preventModdedIngredientsFromVanillaItems.get(),
                    PerItemExpCostConfig.getPerItemExp(),
                    (List<String>) config.restrictedModIngredients.get(),
                    FTBQuestProgressionConfig.getProgressionMap(),
                    config.enableProgression.get(),
                    config.onlyAllowDefinedProgression.get(),
                    config.outputEnchantedBook.get(),
                    config.prioritizeVanillaIngredientRecipe.get()
            );
            ResponseConfigPayload.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), configPayload);
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        PerItemExpCostConfig.stopWatcher();
        FTBQuestProgressionConfig.stopWatcher();
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(UEMenuTypes.UNCRAFTING_TABLE_MENU.get(), UncraftingTableScreen::new);
                MenuScreens.register(UEMenuTypes.AUTO_UNCRAFTING_TABLE_MENU.get(), AutoUncraftingTableScreen::new);
            });
        }
    }
}
