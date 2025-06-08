package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.config.PerItemExpCostConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.item.UECreativeTab;
import com.coolerpromc.uncrafteverything.networking.*;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class UncraftEverything implements ModInitializer {
	public static final String MODID = "uncrafteverything";

	@Override
	public void onInitialize() {
		UEBlocks.register();
		UEBlockEntities.register();
		UEMenuTypes.register();
		UECreativeTab.register();

		UncraftEverythingConfig.load();
		UncraftEverythingConfig.save();

		PerItemExpCostConfig.load();
		PerItemExpCostConfig.startWatcher();

		PayloadTypeRegistry.playC2S().register(UncraftingTableCraftButtonClickPayload.TYPE, UncraftingTableCraftButtonClickPayload.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(UncraftingTableDataPayload.TYPE, UncraftingTableDataPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(UncraftingRecipeSelectionPayload.TYPE, UncraftingRecipeSelectionPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(UEConfigPayload.TYPE, UEConfigPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(RequestConfigPayload.TYPE, RequestConfigPayload.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(ResponseConfigPayload.TYPE, ResponseConfigPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(UEExpPayload.TYPE, UEExpPayload.STREAM_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(UncraftingTableCraftButtonClickPayload.TYPE, (uncraftingTableCraftButtonClickPayload, context) -> {
			if (context.player() instanceof ServerPlayerEntity player){
				ServerWorld level = player.getWorld();
				BlockPos pos = uncraftingTableCraftButtonClickPayload.blockPos();

				BlockEntity blockEntity = level.getBlockEntity(pos);
				if (blockEntity instanceof UncraftingTableBlockEntity uncraftingTableBlockEntity){
					uncraftingTableBlockEntity.handleButtonClick(uncraftingTableCraftButtonClickPayload.data());
					blockEntity.markDirty();
					level.updateListeners(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
				}
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(UncraftingRecipeSelectionPayload.TYPE, (uncraftingRecipeSelectionPayload, context) -> {
			if (context.player() instanceof ServerPlayerEntity player){
				ServerWorld level = player.getWorld();
				BlockPos pos = uncraftingRecipeSelectionPayload.blockPos();

				BlockEntity blockEntity = level.getBlockEntity(pos);
				if (blockEntity instanceof UncraftingTableBlockEntity uncraftingTableBlockEntity){
					uncraftingTableBlockEntity.handleRecipeSelection(uncraftingRecipeSelectionPayload.recipe());
					blockEntity.markDirty();
					level.updateListeners(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
				}
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(UEConfigPayload.TYPE, (payload, context) -> {
			if (context.player() instanceof ServerPlayerEntity) {
				UncraftEverythingConfig.restrictionType = payload.restrictionType();
				UncraftEverythingConfig.restrictions = payload.restrictedItems();
				UncraftEverythingConfig.allowEnchantedItems = payload.allowEnchantedItem();
				UncraftEverythingConfig.experienceType = payload.experienceType();
				UncraftEverythingConfig.experience = payload.experience();
				UncraftEverythingConfig.allowUnSmithing = payload.allowUnsmithing();
				UncraftEverythingConfig.allowDamaged = payload.allowDamaged();
				UncraftEverythingConfig.save();
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(RequestConfigPayload.TYPE, (requestConfigPayload, context) -> {
			if (context.player() instanceof ServerPlayerEntity player) {
				ServerPlayNetworking.send(player, new ResponseConfigPayload(
						UncraftEverythingConfig.restrictionType,
						UncraftEverythingConfig.restrictions,
						UncraftEverythingConfig.allowEnchantedItems,
						UncraftEverythingConfig.experienceType,
						UncraftEverythingConfig.experience,
						UncraftEverythingConfig.allowUnSmithing,
						UncraftEverythingConfig.allowDamaged,
						PerItemExpCostConfig.getPerItemExp()
				));
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(UEExpPayload.TYPE, (payload, context) -> {
			if (context.player() instanceof ServerPlayerEntity) {
				PerItemExpCostConfig.getPerItemExp().clear();
				PerItemExpCostConfig.getPerItemExp().putAll(payload.perItemExp());
				PerItemExpCostConfig.save();
			}
		});
	}
}