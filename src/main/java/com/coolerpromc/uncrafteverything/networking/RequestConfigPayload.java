package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class RequestConfigPayload{
    private static final String PROTOCOL_VERSION = "1";
    public static final ResourceLocation TYPE = new ResourceLocation(UncraftEverything.MODID, "request_config");
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(TYPE,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(RequestConfigPayload payload, PacketBuffer byteBuf){

    }

    public static RequestConfigPayload decode(PacketBuffer byteBuf){
        return new RequestConfigPayload();
    }

    public static void register(){
        INSTANCE.registerMessage(
                nextId(),
                RequestConfigPayload.class,
                RequestConfigPayload::encode,
                RequestConfigPayload::decode,
                ServerPayloadHandler::handleRequestConfig
        );
    }
}