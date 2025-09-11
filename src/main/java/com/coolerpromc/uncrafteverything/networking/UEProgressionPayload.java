package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record UEProgressionPayload(Map<String, String> progressionMap) implements CustomPayload {
    public static final Id<UEProgressionPayload> TYPE = new Id<>(Identifier.of(UncraftEverything.MODID, "ue_progression_payload"));

    public static final PacketCodec<RegistryByteBuf, UEProgressionPayload> STREAM_CODEC =
            PacketCodec.tuple(
                    PacketCodecs.map(HashMap::new, PacketCodecs.STRING, PacketCodecs.STRING),
                    UEProgressionPayload::progressionMap,
                    UEProgressionPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}