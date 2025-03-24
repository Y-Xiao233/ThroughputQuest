package net.yxiao233.throughputquest.common.ftbq;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import net.minecraft.resources.ResourceLocation;
import net.yxiao233.throughputquest.ThroughputQuest;

public interface TQTaskTypes {
    TaskType THROUGHPUT = TaskTypes.register(new ResourceLocation(ThroughputQuest.MODID,"throughput"),ThroughputQuestTask::new,() ->{
        return Icon.getIcon("throughput_quest:textures/icon/throughput_detection_monitor.png");
    });
    static void init() {
    }
}
