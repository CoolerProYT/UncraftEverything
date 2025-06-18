package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public record RecipeSyncPayload(List<RecipeEntry<?>> recipes, int totalRecipe) implements CustomPayload {
    public static final Identifier ID = Identifier.of(UncraftEverything.MODID, "recipe_sync");
    public static final Id<RecipeSyncPayload> TYPE = new Id<>(ID);
    public static final PacketCodec<RegistryByteBuf, RecipeSyncPayload> STREAM_CODEC = PacketCodec.tuple(
            RecipeEntry.PACKET_CODEC.collect(PacketCodecs.toList()), RecipeSyncPayload::recipes,
            PacketCodecs.INTEGER, RecipeSyncPayload::totalRecipe,
            RecipeSyncPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}