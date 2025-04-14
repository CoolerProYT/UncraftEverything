package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record RecipeSyncPayload(List<RecipeHolder<?>> recipes) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(UncraftEverything.MODID, "recipe_sync");
    public static final Type<RecipeSyncPayload> TYPE = new Type<>(ID);

    private static final StreamCodec<FriendlyByteBuf, List<RecipeHolder<?>>> RECIPE_HOLDER_STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> buffer.writeCollection((Collection<?>) value, (buf, recipeHolder) -> RecipeHolder.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, (RecipeHolder<?>) recipeHolder)),
            buffer -> buffer.readCollection(ArrayList::new, buf -> RecipeHolder.STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf))
    );

    public static final StreamCodec<FriendlyByteBuf, RecipeSyncPayload> STREAM_CODEC = StreamCodec.composite(
            RECIPE_HOLDER_STREAM_CODEC,
            RecipeSyncPayload::recipes,
            RecipeSyncPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
