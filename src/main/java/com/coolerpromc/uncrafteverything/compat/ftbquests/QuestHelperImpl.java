package com.coolerpromc.uncrafteverything.compat.ftbquests;

import dev.ftb.mods.ftbquests.quest.*;
import net.minecraft.server.network.ServerPlayerEntity;

public class QuestHelperImpl {
    public static boolean hasCompletedQuestOrChapter(ServerPlayerEntity player, String id) {
        try {
            TeamData teamData = ServerQuestFile.INSTANCE.getTeamData(player).orElse(null);
            if (teamData == null) return false;

            Quest quest = ServerQuestFile.INSTANCE.getQuest(QuestObjectBase.parseCodeString(id));
            Chapter chapter = ServerQuestFile.INSTANCE.getChapter(DefaultChapterGroup.parseCodeString(id));
            if (quest == null && chapter == null) return false;

            return teamData.isCompleted(quest != null ? quest : chapter);
        } catch (Exception e) {
            return false;
        }
    }
}