package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.blockentity.custom.AutoUncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.platform.util.PayloadContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public record ServerBoundAmountToAddPayload(BlockPos blockPos, int index) implements CustomPacketPayload {
    public static final Type<ServerBoundAmountToAddPayload> TYPE = new Type<>(Constants.id("amount_to_add_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundAmountToAddPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ServerBoundAmountToAddPayload::blockPos,
                    ByteBufCodecs.INT,
                    ServerBoundAmountToAddPayload::index,
                    ServerBoundAmountToAddPayload::new
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
                    uncraftingTableBlockEntity.setAmountToAdd(this.index());
                    uncraftingTableBlockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        });
    }
}