package com.coolerpromc.uncrafteverything.compat.ftbquests;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

public class QuestHelper {
    public static final boolean FTBQUESTS_LOADED = ModList.get().isLoaded("ftbquests");

    public static boolean hasCompletedQuest(ServerPlayer player, String questId){
        try{
            TeamData teamData = ServerQuestFile.INSTANCE.getTeamData(player).orElse(null);
            if (teamData == null) return false;

            Quest quest = ServerQuestFile.INSTANCE.getQuest(QuestObjectBase.parseCodeString(questId));
            if (quest == null) return false;

            return teamData.isCompleted(quest);
        }
        catch (Exception e){
            return false;
        }
    }
}