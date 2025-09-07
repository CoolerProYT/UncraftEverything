package com.coolerpromc.uncrafteverything.compat.ftbquests;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.server.level.ServerPlayer;

public class QuestHelper {
    public static boolean hasCompletedQuest(ServerPlayer player, String questId){
        TeamData teamData = TeamData.get(player);
        return teamData.isCompleted(teamData.getFile().getQuest(QuestObjectBase.parseCodeString(questId)));
    }
}