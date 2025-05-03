package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

public class UncraftingTableDataPayload {
    private final BlockPos blockPos;
    private final List<UncraftingTableRecipe> recipes;

    public UncraftingTableDataPayload(BlockPos blockPos, List<UncraftingTableRecipe> recipes){
        this.blockPos = blockPos;
        this.recipes = recipes;
    }

    private static final String PROTOCOL_VERSION = "1";
    public static final ResourceLocation TYPE = new ResourceLocation(UncraftEverything.MODID, "uncrafting_table_data");
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(TYPE,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static final Codec<UncraftingTableDataPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("blockPos").forGetter(UncraftingTableDataPayload::blockPos),
            UncraftingTableRecipe.CODEC.listOf().fieldOf("recipes").forGetter(UncraftingTableDataPayload::recipes)
    ).apply(instance, UncraftingTableDataPayload::new));

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(UncraftingTableDataPayload payload, PacketBuffer byteBuf){
        try{
            byteBuf.writeWithCodec(CODEC, payload);
        }
        catch (IOException e){
            System.out.println("Failed to encode UncraftingTableDataPayload: " + e.getMessage());
        }
    }

    public static UncraftingTableDataPayload decode(PacketBuffer byteBuf){
        try{
            return byteBuf.readWithCodec(CODEC);
        }
        catch (IOException e){
            System.out.println("Failed to decode UncraftingTableDataPayload: " + e.getMessage());
            return null;
        }
    }

    private static java.util.function.BiConsumer<UncraftingTableDataPayload, Supplier<NetworkEvent.Context>> getHandler() {
        return DistExecutor.unsafeCallWhenOn(
                net.minecraftforge.api.distmarker.Dist.CLIENT,
                () -> () -> ClientPayloadHandler::handleBlockEntityData
        );
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    public List<UncraftingTableRecipe> recipes() {
        return recipes;
    }

    public static void register(){
        INSTANCE.registerMessage(
                nextId(),
                UncraftingTableDataPayload.class,
                UncraftingTableDataPayload::encode,
                UncraftingTableDataPayload::decode,
                getHandler()
        );
    }
}
