package com.coolerpromc.uncrafteverything;

import com.coolerpromc.uncrafteverything.networking.ClientBoundResponseConfigPayload;
import com.coolerpromc.uncrafteverything.networking.ClientBoundUncraftingRecipeSelectionRequestPayload;
import com.coolerpromc.uncrafteverything.networking.ClientBoundUncraftingTableDataPayload;
import com.coolerpromc.uncrafteverything.platform.util.NeoForgePayloadContext;
import com.coolerpromc.uncrafteverything.screen.UEMenuTypes;
import com.coolerpromc.uncrafteverything.screen.custom.AutoUncraftingTableScreen;
import com.coolerpromc.uncrafteverything.screen.custom.UncraftingTableScreen;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

@Mod(value = Constants.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public class UncraftEverythingClient {
    public UncraftEverythingClient(IEventBus modBus) {

    }

    @SubscribeEvent
    public static void onRegisterClientPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(ClientBoundResponseConfigPayload.TYPE, (payload, context) -> ClientBoundResponseConfigPayload.ClientHandler.handle(payload, new NeoForgePayloadContext(context)));
        event.register(ClientBoundUncraftingRecipeSelectionRequestPayload.TYPE, (payload, context) -> ClientBoundUncraftingRecipeSelectionRequestPayload.ClientHandler.handle(payload, new NeoForgePayloadContext(context)));
        event.register(ClientBoundUncraftingTableDataPayload.TYPE, (payload, context) -> ClientBoundUncraftingTableDataPayload.ClientHandler.handle(payload, new NeoForgePayloadContext(context)));
    }

    @SubscribeEvent
    public static void onRecipesReceived(RecipesReceivedEvent event) {
        RecipeMap recipes = event.getRecipeMap();
        CommonClientClass.recipesFromServer.clear();
        CommonClientClass.recipesFromServer.addAll(recipes.byType(RecipeType.CRAFTING));
        CommonClientClass.recipesFromServer.addAll(recipes.byType(RecipeType.SMITHING));
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(UEMenuTypes.UNCRAFTING_TABLE_MENU.get(), UncraftingTableScreen::new);
        event.register(UEMenuTypes.AUTO_UNCRAFTING_TABLE_MENU.get(), AutoUncraftingTableScreen::new);
    }
}
