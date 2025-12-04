package com.coolerpromc.uncrafteverything.command;

import com.coolerpromc.uncrafteverything.compat.ftbquests.QuestHelper;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.GameModeCommand;
import net.minecraft.server.command.ServerCommandSource;

public class ModServerCommands {
    public static void registerServer(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(CommandManager.literal("ueconfig")
                .then(CommandManager.literal("common").requires(ModServerCommands::isCreativeOrHasPermission).executes(s -> 1))
                .then(CommandManager.literal("exp").requires(ModServerCommands::isCreativeOrHasPermission).executes(s -> 1))
                .then(CommandManager.literal("progression").requires(ModServerCommands::hasFTBQuest).executes(s -> 1))
                .then(CommandManager.literal("client").executes(s -> 1)));
    }

    private static boolean isCreativeOrHasPermission(ServerCommandSource source){
        PlayerEntity player = source.getPlayer();
        if (player == null) return false;
        return player.isCreative() || source.hasPermissionLevel(4);
    }

    private static boolean hasFTBQuest(ServerCommandSource source){
        return isCreativeOrHasPermission(source) && QuestHelper.FTBQUESTS_LOADED;
    }

}