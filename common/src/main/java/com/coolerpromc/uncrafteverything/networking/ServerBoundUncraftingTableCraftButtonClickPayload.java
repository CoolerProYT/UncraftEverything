package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.platform.util.PayloadContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public record ServerBoundUncraftingTableCraftButtonClickPayload(BlockPos blockPos, boolean hasShiftDown) implements CustomPacketPayload {
    public static final Type<ServerBoundUncraftingTableCraftButtonClickPayload> TYPE = new Type<>(Constants.id("uncrafting_table_craft_button_click"));

    public static final StreamCodec<ByteBuf, ServerBoundUncraftingTableCraftButtonClickPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ServerBoundUncraftingTableCraftButtonClickPayload::blockPos,
                    ByteBufCodecs.BOOL,
                    ServerBoundUncraftingTableCraftButtonClickPayload::hasShiftDown,
                    ServerBoundUncraftingTableCraftButtonClickPayload::new
            );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(PayloadContext context){
        context.execute(() -> {
            if (context.player() instanceof ServerPlayer player){
                ServerLevel level = player.level();
                BlockPos pos = this.blockPos();

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof UncraftingTableBlockEntity uncraftingTableBlockEntity){
                    uncraftingTableBlockEntity.handleUncraftButtonClicked(this.hasShiftDown());
                    blockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        });
    }
}