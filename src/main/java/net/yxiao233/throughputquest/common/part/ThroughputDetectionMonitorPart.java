package net.yxiao233.throughputquest.common.part;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.orientation.BlockOrientation;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AmountFormat;
import appeng.api.util.AEColor;
import appeng.client.render.BlockEntityRenderHelper;
import appeng.core.AppEng;
import appeng.hooks.ticking.TickHandler;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractMonitorPart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.pedroksl.advanced_ae.client.renderer.AAEBlockEntityRenderHelper;
import net.pedroksl.advanced_ae.common.definitions.AAEItems;
import net.pedroksl.advanced_ae.common.definitions.AAEText;

import java.util.ArrayList;
import java.util.List;

public class ThroughputDetectionMonitorPart extends AbstractMonitorPart implements IGridTickable {
    @PartModels
    public static final ResourceLocation MODEL_OFF = AppEng.makeId("part/storage_monitor_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = AppEng.makeId("part/storage_monitor_on");
    @PartModels
    public static final ResourceLocation MODEL_LOCKED_OFF = AppEng.makeId("part/storage_monitor_locked_off");
    @PartModels
    public static final ResourceLocation MODEL_LOCKED_ON = AppEng.makeId("part/storage_monitor_locked_on");
    public static final IPartModel MODELS_OFF;
    public static final IPartModel MODELS_ON;
    public static final IPartModel MODELS_HAS_CHANNEL;
    public static final IPartModel MODELS_LOCKED_OFF;
    public static final IPartModel MODELS_LOCKED_ON;
    public static final IPartModel MODELS_LOCKED_HAS_CHANNEL;
    private long lastUpdateTick = -1L;
    protected long amountAtLastUpdate = -1L;
    protected long lastReportedValue = -1L;
    protected String lastHumanReadableValue = "";
    private WorkRoutine workRoutine;
    private WorkRoutine lastWorkRoutine;
    private static final int positiveColor;
    private static final int negativeColor;
    public static final List<ThroughputDetectionMonitorPart> list = new ArrayList<>();

    public ThroughputDetectionMonitorPart(IPartItem<?> partItem) {
        super(partItem, false);
        this.workRoutine = WorkRoutine.SECOND;
        this.lastWorkRoutine = WorkRoutine.SECOND;
        this.getMainNode().addService(IGridTickable.class, this);
        list.add(this);
    }

    public void writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putLong("lastValue", this.lastReportedValue);
        data.putString("throughput", this.lastHumanReadableValue);
        data.putInt("routine", this.workRoutine.ordinal());
    }

    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.lastReportedValue = data.getLong("lastValue");
        this.lastHumanReadableValue = data.getString("throughput");
        this.workRoutine = WorkRoutine.fromInt(data.getInt("routine"));
    }

    public void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeLong(this.lastUpdateTick);
        data.writeLong(this.amountAtLastUpdate);
        data.writeLong(this.lastReportedValue);
        data.writeUtf(this.lastHumanReadableValue);
        data.writeEnum(this.workRoutine);
    }

    public boolean readFromStream(FriendlyByteBuf data) {
        boolean needRedraw = super.readFromStream(data);
        this.lastUpdateTick = data.readLong();
        this.amountAtLastUpdate = data.readLong();
        long reportedValue = data.readLong();
        needRedraw |= this.lastReportedValue > 0L && reportedValue < 0L || this.lastReportedValue < 0L && reportedValue > 0L;
        this.lastReportedValue = reportedValue;
        this.lastHumanReadableValue = data.readUtf();
        this.workRoutine = data.readEnum(WorkRoutine.class);
        return needRedraw;
    }

    public long getThroughput(){
        return this.lastReportedValue;
    }
    public ItemStack getItemStack(){
        if(this.getDisplayed() != null){
            if(this.getDisplayed() instanceof AEItemKey itemKey){
                return itemKey.toStack();
            }else{
                return ItemStack.EMPTY;
            }
        }
        return ItemStack.EMPTY;
    }

    public WorkRoutine getWorkRoutine(){
        return this.lastWorkRoutine;
    }
    public void writeVisualStateToNBT(CompoundTag data) {
        super.writeVisualStateToNBT(data);
        data.putLong("lastValue", this.lastReportedValue);
        data.putString("throughput", this.lastHumanReadableValue);
        data.putInt("routine", this.workRoutine.ordinal());
    }

    public void readVisualStateFromNBT(CompoundTag data) {
        super.readVisualStateFromNBT(data);
        this.lastReportedValue = data.getLong("lastValue");
        this.lastHumanReadableValue = data.getString("throughput");
        this.workRoutine =WorkRoutine.fromInt(data.getInt("routine"));
    }

    public boolean onPartActivate(Player player, InteractionHand hand, Vec3 pos) {
        if (!this.isClientSide()) {
            ItemStack heldItem = player.getItemInHand(hand);
            if (heldItem.is(AAEItems.MONITOR_CONFIGURATOR.asItem())) {
                this.cycleWorkRoutine();
                return true;
            }
        }

        return super.onPartActivate(player, hand, pos);
    }

    private void cycleWorkRoutine() {
        this.workRoutine = WorkRoutine.cycle(this.workRoutine);
        this.getMainNode().ifPresent((grid, node) -> {
            grid.getTickManager().alertDevice(node);
        });
    }

    protected void configureWatchers() {
        if (this.getDisplayed() != null) {
            this.updateState(TickHandler.instance().getCurrentTick());
            this.getMainNode().ifPresent((grid, node) -> {
                grid.getTickManager().wakeDevice(node);
            });
        } else {
            this.getMainNode().ifPresent((grid, node) -> {
                grid.getTickManager().sleepDevice(node);
            });
        }

        super.configureWatchers();
    }

    @OnlyIn(Dist.CLIENT)
    public void renderDynamic(float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int combinedLightIn, int combinedOverlayIn) {
        if (this.isActive() && this.getDisplayed() != null) {
            poseStack.pushPose();
            BlockOrientation orientation = BlockOrientation.get(this.getSide(), this.getSpin());
            poseStack.translate(0.5, 0.5, 0.5);
            BlockEntityRenderHelper.rotateToFace(poseStack, orientation);
            poseStack.translate(0.0, 0.1, 0.5);
            BlockEntityRenderHelper.renderItem2dWithAmount(poseStack, buffers, this.getDisplayed(), this.getAmount(), this.canCraft(), 0.3F, -0.15F, this.getColor().contrastTextColor, this.getLevel());
            poseStack.translate(0.0F, -0.23F, 0.0F);
            String sign = this.lastReportedValue > 0L ? "+" : (this.lastReportedValue == 0L ? "" : "-");
            int color = this.lastReportedValue > 0L ? positiveColor : (this.lastReportedValue == 0L ? this.getColor().contrastTextColor : negativeColor);
            MutableComponent var10000;
            switch (this.workRoutine) {
                case TICK:
                    var10000 = AAEText.OverdriveThroughputMonitorValue.text(new Object[]{sign, this.lastHumanReadableValue});
                    break;
                case SECOND:
                    var10000 = AAEText.ThroughputMonitorValue.text(new Object[]{sign, this.lastHumanReadableValue});
                    break;
                case MINUTE:
                    var10000 = AAEText.SlowThroughputMonitorValue.text(new Object[]{sign, this.lastHumanReadableValue});
                    break;
                default:
                    throw new IncompatibleClassChangeError();
            }

            MutableComponent text = var10000;
            AAEBlockEntityRenderHelper.renderString(poseStack, buffers, text, color);
            poseStack.popPose();
        }

    }

    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL, MODELS_LOCKED_OFF, MODELS_LOCKED_ON, MODELS_LOCKED_HAS_CHANNEL);
    }

    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        this.getMainNode().ifPresent((grid, node) -> {
            grid.getTickManager().wakeDevice(node);
        });
        super.onMainNodeStateChanged(reason);
    }

    public TickingRequest getTickingRequest(IGridNode iGridNode) {
        return new TickingRequest(20, 400, !this.isActive() || this.getDisplayed() == null, true);
    }

    public TickRateModulation tickingRequest(IGridNode iGridNode, int i) {
        if (this.getMainNode().isActive() && this.getDisplayed() != null) {
            long currentTick = TickHandler.instance().getCurrentTick();
            long tickAmount = currentTick - this.lastUpdateTick;
            float timeInSeconds = (float)tickAmount / 20.0F;
            if (this.lastUpdateTick != -1L && !(timeInSeconds <= 0.0F)) {
                if (this.workRoutine == this.lastWorkRoutine) {
                    float amountPerSecond = (float)(this.getAmount() - this.amountAtLastUpdate) / timeInSeconds;
                    float var10001;
                    switch (this.workRoutine) {
                        case TICK:
                            var10001 = amountPerSecond / 20.0F;
                            break;
                        case SECOND:
                            var10001 = amountPerSecond;
                            break;
                        case MINUTE:
                            var10001 = amountPerSecond * 60.0F;
                            break;
                        default:
                            throw new IncompatibleClassChangeError();
                    }

                    this.lastReportedValue = (long)Math.round(var10001);
                    this.lastHumanReadableValue = this.getDisplayed().formatAmount(Math.abs(this.lastReportedValue), AmountFormat.SLOT);
                } else {
                    this.lastHumanReadableValue = "-";
                }

                this.updateState(currentTick);
                this.getHost().markForUpdate();
                return TickRateModulation.SLOWER;
            } else {
                this.updateState(currentTick);
                this.lastHumanReadableValue = "-";
                return TickRateModulation.URGENT;
            }
        } else {
            this.resetState();
            return TickRateModulation.SLEEP;
        }
    }

    private void resetState() {
        this.lastUpdateTick = -1L;
        this.amountAtLastUpdate = -1L;
        this.lastHumanReadableValue = "";
    }

    private void updateState(long tick) {
        this.lastUpdateTick = tick;
        this.amountAtLastUpdate = this.getAmount();
        this.lastWorkRoutine = this.workRoutine;
    }

    static {
        MODELS_OFF = new PartModel(new ResourceLocation[]{MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF});
        MODELS_ON = new PartModel(new ResourceLocation[]{MODEL_BASE, MODEL_ON, MODEL_STATUS_ON});
        MODELS_HAS_CHANNEL = new PartModel(new ResourceLocation[]{MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL});
        MODELS_LOCKED_OFF = new PartModel(new ResourceLocation[]{MODEL_BASE, MODEL_LOCKED_OFF, MODEL_STATUS_OFF});
        MODELS_LOCKED_ON = new PartModel(new ResourceLocation[]{MODEL_BASE, MODEL_LOCKED_ON, MODEL_STATUS_ON});
        MODELS_LOCKED_HAS_CHANNEL = new PartModel(new ResourceLocation[]{MODEL_BASE, MODEL_LOCKED_ON, MODEL_STATUS_HAS_CHANNEL});
        positiveColor = AEColor.GREEN.mediumVariant;
        negativeColor = AEColor.RED.mediumVariant;
    }

    public static enum WorkRoutine {
        TICK("tick"),
        SECOND("second"),
        MINUTE("minute");

        private String id;
        private WorkRoutine(String id) {
            this.id = id;
        }

        public String getId(){
            return id;
        }

        public static WorkRoutine getById(String id){
            return switch (id){
                case "tick" -> TICK;
                case "minute" -> MINUTE;
                default -> SECOND;
            };
        }

        public static WorkRoutine cycle(WorkRoutine routine) {
            WorkRoutine var10000;
            switch (routine) {
                case TICK:
                    var10000 = SECOND;
                    break;
                case SECOND:
                    var10000 = MINUTE;
                    break;
                case MINUTE:
                    var10000 = TICK;
                    break;
                default:
                    throw new IncompatibleClassChangeError();
            }

            return var10000;
        }

        public static WorkRoutine fromInt(int value) {
            return switch (value) {
                case 0 -> TICK;
                case 2 -> MINUTE;
                default -> SECOND;
            };
        }
    }
}
