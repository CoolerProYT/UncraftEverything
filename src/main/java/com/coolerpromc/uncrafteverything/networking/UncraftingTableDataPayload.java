package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.util.UncraftingTableRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record UncraftingTableDataPayload(BlockPos blockPos, List<UncraftingTableRecipe> recipes) {
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

    public static void encode(UncraftingTableDataPayload payload, FriendlyByteBuf byteBuf){
        byteBuf.writeBlockPos(payload.blockPos());
        byteBuf.writeVarInt(payload.recipes().size());

        for (UncraftingTableRecipe recipe : payload.recipes()) {
            recipe.writeToBuf(byteBuf);
        }
    }

    public static UncraftingTableDataPayload decode(FriendlyByteBuf byteBuf){
        BlockPos blockPos = byteBuf.readBlockPos();
        int recipeCount = byteBuf.readVarInt();
        List<UncraftingTableRecipe> recipes = new ArrayList<>();

        for (int i = 0; i < recipeCount; i++) {
            recipes.add(UncraftingTableRecipe.readFromBuf(byteBuf));
        }

        return new UncraftingTableDataPayload(blockPos, recipes);
    }

    private static java.util.function.BiConsumer<UncraftingTableDataPayload, Supplier<NetworkEvent.Context>> getHandler() {
        return DistExecutor.unsafeCallWhenOn(
                net.minecraftforge.api.distmarker.Dist.CLIENT,
                () -> () -> ClientPayloadHandler::handleBlockEntityData
        );
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
