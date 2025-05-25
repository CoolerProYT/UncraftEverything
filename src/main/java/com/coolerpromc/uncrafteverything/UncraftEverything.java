package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.block.custom.UncraftingTableBlock;
import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.config.PerItemExpCostConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.item.UECreativeTab;
import com.coolerpromc.uncrafteverything.networking.*;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.PacketByteBuf;
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

		ServerPlayNetworking.registerGlobalReceiver(UncraftingTableCraftButtonClickPayload.ID, (minecraftServer, serverPlayerEntity, serverPlayNetworkHandler, packetByteBuf, packetSender) -> {
			ServerWorld level = serverPlayerEntity.getServerWorld();
			UncraftingTableCraftButtonClickPayload uncraftingTableCraftButtonClickPayload = packetByteBuf.decodeAsJson(UncraftingTableCraftButtonClickPayload.CODEC);
			BlockPos pos = uncraftingTableCraftButtonClickPayload.blockPos();

			minecraftServer.execute(() -> {
				BlockEntity blockEntity = level.getBlockEntity(pos, UEBlockEntities.UNCRAFTING_TABLE_BE).orElse(null);

				if (blockEntity instanceof UncraftingTableBlockEntity uncraftingTableBlockEntity){
					uncraftingTableBlockEntity.handleButtonClick();
					blockEntity.markDirty();
					level.updateListeners(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(UncraftingRecipeSelectionPayload.ID, (minecraftServer, serverPlayerEntity, serverPlayNetworkHandler, packetByteBuf, packetSender) -> {
			ServerWorld level = serverPlayerEntity.getServerWorld();
			UncraftingRecipeSelectionPayload uncraftingRecipeSelectionPayload = packetByteBuf.decodeAsJson(UncraftingRecipeSelectionPayload.CODEC);
			BlockPos pos = uncraftingRecipeSelectionPayload.blockPos();

			minecraftServer.execute(() -> {
				BlockEntity blockEntity = level.getBlockEntity(pos);
				if (blockEntity instanceof UncraftingTableBlockEntity uncraftingTableBlockEntity){
					uncraftingTableBlockEntity.handleRecipeSelection(uncraftingRecipeSelectionPayload.recipe());
					blockEntity.markDirty();
					level.updateListeners(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(UEConfigPayload.TYPE, (minecraftServer, serverPlayerEntity, serverPlayNetworkHandler, packetByteBuf, packetSender) -> {
			UEConfigPayload payload = UEConfigPayload.decode(packetByteBuf);

			UncraftEverythingConfig.restrictionType = payload.restrictionType();
			UncraftEverythingConfig.restrictions = payload.restrictedItems();
			UncraftEverythingConfig.allowEnchantedItems = payload.allowEnchantedItem();
			UncraftEverythingConfig.experienceType = payload.experienceType();
			UncraftEverythingConfig.experience = payload.experience();
			UncraftEverythingConfig.allowUnSmithing = payload.allowUnsmithing();
			UncraftEverythingConfig.save();
		});

		ServerPlayNetworking.registerGlobalReceiver(RequestConfigPayload.TYPE, (minecraftServer, serverPlayerEntity, serverPlayNetworkHandler, packetByteBuf, packetSender) -> {
			ServerPlayNetworking.send(serverPlayerEntity, ResponseConfigPayload.TYPE, ResponseConfigPayload.encode(PacketByteBufs.create(), new ResponseConfigPayload(
					UncraftEverythingConfig.restrictionType,
					UncraftEverythingConfig.restrictions,
					UncraftEverythingConfig.allowEnchantedItems,
					UncraftEverythingConfig.experienceType,
					UncraftEverythingConfig.experience,
					UncraftEverythingConfig.allowUnSmithing,
					PerItemExpCostConfig.getPerItemExp()
			)));
		});

		ServerPlayNetworking.registerGlobalReceiver(UEExpPayload.TYPE, (minecraftServer, serverPlayerEntity, serverPlayNetworkHandler, packetByteBuf, packetSender) -> {
			UEExpPayload payload = UEExpPayload.decode(packetByteBuf);

			PerItemExpCostConfig.getPerItemExp().clear();
			PerItemExpCostConfig.getPerItemExp().putAll(payload.perItemExp());
			PerItemExpCostConfig.save();
		});
	}
}