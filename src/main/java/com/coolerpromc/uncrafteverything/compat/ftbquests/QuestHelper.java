package com.coolerpromc.uncrafteverything.compat.ftbquests;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.ModList;

public class QuestHelper {
    public static final boolean FTBQUESTS_LOADED = ModList.get().isLoaded("ftbquests");

    public static boolean hasCompletedQuestOrChapter(ServerPlayerEntity player, String id){
        if (!FTBQUESTS_LOADED) {
            return false;
        }
        return QuestHelperImpl.hasCompletedQuestOrChapter(player, id);
    }
}