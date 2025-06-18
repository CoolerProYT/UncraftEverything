package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record RecipeSyncPayload(List<RecipeEntry<?>> recipes) implements CustomPayload {
    public static final Identifier ID = Identifier.of(UncraftEverything.MODID, "recipe_sync");
    public static final Id<RecipeSyncPayload> TYPE = new Id<>(ID);
    private static final PacketCodec<PacketByteBuf, List<RecipeEntry<?>>> RECIPE_HOLDER_STREAM_CODEC = PacketCodec.of(
            (value, buf) -> buf.writeCollection(value, (buffer, recipeHolder) -> RecipeEntry.PACKET_CODEC.encode((RegistryByteBuf) buffer, recipeHolder)),
            buffer -> buffer.readCollection(ArrayList::new, buf -> RecipeEntry.PACKET_CODEC.decode((RegistryByteBuf) buf))
    );
    public static final PacketCodec<PacketByteBuf, RecipeSyncPayload> STREAM_CODEC = PacketCodec.tuple(
            RECIPE_HOLDER_STREAM_CODEC,
            RecipeSyncPayload::recipes,
            RecipeSyncPayload::new
    );

    @Override
    public List<RecipeEntry<?>> recipes() {
        return recipes;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}