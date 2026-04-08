package com.coolerpromc.uncrafteverything.compat.ftbquests;

import com.coolerpromc.uncrafteverything.platform.Services;
import net.minecraft.server.level.ServerPlayer;

public class QuestHelper {
    public static final boolean FTBQUESTS_LOADED = Services.PLATFORM.isModLoaded("ftbquests");

    public static boolean hasCompletedQuestOrChapter(ServerPlayer player, String id){
        if (!FTBQUESTS_LOADED) {
            return false;
        }
        return QuestHelperImpl.hasCompletedQuestOrChapter(player, id);
    }
}