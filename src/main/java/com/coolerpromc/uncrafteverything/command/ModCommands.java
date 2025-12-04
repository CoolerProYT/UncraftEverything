package com.coolerpromc.uncrafteverything.command;

import com.coolerpromc.uncrafteverything.compat.ftbquests.QuestHelper;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import com.coolerpromc.uncrafteverything.screen.custom.FTBQuestsProgressionConfigScreen;
import com.coolerpromc.uncrafteverything.screen.custom.PerItemExpConfigScreen;
import com.coolerpromc.uncrafteverything.screen.custom.UEClientConfigScreen;
import com.coolerpromc.uncrafteverything.screen.custom.UEConfigScreen;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.GameModeCommand;
import net.minecraft.text.Text;

public class ModCommands {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher){
        dispatcher.register(ClientCommandManager.literal("ueconfig")
                .then(ClientCommandManager.literal("common").requires(ModCommands::isCreativeOrHasPermission).executes(ModCommands::common))
                .then(ClientCommandManager.literal("exp").requires(ModCommands::isCreativeOrHasPermission).executes(ModCommands::exp))
                .then(ClientCommandManager.literal("progression").requires(ModCommands::hasFTBQuest).executes(ModCommands::progression))
                .then(ClientCommandManager.literal("client").executes(ModCommands::client)));
    }

    private static boolean isCreativeOrHasPermission(FabricClientCommandSource commandSource){
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return false;

        return player.isCreative() || commandSource.getPlayer().hasPermissionLevel(4);
    }

    private static boolean hasFTBQuest(FabricClientCommandSource ServerCommandSource){
        return isCreativeOrHasPermission(ServerCommandSource) && QuestHelper.FTBQUESTS_LOADED;
    }

    private static int common(CommandContext<FabricClientCommandSource> context){
        MinecraftClient.getInstance().send(() -> {
            ClientPlayNetworking.send(new RequestConfigPayload());
            MinecraftClient.getInstance().setScreen(new UEConfigScreen(Text.translatable("screen.uncrafteverything.uncraft_everything_config")));
        });
        return 1;
    }

    private static int exp(CommandContext<FabricClientCommandSource> context){
        MinecraftClient.getInstance().send(() -> {
            ClientPlayNetworking.send(new RequestConfigPayload());
            MinecraftClient.getInstance().setScreen(new PerItemExpConfigScreen(Text.translatable("screen.uncrafteverything.per_item_xp_config")));
        });
        return 1;
    }

    private static int client(CommandContext<FabricClientCommandSource> context){
        MinecraftClient.getInstance().send(() -> {
            MinecraftClient.getInstance().setScreen(new UEClientConfigScreen(Text.translatable("screen.uncrafteverything.uncraft_everything_client_config")));
        });
        return 1;
    }

    private static int progression(CommandContext<FabricClientCommandSource> context){
        MinecraftClient.getInstance().send(() -> {
            ClientPlayNetworking.send(new RequestConfigPayload());
            MinecraftClient.getInstance().setScreen(new FTBQuestsProgressionConfigScreen(Text.translatable("screen.uncrafteverything.ftb_quest_progression_config")));
        });
        return 1;
    }
}