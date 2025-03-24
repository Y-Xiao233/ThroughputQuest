package net.yxiao233.throughputquest.common.registries;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.core.definitions.ItemDefinition;
import appeng.util.helpers.ItemComparisonHelper;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class TQItemDefinition<T extends Item> implements ItemLike, Supplier<T> {
    private final String englishName;
    private final RegistryObject<T> item;

    public TQItemDefinition(String englishName, RegistryObject<T> item) {
        this.englishName = englishName;
        this.item = item;
    }

    public String getEnglishName() {
        return this.englishName;
    }

    public ResourceLocation id() {
        return this.item.getId();
    }

    public ItemStack stack() {
        return this.stack(1);
    }

    public ItemStack stack(int stackSize) {
        return new ItemStack((ItemLike)this.item.get(), stackSize);
    }

    public GenericStack genericStack(long stackSize) {
        return new GenericStack(AEItemKey.of((ItemLike)this.item.get()), stackSize);
    }

    public Holder<T> holder() {
        return (Holder)this.item.getHolder().orElseThrow();
    }

    /** @deprecated */
    @Deprecated(
            forRemoval = true,
            since = "1.21"
    )
    public final boolean isSameAs(ItemStack comparableStack) {
        return this.is(comparableStack);
    }

    public final boolean is(ItemStack comparableStack) {
        return ItemComparisonHelper.isEqualItemType(comparableStack, this.stack());
    }

    public final boolean is(AEKey key) {
        if (key instanceof AEItemKey itemKey) {
            return this.asItem() == itemKey.getItem();
        } else {
            return false;
        }
    }

    public final boolean isSameAs(AEKey key) {
        return this.is(key);
    }

    public T get() {
        return (T) this.item.get();
    }

    public T asItem() {
        return (T) this.item.get();
    }

    public ItemDefinition<T> getItemDefinition() {
        return new ItemDefinition(this.englishName, this.id(), this.asItem());
    }
}