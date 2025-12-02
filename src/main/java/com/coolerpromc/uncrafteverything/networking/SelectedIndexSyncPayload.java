package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public record SelectedIndexSyncPayload(BlockPos blockPos, int index) {
    private static final String PROTOCOL_VERSION = "1";
    public static final ResourceLocation TYPE = new ResourceLocation(UncraftEverything.MODID, "selected_index_sync_payload");
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(TYPE,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(SelectedIndexSyncPayload payload, FriendlyByteBuf byteBuf){
        byteBuf.writeBlockPos(payload.blockPos);
        byteBuf.writeInt(payload.index);
    }

    public static SelectedIndexSyncPayload decode(FriendlyByteBuf byteBuf){
        return new SelectedIndexSyncPayload(byteBuf.readBlockPos(), byteBuf.readInt());
    }

    public static void register(){
        INSTANCE.registerMessage(
                nextId(),
                SelectedIndexSyncPayload.class,
                SelectedIndexSyncPayload::encode,
                SelectedIndexSyncPayload::decode,
                ServerPayloadHandler::handleIndexSync
        );
    }
}
