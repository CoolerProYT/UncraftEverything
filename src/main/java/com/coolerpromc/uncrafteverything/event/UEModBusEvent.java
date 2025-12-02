package com.coolerpromc.uncrafteverything.event;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingClientConfig;
import com.coolerpromc.uncrafteverything.networking.*;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = UncraftEverything.MODID, bus = EventBusSubscriber.Bus.MOD)
public class UEModBusEvent {
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, UEBlockEntities.UNCRAFTING_TABLE_BE.get(), (blockEntity, direction) -> {
            if (direction == Direction.DOWN){
                return blockEntity.getOutputHandler();
            }
            return blockEntity.getInputHandler();
        });

        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, UEBlockEntities.AUTO_UNCRAFTING_TABLE_BE.get(), (blockEntity, direction) -> {
            if (direction == Direction.DOWN){
                return blockEntity.getOutputHandler();
            }
            return blockEntity.getInputHandler();
        });
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == UncraftEverythingClientConfig.CONFIG_SPEC) {
            UncraftEverythingClientConfig.CONFIG.onChanged();
        }
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == UncraftEverythingClientConfig.CONFIG_SPEC) {
            UncraftEverythingClientConfig.CONFIG.onChanged();
        }
    }

    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                UncraftingTableCraftButtonClickPayload.TYPE,
                UncraftingTableCraftButtonClickPayload.STREAM_CODEC,
                ServerPayloadHandler::handleButtonClick
        );

        registrar.playToClient(
                UncraftingTableDataPayload.TYPE,
                UncraftingTableDataPayload.STREAM_CODEC,
                FMLEnvironment.dist.isClient() ? ClientPayloadHandler::handleBlockEntityData : (payload, context) -> {
                }
        );

        registrar.playToServer(
                UncraftingRecipeSelectionPayload.TYPE,
                UncraftingRecipeSelectionPayload.STREAM_CODEC,
                ServerPayloadHandler::handleRecipeSelection
        );

        registrar.playToServer(
                UEConfigPayload.TYPE,
                UEConfigPayload.STREAM_CODEC,
                ServerPayloadHandler::handleConfig
        );

        registrar.playToServer(
                RequestConfigPayload.TYPE,
                RequestConfigPayload.STREAM_CODEC,
                ServerPayloadHandler::handleRequestConfig
        );

        registrar.playToClient(
                ResponseConfigPayload.TYPE,
                ResponseConfigPayload.STREAM_CODEC,
                FMLEnvironment.dist.isClient() ? ClientPayloadHandler::handleConfigSync : (payload, context) -> {
                }
        );

        registrar.playToServer(
                UEExpPayload.TYPE,
                UEExpPayload.STREAM_CODEC,
                ServerPayloadHandler::handleExpCost
        );

        registrar.playToClient(
                UncraftingRecipeSelectionRequestPayload.TYPE,
                UncraftingRecipeSelectionRequestPayload.STREAM_CODEC,
                FMLEnvironment.dist.isClient() ? ClientPayloadHandler::handleRecipeSelectionRequest : (payload, context) -> {
                }
        );

        registrar.playToServer(
                UncraftingPageChangePayload.TYPE,
                UncraftingPageChangePayload.STREAM_CODEC,
                ServerPayloadHandler::handleRecipeSelectionData
        );

        registrar.playToServer(
                UEProgressionPayload.TYPE,
                UEProgressionPayload.STREAM_CODEC,
                ServerPayloadHandler::handleProgression
        );

        registrar.playToServer(
                ExpTransferPayload.TYPE,
                ExpTransferPayload.STREAM_CODEC,
                ServerPayloadHandler::handleExpTransfer
        );

        registrar.playToServer(
                SelectedIndexSyncPayload.TYPE,
                SelectedIndexSyncPayload.STREAM_CODEC,
                ServerPayloadHandler::handleIndexSync
        );

        registrar.playToServer(
                AmountToAddPayload.TYPE,
                AmountToAddPayload.STREAM_CODEC,
                ServerPayloadHandler::handleAmountChange
        );

        registrar.playToServer(
                TypeChangePayload.TYPE,
                TypeChangePayload.STREAM_CODEC,
                ServerPayloadHandler::handleTypeChange
        );

        registrar.playToServer(
                CloseMenuPayload.TYPE,
                CloseMenuPayload.STREAM_CODEC,
                ServerPayloadHandler::handleCloseMenu
        );

        registrar.playToServer(
                ClientConfigSyncPayload.TYPE,
                ClientConfigSyncPayload.STREAM_CODEC,
                ServerPayloadHandler::handleClientConfigSync
        );
    }
}
