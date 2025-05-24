package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.config.PerItemExpCostConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

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
            context.disconnect(Component.literal(e.getMessage()));
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
            context.disconnect(Component.literal(e.getMessage()));
            return null;
        });
    }

    public static void handleConfig(UEConfigPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                UncraftEverythingConfig config = UncraftEverythingConfig.CONFIG;
                config.restrictionType.set(payload.restrictionType());
                config.restrictions.set(payload.restrictedItems());
                config.allowEnchantedItems.set(payload.allowEnchantedItem());
                config.experienceType.set(payload.experienceType());
                config.experience.set(payload.experience());
                config.allowUnSmithing.set(payload.allowUnsmithing());
                UncraftEverythingConfig.CONFIG_SPEC.save();
            }
        }).exceptionally(e -> {
            context.disconnect(Component.literal(e.getMessage()));
            return null;
        });
    }

    public static void handleRequestConfig(RequestConfigPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                UncraftEverythingConfig config = UncraftEverythingConfig.CONFIG;
                ResponseConfigPayload configPayload = new ResponseConfigPayload(
                        config.restrictionType.get(),
                        (List<String>) config.restrictions.get(),
                        config.allowEnchantedItems.get(),
                        config.experienceType.get(),
                        config.experience.get(),
                        config.allowUnSmithing.get(),
                        PerItemExpCostConfig.getPerItemExp()
                );
                PacketDistributor.sendToPlayer(player, configPayload);
            }
        }).exceptionally(e -> {
            context.disconnect(Component.literal(e.getMessage()));
            return null;
        });
    }

    public static void handleExpCost(UEExpPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                PerItemExpCostConfig.getPerItemExp().clear();
                PerItemExpCostConfig.getPerItemExp().putAll(payload.perItemExp());
                PerItemExpCostConfig.save();
            }
        }).exceptionally(e -> {
            context.disconnect(Component.literal(e.getMessage()));
            return null;
        });
    }
}
