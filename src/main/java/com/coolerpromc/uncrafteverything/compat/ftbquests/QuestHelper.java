package com.coolerpromc.uncrafteverything.compat.ftbquests;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;

public class QuestHelper {
    public static final boolean FTBQUESTS_LOADED = FabricLoader.getInstance().isModLoaded("ftbquests");

    public static boolean hasCompletedQuestOrChapter(ServerPlayerEntity player, String id){
        if (!FTBQUESTS_LOADED) {
            return false;
        }
        return QuestHelperImpl.hasCompletedQuestOrChapter(player, id);
    }
}