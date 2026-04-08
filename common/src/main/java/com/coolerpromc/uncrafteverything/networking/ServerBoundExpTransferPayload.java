package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.blockentity.custom.AutoUncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import com.coolerpromc.uncrafteverything.platform.util.PayloadContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public record ServerBoundExpTransferPayload(BlockPos pos, int amount, UncraftEverythingConfig.ExperienceType experienceType) implements CustomPacketPayload {
    public static Type<ServerBoundExpTransferPayload> TYPE = new Type<>(Constants.id("exp_transfer_payload"));

    public static StreamCodec<RegistryFriendlyByteBuf, ServerBoundExpTransferPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ServerBoundExpTransferPayload::pos,
            ByteBufCodecs.INT,
            ServerBoundExpTransferPayload::amount,
            UncraftEverythingConfig.ExperienceType.STREAM_CODEC,
            ServerBoundExpTransferPayload::experienceType,
            ServerBoundExpTransferPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(PayloadContext context){
        context.execute(() -> {
            if (context.player() instanceof ServerPlayer player){
                ServerLevel level = player.level();
                BlockPos pos = this.pos();

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof AutoUncraftingTableBlockEntity uncraftingTableBlockEntity) {
                    if (this.experienceType() == UncraftEverythingConfig.ExperienceType.LEVEL){
                        uncraftingTableBlockEntity.addExperienceLevels(this.amount(), player);
                    }
                    else {
                        uncraftingTableBlockEntity.addExperiencePoints(this.amount(), player);
                    }
                }
            }
        });
    }
}