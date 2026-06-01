package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.command.ModServerCommands;
import com.coolerpromc.uncrafteverything.networking.*;
import com.coolerpromc.uncrafteverything.platform.util.FabricServerPayloadContext;
import com.coolerpromc.uncrafteverything.platform.util.PayloadContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.*;

public class FabricUncraftEverything implements ModInitializer {
    public static MinecraftServer MINECRAFT_SERVER;

    @Override
    public void onInitialize() {
        UncraftEverything.init();

        PayloadTypeRegistry.serverboundPlay().register(ServerBoundUncraftingTableCraftButtonClickPayload.TYPE, ServerBoundUncraftingTableCraftButtonClickPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientBoundUncraftingTableDataPayload.TYPE, ClientBoundUncraftingTableDataPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerBoundUncraftingRecipeSelectionPayload.TYPE, ServerBoundUncraftingRecipeSelectionPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerBoundUEConfigPayload.TYPE, ServerBoundUEConfigPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerBoundRequestConfigPayload.TYPE, ServerBoundRequestConfigPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientBoundResponseConfigPayload.TYPE, ClientBoundResponseConfigPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerBoundUEExpPayload.TYPE, ServerBoundUEExpPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientBoundUncraftingRecipeSelectionRequestPayload.TYPE, ClientBoundUncraftingRecipeSelectionRequestPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerBoundUncraftingPageChangePayload.TYPE, ServerBoundUncraftingPageChangePayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerBoundUEProgressionPayload.TYPE, ServerBoundUEProgressionPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerBoundExpTransferPayload.TYPE, ServerBoundExpTransferPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerBoundSelectedIndexSyncPayload.TYPE, ServerBoundSelectedIndexSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerBoundAmountToAddPayload.TYPE, ServerBoundAmountToAddPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerBoundTypeChangePayload.TYPE, ServerBoundTypeChangePayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerBoundCloseMenuPayload.TYPE, ServerBoundCloseMenuPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerBoundClientConfigSyncPayload.TYPE, ServerBoundClientConfigSyncPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ServerBoundUncraftingTableCraftButtonClickPayload.TYPE, (payload, context) -> payload.handle(new FabricServerPayloadContext(context)));
        ServerPlayNetworking.registerGlobalReceiver(ServerBoundUncraftingRecipeSelectionPayload.TYPE, (payload, context) -> payload.handle(new FabricServerPayloadContext(context)));
        ServerPlayNetworking.registerGlobalReceiver(ServerBoundUEConfigPayload.TYPE, (payload, context) -> payload.handle(new FabricServerPayloadContext(context)));
        ServerPlayNetworking.registerGlobalReceiver(ServerBoundRequestConfigPayload.TYPE, (payload, context) -> payload.handle(new FabricServerPayloadContext(context)));
        ServerPlayNetworking.registerGlobalReceiver(ServerBoundUEExpPayload.TYPE, (payload, context) -> payload.handle(new FabricServerPayloadContext(context)));
        ServerPlayNetworking.registerGlobalReceiver(ServerBoundUncraftingPageChangePayload.TYPE, (payload, context) -> payload.handle(new FabricServerPayloadContext(context)));
        ServerPlayNetworking.registerGlobalReceiver(ServerBoundUEProgressionPayload.TYPE, (payload, context) -> payload.handle(new FabricServerPayloadContext(context)));
        ServerPlayNetworking.registerGlobalReceiver(ServerBoundExpTransferPayload.TYPE, (payload, context) -> payload.handle(new FabricServerPayloadContext(context)));
        ServerPlayNetworking.registerGlobalReceiver(ServerBoundSelectedIndexSyncPayload.TYPE, (payload, context) -> payload.handle(new FabricServerPayloadContext(context)));
        ServerPlayNetworking.registerGlobalReceiver(ServerBoundAmountToAddPayload.TYPE, (payload, context) -> payload.handle(new FabricServerPayloadContext(context)));
        ServerPlayNetworking.registerGlobalReceiver(ServerBoundTypeChangePayload.TYPE, (payload, context) -> payload.handle(new FabricServerPayloadContext(context)));
        ServerPlayNetworking.registerGlobalReceiver(ServerBoundCloseMenuPayload.TYPE, (payload, context) -> payload.handle(new FabricServerPayloadContext(context)));
        ServerPlayNetworking.registerGlobalReceiver(ServerBoundClientConfigSyncPayload.TYPE, (payload, context) -> payload.handle(new FabricServerPayloadContext(context)));

        RecipeSynchronization.synchronizeRecipeSerializer(ShapedRecipe.SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(ShapelessRecipe.SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(FireworkRocketRecipe.SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(FireworkStarRecipe.SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(FireworkStarFadeRecipe.SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(ImbueRecipe.SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(TransmuteRecipe.SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(SmithingTransformRecipe.SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(SmithingTrimRecipe.SERIALIZER);

        ServerLifecycleEvents.SERVER_STARTING.register(server -> MINECRAFT_SERVER = server);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((serverPlayer, _) -> PayloadContext.syncConfig(serverPlayer));
        CommandRegistrationCallback.EVENT.register((commandDispatcher, _, _) -> ModServerCommands.registerServer(commandDispatcher));

        ItemStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> {
            if (direction == Direction.DOWN){
                return ContainerStorage.of(blockEntity.getOutputHandler(), null);
            }
            return ContainerStorage.of(blockEntity.getInputHandler(), null);
        }, UEBlockEntities.AUTO_UNCRAFTING_TABLE_BE.get());

        ServerPlayerEvents.JOIN.register(UncraftEverything::onPlayerLogin);
    }
}