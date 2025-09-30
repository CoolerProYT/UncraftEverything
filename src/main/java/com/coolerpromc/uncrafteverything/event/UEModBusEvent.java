package com.coolerpromc.uncrafteverything.event;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.networking.*;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = UncraftEverything.MODID)
public class UEModBusEvent {
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, UEBlockEntities.UNCRAFTING_TABLE_BE.get(), (blockEntity, direction) -> {
            if (direction == Direction.DOWN){
                return blockEntity.getOutputHandler();
            }
            return blockEntity.getInputHandler();
        });
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
                FMLEnvironment.getDist().isClient() ? ClientPayloadHandler::handleBlockEntityData : (payload, context) -> {
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
                FMLEnvironment.getDist().isClient() ? ClientPayloadHandler::handleConfigSync : (payload, context) -> {
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
                FMLEnvironment.getDist().isClient() ? ClientPayloadHandler::handleRecipeSelectionRequest : (payload, context) -> {
                }
        );

        registrar.playToServer(
                UncraftingRecipeSelectionDataPayload.TYPE,
                UncraftingRecipeSelectionDataPayload.STREAM_CODEC,
                ServerPayloadHandler::handleRecipeSelectionData
        );

        registrar.playToServer(
                UEProgressionPayload.TYPE,
                UEProgressionPayload.STREAM_CODEC,
                ServerPayloadHandler::handleProgression
        );
    }
}
