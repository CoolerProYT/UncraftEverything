package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.blockentity.custom.AbstractUncraftingTableBE;
import com.coolerpromc.uncrafteverything.platform.util.PayloadContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public record ServerBoundUncraftingPageChangePayload(int page, BlockPos blockPos) implements CustomPacketPayload {
    public static final Type<ServerBoundUncraftingPageChangePayload> TYPE = new Type<>(Constants.id("uncrafting_recipe_selection_data_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundUncraftingPageChangePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ServerBoundUncraftingPageChangePayload::page,
            BlockPos.STREAM_CODEC,
            ServerBoundUncraftingPageChangePayload::blockPos,
            ServerBoundUncraftingPageChangePayload::new
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
                if (blockEntity instanceof AbstractUncraftingTableBE uncraftingTableBlockEntity) {
                    uncraftingTableBlockEntity.updatePage(this.page());

                    blockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        });
    }
}