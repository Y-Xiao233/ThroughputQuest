package net.yxiao233.throughputquest;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.yxiao233.throughputquest.common.ftbq.TQTaskTypes;
import net.yxiao233.throughputquest.common.registries.TQCreativeModeTab;
import net.yxiao233.throughputquest.common.registries.TQItems;

@Mod(ThroughputQuest.MODID)
public class ThroughputQuest {
    public static final String MODID = "throughput_quest";
    public ThroughputQuest() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        TQItems.DR.register(modEventBus);
        TQCreativeModeTab.CREATIVE_MODE_TAB.register(modEventBus);
        TQTaskTypes.init();
    }
}
