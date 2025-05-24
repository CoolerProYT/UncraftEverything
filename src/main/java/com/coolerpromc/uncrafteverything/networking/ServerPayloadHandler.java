package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.config.PerItemExpCostConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.List;
import java.util.function.Supplier;

public class ServerPayloadHandler {
    public static void handleButtonClick(UncraftingTableCraftButtonClickPayload payload, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();
            if (player != null) {
                ServerWorld level = player.getLevel();
                BlockPos pos = payload.blockPos();

                TileEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof UncraftingTableBlockEntity) {
                    UncraftingTableBlockEntity uncraftingTableBlockEntity = (UncraftingTableBlockEntity) blockEntity;
                    uncraftingTableBlockEntity.handleButtonClick();
                    blockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        }).exceptionally(e -> {
            context.get().getNetworkManager().disconnect(new StringTextComponent(e.getMessage()));
            return null;
        });
    }

    public static void handleRecipeSelection(UncraftingRecipeSelectionPayload payload, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();
            if (player != null) {
                ServerWorld level = player.getLevel();
                BlockPos pos = payload.blockPos();

                TileEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof UncraftingTableBlockEntity) {
                    UncraftingTableBlockEntity uncraftingTableBlockEntity = (UncraftingTableBlockEntity) blockEntity;
                    uncraftingTableBlockEntity.handleRecipeSelection(payload.recipe());

                    blockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        }).exceptionally(e -> {
            context.get().getNetworkManager().disconnect(new StringTextComponent(e.getMessage()));
            return null;
        });
        context.get().setPacketHandled(true);
    }

    public static void handleConfig(UEConfigPayload payload, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (context.get().getSender() != null) {
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
            context.get().getNetworkManager().disconnect(new StringTextComponent(e.getMessage()));
            return null;
        });
    }

    public static void handleRequestConfig(RequestConfigPayload payload, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (context.get().getSender() != null) {
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
                ResponseConfigPayload.INSTANCE.send(PacketDistributor.PLAYER.with(() -> context.get().getSender()), configPayload);
            }
        }).exceptionally(e -> {
            context.get().getNetworkManager().disconnect(new StringTextComponent(e.getMessage()));
            return null;
        });
    }

    public static void handleExpCost(UEExpPayload payload, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (context.get().getSender() != null) {
                PerItemExpCostConfig.getPerItemExp().clear();
                PerItemExpCostConfig.getPerItemExp().putAll(payload.perItemExp());
                PerItemExpCostConfig.save();
            }
        }).exceptionally(e -> {
            context.get().getNetworkManager().disconnect(new StringTextComponent(e.getMessage()));
            return null;
        });
    }
}
