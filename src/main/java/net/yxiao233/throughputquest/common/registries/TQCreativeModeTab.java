package net.yxiao233.throughputquest.common.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.yxiao233.throughputquest.ThroughputQuest;

public class TQCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ThroughputQuest.MODID);
    public static final RegistryObject<CreativeModeTab> TQ_TAB = CREATIVE_MODE_TAB.register("tq_tab", () -> CreativeModeTab.builder()
            .icon(() -> TQItems.THROUGHPUT_MONITOR.asItem().getDefaultInstance())
            .displayItems((parameters, output) -> {

                TQItems.DR.getEntries().forEach((reg) ->{
                    output.accept(reg.get());
                });
            })
            .title(Component.translatable("itemGroup.throughput_quest"))
            .build()
    );
}
