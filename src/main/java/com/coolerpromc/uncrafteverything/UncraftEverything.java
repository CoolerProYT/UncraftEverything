package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.block.UEBlocks;
import com.coolerpromc.uncrafteverything.blockentity.UEBlockEntities;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.config.PerItemExpCostConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.item.UECreativeTab;
import com.coolerpromc.uncrafteverything.networking.UncraftingRecipeSelectionPayload;
import com.coolerpromc.uncrafteverything.networking.UncraftingTableCraftButtonClickPayload;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
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
			try{
				ServerWorld level = serverPlayerEntity.getServerWorld();
				UncraftingTableCraftButtonClickPayload uncraftingTableCraftButtonClickPayload = packetByteBuf.decode(UncraftingTableCraftButtonClickPayload.CODEC);

				BlockPos pos = uncraftingTableCraftButtonClickPayload.blockPos();

				minecraftServer.execute(() -> {
					BlockEntity blockEntity = level.getBlockEntity(pos);

					if (blockEntity instanceof UncraftingTableBlockEntity){
						UncraftingTableBlockEntity uncraftingTableBlockEntity = (UncraftingTableBlockEntity) blockEntity;
						uncraftingTableBlockEntity.handleButtonClick();
						blockEntity.markDirty();
						level.updateListeners(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
					}
				});
			} catch (Exception e) {
				System.out.println("Failed to decode UncraftingTableCraftButtonClickPayload: " + e.getMessage());
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(UncraftingRecipeSelectionPayload.ID, (minecraftServer, serverPlayerEntity, serverPlayNetworkHandler, packetByteBuf, packetSender) -> {
			try{
				ServerWorld level = serverPlayerEntity.getServerWorld();
				UncraftingRecipeSelectionPayload uncraftingRecipeSelectionPayload = packetByteBuf.decode(UncraftingRecipeSelectionPayload.CODEC);
				BlockPos pos = uncraftingRecipeSelectionPayload.blockPos();

				minecraftServer.execute(() -> {
					BlockEntity blockEntity = level.getBlockEntity(pos);
					if (blockEntity instanceof UncraftingTableBlockEntity){
						UncraftingTableBlockEntity uncraftingTableBlockEntity = (UncraftingTableBlockEntity) blockEntity;
						uncraftingTableBlockEntity.handleRecipeSelection(uncraftingRecipeSelectionPayload.recipe());
						blockEntity.markDirty();
						level.updateListeners(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
					}
				});
			} catch (Exception e) {
				System.out.println("Failed to decode UncraftingRecipeSelectionPayload: " + e.getMessage());
			}
		});
	}
}