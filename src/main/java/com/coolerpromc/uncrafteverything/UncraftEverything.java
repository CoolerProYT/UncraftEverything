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
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

@Mod(UncraftEverything.MODID)
public class UncraftEverything
{
    public static final String MODID = "uncrafteverything";
    public static final Channel<CustomPacketPayload> CHANNEL = ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(MODID, "channel_registration"))
            .payloadChannel()
            .play()
            .bidirectional()
            .add(ExpTransferPayload.TYPE, ExpTransferPayload.STREAM_CODEC, ServerPayloadHandler::handleExpTransfer)
            .add(SelectedIndexSyncPayload.TYPE, SelectedIndexSyncPayload.STREAM_CODEC, ServerPayloadHandler::handleIndexSync)
            .add(AmountToAddPayload.TYPE, AmountToAddPayload.STREAM_CODEC, ServerPayloadHandler::handleAmountChange)
            .add(TypeChangePayload.TYPE, TypeChangePayload.STREAM_CODEC, ServerPayloadHandler::handleTypeChange)
            .add(CloseMenuPayload.TYPE, CloseMenuPayload.STREAM_CODEC, ServerPayloadHandler::handleCloseMenu)
            .build();

    public UncraftEverything(FMLJavaModLoadingContext context)
    {
        BusGroup modEventBus = context.getModBusGroup();
        RegisterClientCommandsEvent.BUS.addListener(UncraftEverything::onRegisterClientCommands);
        ServerStoppingEvent.BUS.addListener(UncraftEverything::onServerStopping);
        OnDatapackSyncEvent.BUS.addListener(UncraftEverything::onDatapackSync);

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
        UncraftingPageChangePayload.register(modEventBus);
        UEProgressionPayload.register(modEventBus);

        context.registerConfig(ModConfig.Type.COMMON, UncraftEverythingConfig.CONFIG_SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, UncraftEverythingClientConfig.CONFIG_SPEC);
        PerItemExpCostConfig.load();
        FTBQuestProgressionConfig.load();
        PerItemExpCostConfig.startWatcher();
        FTBQuestProgressionConfig.startWatcher();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        PerItemExpCostConfig.stopWatcher();
        FTBQuestProgressionConfig.stopWatcher();
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
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
                    config.outputEnchantedBook.get()
            );
            ResponseConfigPayload.INSTANCE.send(configPayload, PacketDistributor.PLAYER.with(player));
        }
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
