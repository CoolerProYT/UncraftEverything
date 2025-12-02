package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public record CloseMenuPayload(BlockPos pos) {
    private static final String PROTOCOL_VERSION = "1";
    public static final ResourceLocation TYPE = new ResourceLocation(UncraftEverything.MODID, "close_menu_payload");
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(TYPE,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(CloseMenuPayload payload, FriendlyByteBuf byteBuf){
        byteBuf.writeBlockPos(payload.pos);
    }

    public static CloseMenuPayload decode(FriendlyByteBuf byteBuf){
        return new CloseMenuPayload(byteBuf.readBlockPos());
    }

    public static void register(){
        INSTANCE.registerMessage(
                nextId(),
                CloseMenuPayload.class,
                CloseMenuPayload::encode,
                CloseMenuPayload::decode,
                ServerPayloadHandler::handleCloseMenu
        );
    }
}
