package net.yxiao233.throughputquest.common.registries;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartModels;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yxiao233.throughputquest.ThroughputQuest;
import net.yxiao233.throughputquest.common.part.ThroughputDetectionMonitorPart;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TQItems {
    public static final DeferredRegister.Items DR = DeferredRegister.createItems(ThroughputQuest.MODID);;
    private static final List<ItemDefinition<?>> ITEMS = new ArrayList<>();
    public static final ItemDefinition<PartItem<ThroughputDetectionMonitorPart>> THROUGHPUT_MONITOR = part("ME Throughput Detection Monitor", "throughput_detection_monitor", ThroughputDetectionMonitorPart.class, ThroughputDetectionMonitorPart::new);
    private static <T extends IPart> ItemDefinition<PartItem<T>> part(String englishName, String id, Class<T> partClass, Function<IPartItem<T>, T> factory) {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        return item(englishName, id, (p) -> {
            return new PartItem<>(p, partClass, factory);
        });
    }

    private static <T extends Item> ItemDefinition<T> item(String englishName, String id, Function<Item.Properties, T> factory) {
        ItemDefinition<T> definition = new ItemDefinition<>(englishName, DR.registerItem(id, factory));
        ITEMS.add(definition);
        return definition;
    }
}
