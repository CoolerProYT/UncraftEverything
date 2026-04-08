package com.coolerpromc.uncrafteverything.command;

import com.coolerpromc.uncrafteverything.compat.ftbquests.QuestHelper;
import com.coolerpromc.uncrafteverything.networking.ServerBoundRequestConfigPayload;
import com.coolerpromc.uncrafteverything.platform.Services;
import com.coolerpromc.uncrafteverything.screen.custom.FTBQuestsProgressionConfigScreen;
import com.coolerpromc.uncrafteverything.screen.custom.PerItemExpConfigScreen;
import com.coolerpromc.uncrafteverything.screen.custom.UEClientConfigScreen;
import com.coolerpromc.uncrafteverything.screen.custom.UEConfigScreen;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.world.entity.player.Player;

public class ModCommands {
    public static <T extends SharedSuggestionProvider> void register(CommandDispatcher<T> dispatcher){
        dispatcher.register(LiteralArgumentBuilder.<T>literal("ueconfig")
                .then(LiteralArgumentBuilder.<T>literal("common").requires(ModCommands::isCreativeOrHasPermission).executes(ModCommands::common))
                .then(LiteralArgumentBuilder.<T>literal("exp").requires(ModCommands::isCreativeOrHasPermission).executes(ModCommands::exp))
                .then(LiteralArgumentBuilder.<T>literal("progression").requires(ModCommands::hasFTBQuest).executes(ModCommands::progression))
                .then(LiteralArgumentBuilder.<T>literal("client").executes(ModCommands::client)));
    }

    private static <T extends SharedSuggestionProvider> boolean isCreativeOrHasPermission(T ServerCommandSource){
        Player player = Minecraft.getInstance().player;
        if (player == null) return false;

        return player.isCreative() || GameModeCommand.PERMISSION_CHECK.check(player.permissions());
    }

    private static <T extends SharedSuggestionProvider> boolean hasFTBQuest(T ServerCommandSource){
        return isCreativeOrHasPermission(ServerCommandSource) && QuestHelper.FTBQUESTS_LOADED;
    }

    private static <T extends SharedSuggestionProvider> int common(CommandContext<T> context){
        Minecraft.getInstance().schedule(() -> {
            Services.NETWORK.sendToServer(new ServerBoundRequestConfigPayload());
            Minecraft.getInstance().setScreen(new UEConfigScreen(Component.translatable("screen.uncrafteverything.uncraft_everything_config")));
        });
        return 1;
    }

    private static <T extends SharedSuggestionProvider> int exp(CommandContext<T> context){
        Minecraft.getInstance().schedule(() -> {
            Services.NETWORK.sendToServer(new ServerBoundRequestConfigPayload());
            Minecraft.getInstance().setScreen(new PerItemExpConfigScreen(Component.translatable("screen.uncrafteverything.per_item_xp_config")));
        });
        return 1;
    }

    private static <T extends SharedSuggestionProvider> int client(CommandContext<T> context){
        Minecraft.getInstance().schedule(() -> {
            Minecraft.getInstance().setScreen(new UEClientConfigScreen(Component.translatable("screen.uncrafteverything.uncraft_everything_client_config")));
        });
        return 1;
    }

    private static <T extends SharedSuggestionProvider> int progression(CommandContext<T> context){
        Minecraft.getInstance().schedule(() -> {
            Services.NETWORK.sendToServer(new ServerBoundRequestConfigPayload());
            Minecraft.getInstance().setScreen(new FTBQuestsProgressionConfigScreen(Component.translatable("screen.uncrafteverything.ftb_quest_progression_config")));
        });
        return 1;
    }
}