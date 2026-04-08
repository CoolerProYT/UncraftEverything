package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.blockentity.custom.AutoUncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.platform.util.PayloadContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public record ServerBoundCloseMenuPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<ServerBoundCloseMenuPayload> TYPE = new Type<>(Constants.id("close_menu_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundCloseMenuPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ServerBoundCloseMenuPayload::pos,
            ServerBoundCloseMenuPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(PayloadContext context){
        context.execute(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ServerLevel level = player.level();
                BlockPos pos = this.pos();

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof AutoUncraftingTableBlockEntity uncraftingTableBlockEntity) {
                    uncraftingTableBlockEntity.setPlayer(null);
                    uncraftingTableBlockEntity.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        });
    }
}