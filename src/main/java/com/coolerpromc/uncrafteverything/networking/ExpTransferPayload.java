package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public record ExpTransferPayload(BlockPos pos, int amount, UncraftEverythingConfig.ExperienceType experienceType) {
    private static final String PROTOCOL_VERSION = "1";
    public static ResourceLocation TYPE = new ResourceLocation(UncraftEverything.MODID, "exp_transfer_payload");
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(TYPE,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(ExpTransferPayload payload, FriendlyByteBuf byteBuf){
        byteBuf.writeBlockPos(payload.pos);
        byteBuf.writeInt(payload.amount);
        byteBuf.writeEnum(payload.experienceType);
    }

    public static ExpTransferPayload decode(FriendlyByteBuf byteBuf){
        return new ExpTransferPayload(byteBuf.readBlockPos(), byteBuf.readInt(), byteBuf.readEnum(UncraftEverythingConfig.ExperienceType.class));
    }

    public static void register(){
        INSTANCE.registerMessage(
                nextId(),
                ExpTransferPayload.class,
                ExpTransferPayload::encode,
                ExpTransferPayload::decode,
                ServerPayloadHandler::handleExpTransfer
        );
    }
}
