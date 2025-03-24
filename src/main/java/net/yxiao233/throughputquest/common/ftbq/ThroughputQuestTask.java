package net.yxiao233.throughputquest.common.ftbq;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.IconAnimation;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.integration.item_filtering.ItemMatchingSystem;
import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import dev.ftb.mods.ftbquests.net.FTBQuestsNetHandler;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.util.NBTUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.yxiao233.throughputquest.common.part.ThroughputDetectionMonitorPart;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ThroughputQuestTask extends Task {
    private long throughput;
    private ItemStack itemStack;
    private ThroughputDetectionMonitorPart.WorkRoutine workRoutine;
    public ThroughputQuestTask(long id, Quest quest) {
        super(id, quest);
        this.itemStack = ItemStack.EMPTY;
        this.throughput = 1;
        this.workRoutine = ThroughputDetectionMonitorPart.WorkRoutine.SECOND;
    }

    public List<ItemStack> getValidDisplayItems() {
        int count = (int) Math.min(throughput,Integer.MAX_VALUE);
        return ItemMatchingSystem.INSTANCE.getAllMatchingStacks(new ItemStack(itemStack.getItem(),count));
    }

    @Override
    public void addMouseOverText(TooltipList list, TeamData teamData) {
        super.addMouseOverText(list, teamData);
        list.add(Component.translatable("ftbquests.task.throughput_quest.over_text",new Object[]{throughput,workRoutine.getId()}).withStyle(ChatFormatting.GREEN));
    }

    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return this.throughput > 1L ? Component.literal(this.throughput + "x ").append(this.itemStack.getHoverName()).append(" /" + this.workRoutine.getId()) : Component.literal("").append(this.itemStack.getHoverName()).append(" /" + this.workRoutine.getId());
    }

    @OnlyIn(Dist.CLIENT)
    public Icon getAltIcon() {
        List<Icon> icons = new ArrayList<>();
        Iterator<ItemStack> var2 = this.getValidDisplayItems().iterator();

        while(var2.hasNext()) {
            ItemStack stack = var2.next();
            ItemStack copy = stack.copy();
            copy.setCount(1);
            Icon icon = ItemIcon.getItemIcon(copy);
            if (!icon.isEmpty()) {
                icons.add(icon);
            }
        }

        if (icons.isEmpty()) {
            return ItemIcon.getItemIcon(FTBQuestsItems.MISSING_ITEM.get());
        } else {
            return IconAnimation.fromList(icons, false);
        }
    }


    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addItemStack("item", this.itemStack, (v) -> {
            this.itemStack = v;
        }, ItemStack.EMPTY, true, false).setNameKey("ftbquests.task.ftbquests.item");
        config.addLong("throughput", this.throughput, (v) ->{
            this.throughput = v;
        }, 1, 1, Integer.MAX_VALUE);
        var workRoutines = new ThroughputDetectionMonitorPart.WorkRoutine[]{
                ThroughputDetectionMonitorPart.WorkRoutine.SECOND,
                ThroughputDetectionMonitorPart.WorkRoutine.MINUTE,
                ThroughputDetectionMonitorPart.WorkRoutine.TICK
        };

        config.addEnum("work_routine",this.workRoutine, (v) ->{
            this.workRoutine = v;
        }, NameMap.of(ThroughputDetectionMonitorPart.WorkRoutine.SECOND,workRoutines).name((v) ->{
            return Component.literal(v.getId());
        }).create());
    }

    @Override
    public TaskType getType() {
        return TQTaskTypes.THROUGHPUT;
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        throughput = nbt.getInt("throughput");
        itemStack = NBTUtils.read(nbt,"item");
        workRoutine = ThroughputDetectionMonitorPart.WorkRoutine.getById(nbt.getString("workroutine"));
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putLong("throughput", throughput);
        NBTUtils.write(nbt,"item",this.itemStack);
        nbt.putString("workroutine",this.workRoutine.getId());
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        this.throughput = buffer.readLong();
        this.itemStack = FTBQuestsNetHandler.readItemType(buffer);
        this.workRoutine = buffer.readEnum(ThroughputDetectionMonitorPart.WorkRoutine.class);
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeLong(this.throughput);
        FTBQuestsNetHandler.writeItemType(buffer, this.itemStack);
        buffer.writeEnum(this.workRoutine);
    }

    @Override
    public int autoSubmitOnPlayerTick() {
        return 5;
    }


    @Override
    public void submitTask(TeamData teamData, ServerPlayer player, ItemStack craftedItem) {
        for (int i = 0; i < ThroughputDetectionMonitorPart.list.size(); i++) {
            ThroughputDetectionMonitorPart part = ThroughputDetectionMonitorPart.list.get(i);
            if (part != null) {
                if(this.itemStack.is(part.getItemStack().getItem()) && this.workRoutine.equals(part.getWorkRoutine())){
                    if(part.getThroughput() >= this.throughput){
                        teamData.addProgress(this, 1);
                    }
                }
            }
        }
    }
}
