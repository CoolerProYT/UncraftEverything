package com.coolerpromc.uncrafteverything.compat.ftbquests;

import dev.ftb.mods.ftbquests.quest.*;
import net.minecraft.server.level.ServerPlayer;

public class QuestHelperImpl {
    public static boolean hasCompletedQuestOrChapter(ServerPlayer player, String id) {
        try {
            TeamData teamData = ServerQuestFile.getInstance().getTeamData(player).orElse(null);
            if (teamData == null) return false;

            Quest quest = ServerQuestFile.getInstance().getQuest(QuestObjectBase.parseCodeString(id));
            Chapter chapter = ServerQuestFile.getInstance().getChapter(DefaultChapterGroup.parseCodeString(id));
            if (quest == null && chapter == null) return false;

            return teamData.isCompleted(quest != null ? quest : chapter);
        } catch (Exception e) {
            return false;
        }
    }
}