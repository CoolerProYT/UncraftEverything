package com.coolerpromc.uncrafteverything.command;

import com.coolerpromc.uncrafteverything.compat.ftbquests.QuestHelper;
import com.coolerpromc.uncrafteverything.networking.RequestConfigPayload;
import com.coolerpromc.uncrafteverything.screen.custom.FTBQuestsProgressionConfigScreen;
import com.coolerpromc.uncrafteverything.screen.custom.PerItemExpConfigScreen;
import com.coolerpromc.uncrafteverything.screen.custom.UEClientConfigScreen;
import com.coolerpromc.uncrafteverything.screen.custom.UEConfigScreen;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("ueconfig")
                .then(Commands.literal("common").requires(ModCommands::isCreativeOrHasPermission).executes(ModCommands::common))
                .then(Commands.literal("exp").requires(ModCommands::isCreativeOrHasPermission).executes(ModCommands::exp))
                .then(Commands.literal("progression").requires(ModCommands::hasFTBQuest).executes(ModCommands::progression))
                .then(Commands.literal("client").executes(ModCommands::client)));
    }

    private static boolean isCreativeOrHasPermission(CommandSourceStack commandSourceStack){
        Player player = Minecraft.getInstance().player;
        if (player == null) return false;

        return player.isCreative() || commandSourceStack.hasPermission(4);
    }

    private static boolean hasFTBQuest(CommandSourceStack commandSourceStack){
        return isCreativeOrHasPermission(commandSourceStack) && QuestHelper.FTBQUESTS_LOADED;
    }

    private static int common(CommandContext<CommandSourceStack> context){
        Minecraft.getInstance().schedule(() -> {
            ClientPacketDistributor.sendToServer(new RequestConfigPayload());
            Minecraft.getInstance().setScreen(new UEConfigScreen(Component.translatable("screen.uncrafteverything.uncraft_everything_config")));
        });
        return 1;
    }

    private static int exp(CommandContext<CommandSourceStack> context){
        Minecraft.getInstance().schedule(() -> {
            ClientPacketDistributor.sendToServer(new RequestConfigPayload());
            Minecraft.getInstance().setScreen(new PerItemExpConfigScreen(Component.translatable("screen.uncrafteverything.per_item_xp_config")));
        });
        return 1;
    }

    private static int client(CommandContext<CommandSourceStack> context){
        Minecraft.getInstance().schedule(() -> {
            Minecraft.getInstance().setScreen(new UEClientConfigScreen(Component.translatable("screen.uncrafteverything.uncraft_everything_client_config")));
        });
        return 1;
    }

    private static int progression(CommandContext<CommandSourceStack> context){
        Minecraft.getInstance().schedule(() -> {
            ClientPacketDistributor.sendToServer(new RequestConfigPayload());
            Minecraft.getInstance().setScreen(new FTBQuestsProgressionConfigScreen(Component.translatable("screen.uncrafteverything.ftb_quest_progression_config")));
        });
        return 1;
    }
}
