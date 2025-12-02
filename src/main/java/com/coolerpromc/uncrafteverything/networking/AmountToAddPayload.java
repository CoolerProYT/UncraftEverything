package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public record AmountToAddPayload(BlockPos blockPos, int index) {
    private static final String PROTOCOL_VERSION = "1";
    public static final ResourceLocation TYPE = new ResourceLocation(UncraftEverything.MODID, "amount_to_add_payload");
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(TYPE,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(AmountToAddPayload payload, FriendlyByteBuf byteBuf){
        byteBuf.writeBlockPos(payload.blockPos);
        byteBuf.writeInt(payload.index);
    }

    public static AmountToAddPayload decode(FriendlyByteBuf byteBuf){
        return new AmountToAddPayload(byteBuf.readBlockPos(), byteBuf.readInt());
    }

    public static void register(){
        INSTANCE.registerMessage(
                nextId(),
                AmountToAddPayload.class,
                AmountToAddPayload::encode,
                AmountToAddPayload::decode,
                ServerPayloadHandler::handleAmountChange
        );
    }
}
