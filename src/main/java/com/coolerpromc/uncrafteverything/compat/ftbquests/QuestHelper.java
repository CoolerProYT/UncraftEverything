package com.coolerpromc.uncrafteverything.compat.ftbquests;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;

public class QuestHelper {
    public static final boolean FTBQUESTS_LOADED = ModList.get().isLoaded("ftbquests");

    public static boolean hasCompletedQuestOrChapter(ServerPlayer player, String id){
        if (!FTBQUESTS_LOADED) {
            return false;
        }
        return QuestHelperImpl.hasCompletedQuestOrChapter(player, id);
    }
}