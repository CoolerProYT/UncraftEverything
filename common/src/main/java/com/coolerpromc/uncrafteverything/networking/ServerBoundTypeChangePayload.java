package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.blockentity.custom.AutoUncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.platform.util.PayloadContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public record ServerBoundTypeChangePayload(BlockPos blockPos, UncraftEverythingConfig.ExperienceType expType) implements CustomPacketPayload {
    public static final Type<ServerBoundTypeChangePayload> TYPE = new Type<>(Constants.id("type_change_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundTypeChangePayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ServerBoundTypeChangePayload::blockPos,
                    UncraftEverythingConfig.ExperienceType.STREAM_CODEC,
                    ServerBoundTypeChangePayload::expType,
                    ServerBoundTypeChangePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(PayloadContext context){
        context.execute(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ServerLevel level = player.level();
                BlockPos pos = this.blockPos();

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof AutoUncraftingTableBlockEntity uncraftingTableBlockEntity) {
                    uncraftingTableBlockEntity.setTypeToAdd(this.expType());
                    uncraftingTableBlockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        });
    }
}