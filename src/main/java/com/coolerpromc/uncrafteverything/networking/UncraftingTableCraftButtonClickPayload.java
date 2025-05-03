package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.io.IOException;

public class UncraftingTableCraftButtonClickPayload {
    private final BlockPos blockPos;

    public UncraftingTableCraftButtonClickPayload(BlockPos blockPos){
        this.blockPos = blockPos;
    }

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

    public static void encode(UncraftingTableCraftButtonClickPayload payload, PacketBuffer byteBuf){
       try{
           byteBuf.writeWithCodec(CODEC, payload);
       } catch (IOException e) {
           System.out.println("Failed to encode UncraftingTableCraftButtonClickPayload: " + e.getMessage());
       }
    }

    public static UncraftingTableCraftButtonClickPayload decode(PacketBuffer byteBuf){
        try{
            return byteBuf.readWithCodec(CODEC);
        } catch (IOException e) {
            System.out.println("Failed to decode UncraftingTableCraftButtonClickPayload: " + e.getMessage());
            return null;
        }
    }

    public BlockPos blockPos() {
        return blockPos;
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
