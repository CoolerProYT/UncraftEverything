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
import com.coolerpromc.uncrafteverything.networking.ResponseConfigPayload;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import com.coolerpromc.uncrafteverything.util.UncraftDebugLogger;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@Mod(UncraftEverything.MODID)
public class UncraftEverything
{
    public static final String MODID = "uncrafteverything";

    public UncraftEverything(IEventBus modEventBus, ModContainer modContainer)
    {
        UEBlocks.register(modEventBus);
        UEItems.register(modEventBus);
        UECreativeTab.register(modEventBus);
        UEBlockEntities.register(modEventBus);
        UEMenuTypes.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, UncraftEverythingConfig.CONFIG_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, UncraftEverythingClientConfig.CONFIG_SPEC);
        PerItemExpCostConfig.load();
        FTBQuestProgressionConfig.load();
        PerItemExpCostConfig.startWatcher();
        FTBQuestProgressionConfig.startWatcher();

        UncraftDebugLogger.init(FMLPaths.GAMEDIR.get().resolve("logs"));
    }

    @SubscribeEvent
    public void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onOnDatapackSync(OnDatapackSyncEvent event) {
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
                    config.outputEnchantedBook.get(),
                    config.prioritizeVanillaIngredientRecipe.get()
            );
            PacketDistributor.sendToPlayer(player, configPayload);
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        PerItemExpCostConfig.stopWatcher();
        FTBQuestProgressionConfig.stopWatcher();
        UncraftDebugLogger.close();
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
    }
}
