package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.command.ModCommands;
import com.coolerpromc.uncrafteverything.config.FTBQuestProgressionConfig;
import com.coolerpromc.uncrafteverything.config.NeoForgeUncraftEverythingClientConfig;
import com.coolerpromc.uncrafteverything.config.NeoForgeUncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.config.PerItemExpCostConfig;
import com.coolerpromc.uncrafteverything.networking.*;
import com.coolerpromc.uncrafteverything.platform.NeoForgeRegistryHelper;
import com.coolerpromc.uncrafteverything.platform.util.NeoForgePayloadContext;
import com.coolerpromc.uncrafteverything.platform.util.PayloadContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Constants.MODID)
@EventBusSubscriber(modid = Constants.MODID)
public class UncraftEverything
{
    public UncraftEverything(IEventBus modEventBus, ModContainer modContainer)
    {
        CommonClass.init();
        NeoForgeRegistryHelper.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, NeoForgeUncraftEverythingConfig.CONFIG_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, NeoForgeUncraftEverythingClientConfig.CONFIG_SPEC);

        modEventBus.addListener(NeoForgeUncraftEverythingConfig::onLoading);
        modEventBus.addListener(NeoForgeUncraftEverythingConfig::onReloading);
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onOnDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(RecipeType.CRAFTING, RecipeType.SMITHING);
        if (event.getPlayer() instanceof ServerPlayer player) {
            PayloadContext.syncConfig(player);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        PerItemExpCostConfig.stopWatcher();
        FTBQuestProgressionConfig.stopWatcher();
    }

    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(ServerBoundUncraftingTableCraftButtonClickPayload.TYPE, ServerBoundUncraftingTableCraftButtonClickPayload.STREAM_CODEC, (payload, context) -> payload.handle(new NeoForgePayloadContext(context)));
        registrar.playToClient(ClientBoundUncraftingTableDataPayload.TYPE, ClientBoundUncraftingTableDataPayload.STREAM_CODEC);
        registrar.playToServer(ServerBoundUncraftingRecipeSelectionPayload.TYPE, ServerBoundUncraftingRecipeSelectionPayload.STREAM_CODEC, (payload, context) -> payload.handle(new NeoForgePayloadContext(context)));
        registrar.playToServer(ServerBoundUEConfigPayload.TYPE, ServerBoundUEConfigPayload.STREAM_CODEC, (payload, context) -> {payload.handle(new NeoForgePayloadContext(context));NeoForgeUncraftEverythingConfig.save();});
        registrar.playToServer(ServerBoundRequestConfigPayload.TYPE, ServerBoundRequestConfigPayload.STREAM_CODEC, (payload, context) -> payload.handle(new NeoForgePayloadContext(context)));
        registrar.playToClient(ClientBoundResponseConfigPayload.TYPE, ClientBoundResponseConfigPayload.STREAM_CODEC);
        registrar.playToServer(ServerBoundUEExpPayload.TYPE, ServerBoundUEExpPayload.STREAM_CODEC, (payload, context) -> payload.handle(new NeoForgePayloadContext(context)));
        registrar.playToClient(ClientBoundUncraftingRecipeSelectionRequestPayload.TYPE, ClientBoundUncraftingRecipeSelectionRequestPayload.STREAM_CODEC);
        registrar.playToServer(ServerBoundUncraftingPageChangePayload.TYPE, ServerBoundUncraftingPageChangePayload.STREAM_CODEC, (payload, context) -> payload.handle(new NeoForgePayloadContext(context)));
        registrar.playToServer(ServerBoundUEProgressionPayload.TYPE, ServerBoundUEProgressionPayload.STREAM_CODEC, (payload, context) -> payload.handle(new NeoForgePayloadContext(context)));
        registrar.playToServer(ServerBoundExpTransferPayload.TYPE, ServerBoundExpTransferPayload.STREAM_CODEC, (payload, context) -> payload.handle(new NeoForgePayloadContext(context)));
        registrar.playToServer(ServerBoundSelectedIndexSyncPayload.TYPE, ServerBoundSelectedIndexSyncPayload.STREAM_CODEC, (payload, context) -> payload.handle(new NeoForgePayloadContext(context)));
        registrar.playToServer(ServerBoundAmountToAddPayload.TYPE, ServerBoundAmountToAddPayload.STREAM_CODEC, (payload, context) -> payload.handle(new NeoForgePayloadContext(context)));
        registrar.playToServer(ServerBoundTypeChangePayload.TYPE, ServerBoundTypeChangePayload.STREAM_CODEC, (payload, context) -> payload.handle(new NeoForgePayloadContext(context)));
        registrar.playToServer(ServerBoundCloseMenuPayload.TYPE, ServerBoundCloseMenuPayload.STREAM_CODEC, (payload, context) -> payload.handle(new NeoForgePayloadContext(context)));
        registrar.playToServer(ServerBoundClientConfigSyncPayload.TYPE, ServerBoundClientConfigSyncPayload.STREAM_CODEC, (payload, context) -> payload.handle(new NeoForgePayloadContext(context)));
    }
}
