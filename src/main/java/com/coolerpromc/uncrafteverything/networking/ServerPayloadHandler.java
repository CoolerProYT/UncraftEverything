package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
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
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

public class ServerPayloadHandler {
    public static boolean AUTO_MOVE = false;

    public static void handleButtonClick(UncraftingTableCraftButtonClickPayload payload, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            if (context.getSender() instanceof ServerPlayer player) {
                ServerLevel level = player.level();
                BlockPos pos = payload.blockPos();

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof UncraftingTableBlockEntity uncraftingTableBlockEntity) {
                    uncraftingTableBlockEntity.handleUncraftButtonClicked(payload.hasShiftDown());
                    blockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public static void handleRecipeSelection(UncraftingRecipeSelectionPayload payload, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            if (context.getSender() instanceof ServerPlayer player) {
                ServerLevel level = player.level();
                BlockPos pos = payload.blockPos();

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof AbstractUncraftingTableBE uncraftingTableBlockEntity) {
                    uncraftingTableBlockEntity.handleRecipeSelection(payload.recipe());

                    blockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        }).exceptionally(e -> {
            context.getConnection().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
            return null;
        });
    }

    public static void handleConfig(UEConfigPayload payload, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            if (context.getSender() instanceof ServerPlayer player) {
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
                config.prioritizeVanillaIngredientRecipe.set(payload.prioritizeVanillaIngredientRecipe());
                UncraftEverythingConfig.CONFIG_SPEC.save();
            }
        }).exceptionally(e -> {
            context.getConnection().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
            return null;
        });
    }

    public static void handleRequestConfig(RequestConfigPayload payload, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            if (context.getSender() instanceof ServerPlayer player) {
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
                        config.outputEnchantedBook.get(),
                        config.prioritizeVanillaIngredientRecipe.get()
                );
                UncraftEverything.CHANNEL.send(configPayload, PacketDistributor.PLAYER.with(player));
            }
        }).exceptionally(e -> {
            context.getConnection().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
            return null;
        });
    }

    public static void handleExpCost(UEExpPayload payload, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            if (context.getSender() instanceof ServerPlayer player) {
                PerItemExpCostConfig.getPerItemExp().clear();
                PerItemExpCostConfig.getPerItemExp().putAll(payload.perItemExp());
                PerItemExpCostConfig.save();
            }
        }).exceptionally(e -> {
            context.getConnection().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
            return null;
        });
    }

    public static void handleProgression(UEProgressionPayload payload, CustomPayloadEvent.Context context){
        context.enqueueWork(() -> {
            if (context.getSender() instanceof ServerPlayer){
                FTBQuestProgressionConfig.getProgressionMap().clear();
                FTBQuestProgressionConfig.getProgressionMap().putAll(payload.progressionMap());
                FTBQuestProgressionConfig.save();
            }
        }).exceptionally(e -> {
            context.getConnection().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
            return null;
        });
    }

    public static void handleRecipeSelectionData(UncraftingPageChangePayload payload, CustomPayloadEvent.Context context){
        context.enqueueWork(() -> {
            if (context.getSender() instanceof ServerPlayer player){
                ServerLevel level = player.level();
                BlockPos pos = payload.blockPos();

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof AbstractUncraftingTableBE uncraftingTableBlockEntity) {
                    uncraftingTableBlockEntity.updatePage(payload.page());

                    blockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        }).exceptionally(e -> {
            context.getConnection().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
            return null;
        });
    }

    public static void handleExpTransfer(ExpTransferPayload payload, CustomPayloadEvent.Context context){
        context.enqueueWork(() -> {
            if (context.getSender() instanceof ServerPlayer player){
                ServerLevel level = player.level();
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
            context.getConnection().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
            return null;
        });
    }

    public static void handleIndexSync(SelectedIndexSyncPayload payload, CustomPayloadEvent.Context context){
        context.enqueueWork(() -> {
            if (context.getSender() instanceof ServerPlayer player) {
                ServerLevel level = player.level();
                BlockPos pos = payload.blockPos();

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof AutoUncraftingTableBlockEntity uncraftingTableBlockEntity) {
                    uncraftingTableBlockEntity.index = payload.index();

                    uncraftingTableBlockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        }).exceptionally(e -> {
            context.getConnection().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
            return null;
        });
    }

    public static void handleAmountChange(AmountToAddPayload payload, CustomPayloadEvent.Context context){
        context.enqueueWork(() -> {
            if (context.getSender() instanceof ServerPlayer player) {
                ServerLevel level = player.level();
                BlockPos pos = payload.blockPos();

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof AutoUncraftingTableBlockEntity uncraftingTableBlockEntity) {
                    uncraftingTableBlockEntity.setAmountToAdd(payload.index());
                    uncraftingTableBlockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        }).exceptionally(e -> {
            context.getConnection().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
            return null;
        });
    }

    public static void handleTypeChange(TypeChangePayload payload, CustomPayloadEvent.Context context){
        context.enqueueWork(() -> {
            if (context.getSender() instanceof ServerPlayer player) {
                ServerLevel level = player.level();
                BlockPos pos = payload.blockPos();

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof AutoUncraftingTableBlockEntity uncraftingTableBlockEntity) {
                    uncraftingTableBlockEntity.setTypeToAdd(payload.expType());
                    uncraftingTableBlockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        }).exceptionally(e -> {
            context.getConnection().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
            return null;
        });
    }

    public static void handleCloseMenu(CloseMenuPayload payload, CustomPayloadEvent.Context context){
        context.enqueueWork(() -> {
            if (context.getSender() instanceof ServerPlayer player) {
                ServerLevel level = player.level();
                BlockPos pos = payload.pos();

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof AutoUncraftingTableBlockEntity uncraftingTableBlockEntity) {
                    uncraftingTableBlockEntity.setPlayer(null);
                    uncraftingTableBlockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        }).exceptionally(e -> {
            context.getConnection().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
            return null;
        });
    }

    public static void handleClientConfigSync(ClientConfigSyncPayload payload, CustomPayloadEvent.Context context){
        context.enqueueWork(() -> {
            AUTO_MOVE = payload.autoMoveToInventory();
        }).exceptionally(e -> {
            context.getConnection().disconnect(Component.translatable("screen.uncrafteverything.disconnected", e.getMessage()));
            return null;
        });
    }
}
