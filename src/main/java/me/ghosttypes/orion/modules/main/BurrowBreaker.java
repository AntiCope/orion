package me.ghosttypes.orion.modules.main;

import me.ghosttypes.orion.Orion;
import me.ghosttypes.orion.utils.Wrapper;
import me.ghosttypes.orion.utils.player.AutomationUtils;
import me.ghosttypes.orion.utils.player.ItemHelper;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;


public class BurrowBreaker extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder().name("range").description("Max targeting range.").defaultValue(4).min(0).sliderMax(5).build());
    private final Setting<Boolean> usePacketMine = sgGeneral.add(new BoolSetting.Builder().name("packet-mine").description("Hide mining progress on the target block.").defaultValue(true).build());
    private final Setting<Boolean> preventAfter = sgGeneral.add(new BoolSetting.Builder().name("prevent-after").description("Prevent the target from placing another burrow block in their current hole.").defaultValue(false).build());

    private PlayerEntity target;
    private boolean sentPacketMine;
    private boolean alertedTarget;
    private boolean wasBurrowed;

    public BurrowBreaker() {
        super(Orion.CATEGORY, "burrow-breaker", "Automatically destroy target's burrow block.");
    }

    @Override
    public void onActivate() {
        target = null;
        sentPacketMine = false;
        alertedTarget = false;
        wasBurrowed = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        FindItemResult pickSlot = ItemHelper.findPick();
        if (!pickSlot.found()) {
            error("No pickaxe in hotbar!");
            toggle();
            return;
        }
        if (target == null && TargetUtils.getPlayerTarget(range.get(), SortPriority.LowestDistance) == null) {
            error("No burrowed targets in range!");
            toggle();
            return;
        }
        target = TargetUtils.getPlayerTarget(range.get(), SortPriority.LowestDistance);
        if (target == null) return;
        if (AutomationUtils.isBurrowed(target, true)) {
            wasBurrowed = true;
            BlockPos burrowBlock = target.getBlockPos();
            if (!alertedTarget) {
                info("Breaking " + target.getEntityName() + "'s burrow!");
                alertedTarget = true;
            }
            Wrapper.updateSlot(pickSlot.slot());
            if (usePacketMine.get() && !sentPacketMine) {
                AutomationUtils.doPacketMine(burrowBlock);
                sentPacketMine = true;
            } else { AutomationUtils.doRegularMine(burrowBlock); }
            return;
        }
        if (!AutomationUtils.isBurrowed(target, true) && wasBurrowed) {
            info("Broke " + target.getEntityName() + "'s burrow!");
            if (preventAfter.get()) {
                FindItemResult floorBlock = InvUtils.findInHotbar(itemStack -> Block.getBlockFromItem(itemStack.getItem()) instanceof AbstractPressurePlateBlock || Block.getBlockFromItem(itemStack.getItem()) instanceof AbstractButtonBlock);
                if (!floorBlock.found()) {
                    warning("No buttons or plates in hotbar, cannot prevent re-burrow.");
                    toggle();
                }
                BlockUtils.place(target.getBlockPos(), floorBlock, true, 0, false);
                info("Blocked " + target.getEntityName() + " from re-burrowing!");
            }
            toggle();
        }
    }
}
