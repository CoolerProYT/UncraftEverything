package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public record UncraftingTableCraftButtonClickPayload(BlockPos blockPos) {
    private static final String PROTOCOL_VERSION = "1";
    public static final ResourceLocation TYPE = new ResourceLocation(UncraftEverything.MODID, "uncrafting_table_craft_button_click");
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(TYPE,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static final Codec<UncraftingTableCraftButtonClickPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("blockPos").forGetter(UncraftingTableCraftButtonClickPayload::blockPos)
    ).apply(instance, UncraftingTableCraftButtonClickPayload::new));

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(UncraftingTableCraftButtonClickPayload payload, FriendlyByteBuf byteBuf){
        byteBuf.writeJsonWithCodec(CODEC, payload);
    }

    public static UncraftingTableCraftButtonClickPayload decode(FriendlyByteBuf byteBuf){
        return byteBuf.readJsonWithCodec(CODEC);
    }

    public static void register(){
        INSTANCE.registerMessage(
                nextId(),
                UncraftingTableCraftButtonClickPayload.class,
                UncraftingTableCraftButtonClickPayload::encode,
                UncraftingTableCraftButtonClickPayload::decode,
                ServerPayloadHandler::handleButtonClick
        );
    }
}
