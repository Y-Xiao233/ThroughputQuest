package net.yxiao233.throughputquest.common.registries;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartModels;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.yxiao233.throughputquest.common.part.ThroughputDetectionMonitorPart;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TQItems {
    public static final DeferredRegister<Item> DR;
    private static final List<TQItemDefinition<?>> ITEMS;
    public static final TQItemDefinition<PartItem<ThroughputDetectionMonitorPart>> THROUGHPUT_MONITOR;
    private static <T extends Item> TQItemDefinition<T> item(String englishName, String id, Function<Item.Properties, T> factory) {
        TQItemDefinition<T> definition = new TQItemDefinition(englishName, DR.register(id, () -> {
            return (Item)factory.apply(new Item.Properties());
        }));
        ITEMS.add(definition);
        return definition;
    }
    private static <T extends IPart> TQItemDefinition<PartItem<T>> part(String englishName, String id, Class<T> partClass, Function<IPartItem<T>, T> factory) {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        return item(englishName, id, (p) -> {
            return new PartItem<>(p, partClass, factory);
        });
    }
   static {
       DR = DeferredRegister.create(ForgeRegistries.ITEMS, "throughput_quest");
       ITEMS = new ArrayList();
       THROUGHPUT_MONITOR = part("ME Throughput Detection Monitor", "throughput_detection_monitor", ThroughputDetectionMonitorPart.class, ThroughputDetectionMonitorPart::new);
   }
}
