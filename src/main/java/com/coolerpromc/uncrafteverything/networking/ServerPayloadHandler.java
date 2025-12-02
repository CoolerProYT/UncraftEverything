package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.blockentity.custom.AbstractUncraftingTableBE;
import com.coolerpromc.uncrafteverything.blockentity.custom.AutoUncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.config.FTBQuestProgressionConfig;
import com.coolerpromc.uncrafteverything.config.PerItemExpCostConfig;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Supplier;

public class ServerPayloadHandler {
    public static boolean AUTO_MOVE = false;

    public static void handleButtonClick(UncraftingTableCraftButtonClickPayload payload, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (context.get().getSender() != null) {
                ServerPlayer player = context.get().getSender();
                ServerLevel level = player.serverLevel();
                BlockPos pos = payload.blockPos();

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof UncraftingTableBlockEntity uncraftingTableBlockEntity) {
                    uncraftingTableBlockEntity.handleUncraftButtonClicked(payload.hasShiftDown());
                    blockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        }).exceptionally(e -> {
            context.get().getNetworkManager().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
            return null;
        });
    }

    public static void handleRecipeSelection(UncraftingRecipeSelectionPayload payload, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                ServerLevel level = player.serverLevel();
                BlockPos pos = payload.blockPos();

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof AbstractUncraftingTableBE uncraftingTableBlockEntity) {
                    uncraftingTableBlockEntity.handleRecipeSelection(payload.recipe());

                    blockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        }).exceptionally(e -> {
            context.get().getNetworkManager().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
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
                config.allowDamaged.set(payload.allowDamaged());
                config.preventModdedIngredientsFromVanillaItems.set(payload.preventModdedIngredientsFromVanillaItems());
                config.restrictedModIngredients.set(payload.restrictedModIngredients());
                config.enableProgression.set(payload.enableProgression());
                config.onlyAllowDefinedProgression.set(payload.onlyAllowDefinedProgression());
                config.outputEnchantedBook.set(payload.outputEnchantedBook());
                UncraftEverythingConfig.CONFIG_SPEC.save();
            }
        }).exceptionally(e -> {
            context.get().getNetworkManager().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
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
                        config.allowDamaged.get(),
                        config.preventModdedIngredientsFromVanillaItems.get(),
                        PerItemExpCostConfig.getPerItemExp(),
                        (List<String>) config.restrictedModIngredients.get(),
                        FTBQuestProgressionConfig.getProgressionMap(),
                        config.enableProgression.get(),
                        config.onlyAllowDefinedProgression.get(),
                        config.outputEnchantedBook.get()
                );
                ResponseConfigPayload.INSTANCE.send(PacketDistributor.PLAYER.with(() -> context.get().getSender()), configPayload);
            }
        }).exceptionally(e -> {
            context.get().getNetworkManager().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
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
            context.get().getNetworkManager().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
            return null;
        });
    }

    public static void handleProgression(UEProgressionPayload payload, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> {
            if (context.get().getSender() != null) {
                FTBQuestProgressionConfig.getProgressionMap().clear();
                FTBQuestProgressionConfig.getProgressionMap().putAll(payload.progressionMap());
                FTBQuestProgressionConfig.save();
            }
        }).exceptionally(e -> {
            context.get().getNetworkManager().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
            return null;
        });
    }

    public static void handleRecipeSelectionData(UncraftingPageChangePayload payload, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> {
            if (context.get().getSender() != null){
                ServerPlayer player = context.get().getSender();
                ServerLevel level = player.serverLevel();
                BlockPos pos = payload.blockPos();

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof AbstractUncraftingTableBE uncraftingTableBlockEntity) {
                    uncraftingTableBlockEntity.updatePage(payload.page());

                    blockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        }).exceptionally(e -> {
            context.get().getNetworkManager().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
            return null;
        });
    }

    public static void handleExpTransfer(ExpTransferPayload payload, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> {
            if (context.get().getSender() != null){
                ServerPlayer player = context.get().getSender();
                ServerLevel level = player.serverLevel();
                BlockPos pos = payload.pos();

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof AutoUncraftingTableBlockEntity uncraftingTableBlockEntity) {
                    if (payload.experienceType() == UncraftEverythingConfig.ExperienceType.LEVEL){
                        uncraftingTableBlockEntity.addExperienceLevels(payload.amount(), player);
                    }
                    else {
                        uncraftingTableBlockEntity.addExperiencePoints(payload.amount(), player);
                    }
                }
            }
        }).exceptionally(e -> {
            context.get().getNetworkManager().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
            return null;
        });
    }

    public static void handleIndexSync(SelectedIndexSyncPayload payload, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> {
            if (context.get().getSender() != null) {
                ServerPlayer player = context.get().getSender();
                ServerLevel level = player.serverLevel();
                BlockPos pos = payload.blockPos();

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof AutoUncraftingTableBlockEntity uncraftingTableBlockEntity) {
                    uncraftingTableBlockEntity.index = payload.index();

                    uncraftingTableBlockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        }).exceptionally(e -> {
            context.get().getNetworkManager().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
            return null;
        });
    }

    public static void handleAmountChange(AmountToAddPayload payload, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> {
            if (context.get().getSender() != null) {
                ServerPlayer player = context.get().getSender();
                ServerLevel level = player.serverLevel();
                BlockPos pos = payload.blockPos();

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof AutoUncraftingTableBlockEntity uncraftingTableBlockEntity) {
                    uncraftingTableBlockEntity.setAmountToAdd(payload.index());
                    uncraftingTableBlockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        }).exceptionally(e -> {
            context.get().getNetworkManager().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
            return null;
        });
    }

    public static void handleTypeChange(TypeChangePayload payload, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> {
            if (context.get().getSender() != null) {
                ServerPlayer player = context.get().getSender();
                ServerLevel level = player.serverLevel();
                BlockPos pos = payload.blockPos();

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof AutoUncraftingTableBlockEntity uncraftingTableBlockEntity) {
                    uncraftingTableBlockEntity.setTypeToAdd(payload.expType());
                    uncraftingTableBlockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        }).exceptionally(e -> {
            context.get().getNetworkManager().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
            return null;
        });
    }

    public static void handleCloseMenu(CloseMenuPayload payload, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> {
            if (context.get().getSender() != null) {
                ServerPlayer player = context.get().getSender();
                ServerLevel level = player.serverLevel();
                BlockPos pos = payload.pos();

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof AutoUncraftingTableBlockEntity uncraftingTableBlockEntity) {
                    uncraftingTableBlockEntity.setPlayer(null);
                    uncraftingTableBlockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        }).exceptionally(e -> {
            context.get().getNetworkManager().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
            return null;
        });
    }

    public static void handleClientConfigSync(ClientConfigSyncPayload payload, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> {
            AUTO_MOVE = payload.autoMoveToInventory();
        }).exceptionally(e -> {
            context.get().getNetworkManager().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
            return null;
        });
    }
}
