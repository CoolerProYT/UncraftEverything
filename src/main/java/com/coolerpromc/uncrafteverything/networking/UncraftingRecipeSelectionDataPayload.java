package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

@SuppressWarnings("removal")
public class UncraftingRecipeSelectionDataPayload{
    private final int page;
    private final BlockPos blockPos;

    public UncraftingRecipeSelectionDataPayload(int page, BlockPos blockPos){
        this.page = page;
        this.blockPos = blockPos;
    }

    private static final String PROTOCOL_VERSION = "1";
    public static final ResourceLocation TYPE = new ResourceLocation(UncraftEverything.MODID, "uncrafting_recipe_selection_data_payload");
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(TYPE,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void encode(UncraftingRecipeSelectionDataPayload payload, PacketBuffer byteBuf){
        byteBuf.writeVarInt(payload.page);
        byteBuf.writeBlockPos(payload.blockPos);
    }

    public static UncraftingRecipeSelectionDataPayload decode(PacketBuffer byteBuf){
        int page = byteBuf.readVarInt();
        BlockPos blockPos = byteBuf.readBlockPos();
        return new UncraftingRecipeSelectionDataPayload(page, blockPos);
    }

    public static void register() {
        INSTANCE.registerMessage(
                nextId(),
                UncraftingRecipeSelectionDataPayload.class,
                UncraftingRecipeSelectionDataPayload::encode,
                UncraftingRecipeSelectionDataPayload::decode,
                ServerPayloadHandler::handleRecipeSelectionData
        );
    }

    public int page(){
        return page;
    }

    public BlockPos blockPos(){
        return blockPos;
    }
}
