package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public record TypeChangePayload(BlockPos blockPos, UncraftEverythingConfig.ExperienceType expType) {
    private static final String PROTOCOL_VERSION = "1";
    public static final ResourceLocation TYPE = new ResourceLocation(UncraftEverything.MODID, "type_change_payload");
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(TYPE,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(TypeChangePayload payload, FriendlyByteBuf byteBuf){
        byteBuf.writeBlockPos(payload.blockPos);
        byteBuf.writeEnum(payload.expType);
    }

    public static TypeChangePayload decode(FriendlyByteBuf byteBuf){
        return new TypeChangePayload(byteBuf.readBlockPos(), byteBuf.readEnum(UncraftEverythingConfig.ExperienceType.class));
    }

    public static void register(){
        INSTANCE.registerMessage(
                nextId(),
                TypeChangePayload.class,
                TypeChangePayload::encode,
                TypeChangePayload::decode,
                ServerPayloadHandler::handleTypeChange
        );
    }
}
