package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.config.FTBQuestProgressionConfig;
import com.coolerpromc.uncrafteverything.platform.util.PayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public record ServerBoundUEProgressionPayload(Map<String, String> progressionMap) implements CustomPacketPayload {
    public static final Type<ServerBoundUEProgressionPayload> TYPE = new Type<>(Constants.id("ue_progression_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundUEProgressionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8),
                    ServerBoundUEProgressionPayload::progressionMap,
                    ServerBoundUEProgressionPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(PayloadContext context){
        context.execute(() -> {
            if (context.player() instanceof ServerPlayer){
                FTBQuestProgressionConfig.CONFIG.progressionMap.set(this.progressionMap());
                FTBQuestProgressionConfig.CONFIG.save();
            }
        });
    }
}