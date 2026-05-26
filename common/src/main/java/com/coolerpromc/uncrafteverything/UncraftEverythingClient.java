package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.networking.ClientBoundResponseConfigPayload;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

public class UncraftEverythingClient {
    public static ClientBoundResponseConfigPayload payloadFromServer;
    public static List<RecipeHolder<?>> recipesFromServer = new ArrayList<>();
}
