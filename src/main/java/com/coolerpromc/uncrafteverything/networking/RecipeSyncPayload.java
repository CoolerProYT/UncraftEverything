package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;

public record RecipeSyncPayload(List<RecipeHolder<?>> recipes, int totalRecipe) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(UncraftEverything.MODID, "recipe_sync");
    public static final Type<RecipeSyncPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeSyncPayload> STREAM_CODEC = StreamCodec.composite(
            RecipeHolder.STREAM_CODEC.apply(ByteBufCodecs.list()), RecipeSyncPayload::recipes,
            ByteBufCodecs.INT, RecipeSyncPayload::totalRecipe,
            RecipeSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}