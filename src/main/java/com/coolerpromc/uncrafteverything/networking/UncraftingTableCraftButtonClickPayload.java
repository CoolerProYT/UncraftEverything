package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record UncraftingTableCraftButtonClickPayload(BlockPos blockPos, String data) implements CustomPayload {
    public static final CustomPayload.Id<UncraftingTableCraftButtonClickPayload> TYPE = new Id<>(Identifier.of(UncraftEverything.MODID, "uncrafting_table_craft_button_click"));

    public static final PacketCodec<ByteBuf, UncraftingTableCraftButtonClickPayload> STREAM_CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC,
                    UncraftingTableCraftButtonClickPayload::blockPos,
                    PacketCodecs.STRING,
                    UncraftingTableCraftButtonClickPayload::data,
                    UncraftingTableCraftButtonClickPayload::new
            );


    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}