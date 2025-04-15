package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {
    public static void handleButtonClick(UncraftingTableCraftButtonClickPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ServerLevel level = player.serverLevel();
                BlockPos pos = payload.blockPos();

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof UncraftingTableBlockEntity uncraftingTableBlockEntity) {
                    uncraftingTableBlockEntity.handleButtonClick(payload.data());
                    blockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("mymod.networking.failed", e.getMessage()));
            return null;
        });
    }

    public static void handleRecipeSelection(UncraftingRecipeSelectionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ServerLevel level = player.serverLevel();
                BlockPos pos = payload.blockPos();

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof UncraftingTableBlockEntity uncraftingTableBlockEntity) {
                    uncraftingTableBlockEntity.handleRecipeSelection(payload.recipe());

                    blockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("mymod.networking.failed", e.getMessage()));
            return null;
        });
    }
}
