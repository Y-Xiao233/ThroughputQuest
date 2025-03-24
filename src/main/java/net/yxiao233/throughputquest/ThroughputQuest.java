package net.yxiao233.throughputquest;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.yxiao233.throughputquest.common.ftbq.TQTaskTypes;
import net.yxiao233.throughputquest.common.registries.TQCreativeModeTab;
import net.yxiao233.throughputquest.common.registries.TQItems;

@Mod(ThroughputQuest.MODID)
public class ThroughputQuest {
    public static final String MODID = "throughput_quest";
    public ThroughputQuest(IEventBus modEventBus, ModContainer modContainer) {
        TQItems.DR.register(modEventBus);
        TQCreativeModeTab.CREATIVE_MODE_TAB.register(modEventBus);
        TQTaskTypes.init();
    }
}
