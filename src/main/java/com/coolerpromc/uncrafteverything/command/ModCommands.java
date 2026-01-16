package com.coolerpromc.uncrafteverything.command;

import com.coolerpromc.uncrafteverything.compat.ftbquests.QuestHelper;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import com.coolerpromc.uncrafteverything.screen.custom.FTBQuestsProgressionConfigScreen;
import com.coolerpromc.uncrafteverything.screen.custom.PerItemExpConfigScreen;
import com.coolerpromc.uncrafteverything.screen.custom.UEClientConfigScreen;
import com.coolerpromc.uncrafteverything.screen.custom.UEConfigScreen;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.world.entity.player.Player;

public class ModCommands {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher){
        dispatcher.register(ClientCommands.literal("ueconfig")
                .then(ClientCommands.literal("common").requires(ModCommands::isCreativeOrHasPermission).executes(ModCommands::common))
                .then(ClientCommands.literal("exp").requires(ModCommands::isCreativeOrHasPermission).executes(ModCommands::exp))
                .then(ClientCommands.literal("progression").requires(ModCommands::hasFTBQuest).executes(ModCommands::progression))
                .then(ClientCommands.literal("client").executes(ModCommands::client)));
    }

    private static boolean isCreativeOrHasPermission(FabricClientCommandSource ServerCommandSource){
        Player player = Minecraft.getInstance().player;
        if (player == null) return false;

        return player.isCreative() || GameModeCommand.PERMISSION_CHECK.check(player.permissions());
    }

    private static boolean hasFTBQuest(FabricClientCommandSource ServerCommandSource){
        return isCreativeOrHasPermission(ServerCommandSource) && QuestHelper.FTBQUESTS_LOADED;
    }

    private static int common(CommandContext<FabricClientCommandSource> context){
        Minecraft.getInstance().schedule(() -> {
            ClientPlayNetworking.send(new RequestConfigPayload());
            Minecraft.getInstance().setScreen(new UEConfigScreen(Component.translatable("screen.uncrafteverything.uncraft_everything_config")));
        });
        return 1;
    }

    private static int exp(CommandContext<FabricClientCommandSource> context){
        Minecraft.getInstance().schedule(() -> {
            ClientPlayNetworking.send(new RequestConfigPayload());
            Minecraft.getInstance().setScreen(new PerItemExpConfigScreen(Component.translatable("screen.uncrafteverything.per_item_xp_config")));
        });
        return 1;
    }

    private static int client(CommandContext<FabricClientCommandSource> context){
        Minecraft.getInstance().schedule(() -> {
            Minecraft.getInstance().setScreen(new UEClientConfigScreen(Component.translatable("screen.uncrafteverything.uncraft_everything_client_config")));
        });
        return 1;
    }

    private static int progression(CommandContext<FabricClientCommandSource> context){
        Minecraft.getInstance().schedule(() -> {
            ClientPlayNetworking.send(new RequestConfigPayload());
            Minecraft.getInstance().setScreen(new FTBQuestsProgressionConfigScreen(Component.translatable("screen.uncrafteverything.ftb_quest_progression_config")));
        });
        return 1;
    }
}