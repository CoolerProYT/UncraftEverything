package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.io.IOException;

public class UncraftingRecipeSelectionPayload {
    public final BlockPos blockPos;
    public final UncraftingTableRecipe recipe;

    public UncraftingRecipeSelectionPayload(BlockPos blockPos, UncraftingTableRecipe recipe){
        this.blockPos = blockPos;
        this.recipe = recipe;
    }

    private static final String PROTOCOL_VERSION = "1";
    public static final ResourceLocation TYPE = new ResourceLocation(UncraftEverything.MODID, "uncrafting_table_recipe_selection");
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(TYPE,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static final Codec<UncraftingRecipeSelectionPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("blockPos").forGetter(UncraftingRecipeSelectionPayload::blockPos),
            UncraftingTableRecipe.CODEC.fieldOf("recipe").forGetter(UncraftingRecipeSelectionPayload::recipe)
    ).apply(instance, UncraftingRecipeSelectionPayload::new));

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(UncraftingRecipeSelectionPayload payload, PacketBuffer byteBuf){
        try{
            byteBuf.writeWithCodec(CODEC, payload);
        }
        catch (IOException e){
            System.out.println("Failed to encode UncraftingRecipeSelectionPayload: " + e.getMessage());
        }
    }

    public static UncraftingRecipeSelectionPayload decode(PacketBuffer byteBuf){
        try{
            return byteBuf.readWithCodec(CODEC);
        }
        catch (IOException e){
            System.out.println("Failed to decode UncraftingRecipeSelectionPayload: " + e.getMessage());
            return null;
        }
    }

    public BlockPos blockPos(){
        return blockPos;
    }

    public UncraftingTableRecipe recipe(){
        return recipe;
    }

    public static void register(){
        INSTANCE.registerMessage(
                nextId(),
                UncraftingRecipeSelectionPayload.class,
                UncraftingRecipeSelectionPayload::encode,
                UncraftingRecipeSelectionPayload::decode,
                ServerPayloadHandler::handleRecipeSelection
        );
    }
}
