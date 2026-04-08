package com.coolerpromc.uncrafteverything.command;

import com.coolerpromc.uncrafteverything.compat.ftbquests.QuestHelper;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.world.entity.player.Player;

public class ModServerCommands {
    public static void registerServer(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("ueconfig")
                .then(Commands.literal("common").requires(ModServerCommands::isCreativeOrHasPermission).executes(s -> 1))
                .then(Commands.literal("exp").requires(ModServerCommands::isCreativeOrHasPermission).executes(s -> 1))
                .then(Commands.literal("progression").requires(ModServerCommands::hasFTBQuest).executes(s -> 1))
                .then(Commands.literal("client").executes(s -> 1)));
    }

    private static boolean isCreativeOrHasPermission(CommandSourceStack source){
        Player player = source.getPlayer();
        if (player == null) return false;
        return player.isCreative() || GameModeCommand.PERMISSION_CHECK.check(player.permissions());
    }

    private static boolean hasFTBQuest(CommandSourceStack source){
        return isCreativeOrHasPermission(source) && QuestHelper.FTBQUESTS_LOADED;
    }

}