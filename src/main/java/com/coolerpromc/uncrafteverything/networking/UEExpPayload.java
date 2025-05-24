package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.util.BufferUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Map;

public class UEExpPayload {
    public static final ResourceLocation TYPE = new ResourceLocation(UncraftEverything.MODID, "ue_exp_payload");
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(TYPE,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private final Map<String, Integer> perItemExp;

    public UEExpPayload(Map<String, Integer> perItemExp){
        this.perItemExp = perItemExp;
    }

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(UEExpPayload payload, PacketBuffer byteBuf){
        BufferUtil.writeMap(byteBuf, payload.perItemExp);
    }

    public static UEExpPayload decode(PacketBuffer byteBuf){
        Map<String, Integer> perItemExp = BufferUtil.readMap(byteBuf);
        return new UEExpPayload(perItemExp);
    }

    public static void register(){
        INSTANCE.registerMessage(
                nextId(),
                UEExpPayload.class,
                UEExpPayload::encode,
                UEExpPayload::decode,
                ServerPayloadHandler::handleExpCost
        );
    }

    public Map<String, Integer> perItemExp() {
        return perItemExp;
    }
}