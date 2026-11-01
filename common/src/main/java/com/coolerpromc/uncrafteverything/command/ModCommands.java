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
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class ModCommands {
    public static <S> void register(
            CommandDispatcher<S> dispatcher,
            java.util.function.Function<
                            String, com.mojang.brigadier.builder.LiteralArgumentBuilder<S>>
                    literal) {
        dispatcher.register(
                literal.apply("ueconfig")
                        .then(
                                literal.apply("common")
                                        .requires(ModCommands::isCreativeOrHasPermission)
                                        .executes(ModCommands::common))
                        .then(
                                literal.apply("exp")
                                        .requires(ModCommands::isCreativeOrHasPermission)
                                        .executes(ModCommands::exp))
                        .then(
                                literal.apply("progression")
                                        .requires(ModCommands::hasFTBQuest)
                                        .executes(ModCommands::progression))
                        .then(literal.apply("client").executes(ModCommands::client)));
    }

    private static boolean isCreativeOrHasPermission(Object commandSourceStack) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return false;

        return player.isCreative() || player.hasPermissions(4);
    }

    private static boolean hasFTBQuest(Object commandSourceStack) {
        return isCreativeOrHasPermission(commandSourceStack) && QuestHelper.FTBQUESTS_LOADED;
    }

    private static int common(CommandContext<?> context) {
        Minecraft.getInstance()
                .execute(
                        () -> {
                            RequestConfigPayload.INSTANCE.sendToServer(new RequestConfigPayload());
                            Minecraft.getInstance()
                                    .setScreen(
                                            new UEConfigScreen(
                                                    Component.translatable(
                                                            "screen.uncrafteverything.uncraft_everything_config")));
                        });
        return 1;
    }

    private static int exp(CommandContext<?> context) {
        Minecraft.getInstance()
                .execute(
                        () -> {
                            RequestConfigPayload.INSTANCE.sendToServer(new RequestConfigPayload());
                            Minecraft.getInstance()
                                    .setScreen(
                                            new PerItemExpConfigScreen(
                                                    Component.translatable(
                                                            "screen.uncrafteverything.per_item_xp_config")));
                        });
        return 1;
    }

    private static int client(CommandContext<?> context) {
        Minecraft.getInstance()
                .execute(
                        () -> {
                            Minecraft.getInstance()
                                    .setScreen(
                                            new UEClientConfigScreen(
                                                    Component.translatable(
                                                            "screen.uncrafteverything.uncraft_everything_client_config")));
                        });
        return 1;
    }

    private static int progression(CommandContext<?> context) {
        Minecraft.getInstance()
                .execute(
                        () -> {
                            RequestConfigPayload.INSTANCE.sendToServer(new RequestConfigPayload());
                            Minecraft.getInstance()
                                    .setScreen(
                                            new FTBQuestsProgressionConfigScreen(
                                                    Component.translatable(
                                                            "screen.uncrafteverything.ftb_quest_progression_config")));
                        });
        return 1;
    }
}
