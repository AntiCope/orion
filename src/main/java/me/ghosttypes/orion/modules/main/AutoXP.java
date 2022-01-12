package me.ghosttypes.orion.modules.main;

import me.ghosttypes.orion.Orion;
import me.ghosttypes.orion.utils.Wrapper;
import me.ghosttypes.orion.utils.player.ArmorUtil;
import me.ghosttypes.orion.utils.player.ItemHelper;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.CrystalAura;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.combat.Offhand;
import meteordevelopment.meteorclient.utils.player.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoXP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPause = settings.createGroup("Pause");

    private final Setting<Double> enableAt = sgGeneral.add(new DoubleSetting.Builder().name("threshold").description("What durability to enable at.").defaultValue(20).min(1).sliderMin(1).sliderMax(100).max(100).build());
    private final Setting<Double> minHealth = sgGeneral.add(new DoubleSetting.Builder().name("min-health").description("Min health for repairing.").defaultValue(10).min(0).sliderMax(36).max(36).build());
    private final Setting<Boolean> moduleControl = sgGeneral.add(new BoolSetting.Builder().name("module-control").description("Disable combat modules while repairing armor.").defaultValue(true).build());
    private final Setting<Boolean> onlyInHole = sgGeneral.add(new BoolSetting.Builder().name("only-in-hole").description("Only throw XP while in a hole.").defaultValue(false).build());
    private final Setting<Boolean> silent = sgGeneral.add(new BoolSetting.Builder().name("silent").description("Allows you to use other hotbar slots while throwing XP.").defaultValue(false).build());
    private final Setting<Boolean> refill = sgGeneral.add(new BoolSetting.Builder().name("refill").description("Moves XP from your inventory to your hotbar when you run out.").defaultValue(false).build());
    private final Setting<Boolean> refillOffhand = sgGeneral.add(new BoolSetting.Builder().name("use-offhand").description("Uses your offhand for XP.").defaultValue(false).build());
    private final Setting<Integer> refillSlot = sgGeneral.add(new IntSetting.Builder().name("refill-slot").description("Which slot to refill.").defaultValue(1).min(1).sliderMin(1).max(9).sliderMax(9).visible(refill::get).build());
    private final Setting<Boolean> lookDown = sgGeneral.add(new BoolSetting.Builder().name("look-down").description("Throws the XP at your feet.").defaultValue(true).build());

    private final Setting<Boolean> pauseOnEat = sgPause.add(new BoolSetting.Builder().name("pause-on-eat").description("Pauses while eating.").defaultValue(true).build());
    private final Setting<Boolean> pauseOnDrink = sgPause.add(new BoolSetting.Builder().name("pause-on-drink").description("Pauses while drinking.").defaultValue(true).build());
    private final Setting<Boolean> pauseOnMine = sgPause.add(new BoolSetting.Builder().name("pause-on-mine").description("Pauses while mining.").defaultValue(true).build());

    private boolean alerted, toggledOffhand;
    private int slotRefill;

    public AutoXP() {
        super(Orion.CATEGORY, "auto-xp", "Automatically repair your armor.");
    }

    @Override
    public void onActivate() {
        if (moduleControl.get()) {
            CrystalAura ca = Modules.get().get(CrystalAura.class);
            KillAura ka = Modules.get().get(KillAura.class);
            BedAura ba = Modules.get().get(BedAura.class);
            Offhand offhand = Modules.get().get(Offhand.class);
            if (ca.isActive()) ca.toggle();
            if (ka.isActive()) ka.toggle();
            if (ba.isActive()) ba.toggle();
            if (offhand.isActive() && refillOffhand.get()) {
                toggledOffhand = true;
                offhand.toggle();
            }
        }
        alerted = false;
        slotRefill = refillSlot.get() - 1;
    }

    @Override
    public void onDeactivate() {
        Offhand offhand = Modules.get().get(Offhand.class);
        if (moduleControl.get() && toggledOffhand && !offhand.isActive()) offhand.toggle();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        assert mc.player != null;
        if (Wrapper.getTotalHealth(mc.player) <= minHealth.get()) {
            error("Your health is too low!");
            toggle();
            return;
        }
        if (onlyInHole.get() && !Wrapper.isInHole(mc.player)) {
            error("You're not in a hole!");
            toggle();
            return;
        }
        if (PlayerUtils.shouldPause(pauseOnMine.get(), pauseOnEat.get(), pauseOnDrink.get())) return;
        if (refillSlotEmpty(false) && !refillOffhand.get()) {
            if (refill.get()) {
                FindItemResult invXP = ItemHelper.findXPinAll();
                if (invXP.found()) {
                    InvUtils.move().from(invXP.slot()).toHotbar(slotRefill);
                } else {
                    error("You're out of XP!");
                    toggle();
                    return;
                }
            } else {
                error("No XP in hotbar!");
                toggle();
                return;
            }
        }
        if (refillSlotEmpty(true) && refillOffhand.get()) {
            FindItemResult invXP = ItemHelper.findXPinAll();
            if (invXP.found()) {
                InvUtils.move().from(invXP.slot()).toOffhand();
            } else {
                error("You're out of XP!");
                toggle();
                return;
            }
        }

        boolean needsRepair = shouldRepair();
        if (!needsRepair) {
            if (alerted) info("Finished repair.");
            toggle();
            return;
        }
        if (!alerted) {
            info("Repairing armor to " + enableAt.get() + "%%");
            alerted = true;
        }
        if (lookDown.get()) {
            Rotations.rotate(mc.player.getYaw(), 90, 50, this::throwXP);
        } else {
            throwXP();
        }
    }

    private void throwXP() {
        int lastSlot = mc.player.getInventory().selectedSlot;
        if (mc.player.getInventory().getStack(lastSlot).getItem() == Items.ENCHANTED_GOLDEN_APPLE && pauseOnEat.get()) return;
        if (refillOffhand.get()) {
            mc.interactionManager.interactItem(mc.player, mc.world, Hand.OFF_HAND);
        } else {
            Wrapper.updateSlot(slotRefill);
            mc.interactionManager.interactItem(mc.player, mc.world, Hand.MAIN_HAND);
            if (silent.get() && lastSlot != -1) Wrapper.updateSlot(lastSlot);
        }
    }

    private boolean shouldRepair() {
        for (int i = 0; i < 4; i++) if (ArmorUtil.checkThreshold(ArmorUtil.getArmor(i), enableAt.get())) return true;
        return false;
    }

    private boolean refillSlotEmpty(boolean offhand) {
        if (offhand) return Wrapper.getItemFromSlot(SlotUtils.OFFHAND) != Items.EXPERIENCE_BOTTLE;
        return Wrapper.getItemFromSlot(slotRefill) != Items.EXPERIENCE_BOTTLE;
    }
}
