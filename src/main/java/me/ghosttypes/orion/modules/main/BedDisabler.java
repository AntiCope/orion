package me.ghosttypes.orion.modules.main;

import me.ghosttypes.orion.Orion;
import me.ghosttypes.orion.utils.Wrapper;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;

public class BedDisabler extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPause = settings.createGroup("Pause");

    private final Setting<Integer> webPerTick = sgGeneral.add(new IntSetting.Builder().name("webs-per-tick").description("Web placements per tick.").defaultValue(2).min(1).sliderMax(3).build());
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder().name("delay").description("Delay between attempts.").defaultValue(4).min(1).sliderMax(10).build());
    private final Setting<Boolean> ignoreFeet = sgGeneral.add(new BoolSetting.Builder().name("ignore-feet").description("Don't place on your feet.").defaultValue(false).build());
    private final Setting<Boolean> onlyInHole = sgGeneral.add(new BoolSetting.Builder().name("only-in-hole").description("Only place while you're in a hole.").defaultValue(true).build());


    private final Setting<Boolean> pauseOnEat = sgPause.add(new BoolSetting.Builder().name("pause-on-eat").description("Pauses while eating.").defaultValue(true).build());
    private final Setting<Boolean> pauseOnDrink = sgPause.add(new BoolSetting.Builder().name("pause-on-drink").description("Pauses while drinking.").defaultValue(true).build());
    private final Setting<Boolean> pauseOnMine = sgPause.add(new BoolSetting.Builder().name("pause-on-mine").description("Pauses while mining.").defaultValue(true).build());

    private int wpt, delayTimer;


    private final ArrayList<Vec3i> places = new ArrayList<Vec3i>() {{
        add(new Vec3i(0, 1, 0));
        add(new Vec3i(0, 2, 0));
    }};


    public BedDisabler() {
        super(Orion.CATEGORY, "bed-disabler", "Anti bed that works.");
    }


    @Override
    public void onActivate() {
        wpt = 0;
        delayTimer = delay.get();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        FindItemResult string = InvUtils.findInHotbar(Items.STRING);
        if (!string.found()) {
            error("No string found in hotbar.");
            toggle();
            return;
        }
        if (onlyInHole.get() && !Wrapper.isInHole(mc.player)) {
            warning("You're not in a hole!");
            toggle();
            return;
        }
        if (PlayerUtils.shouldPause(pauseOnMine.get(), pauseOnEat.get(), pauseOnDrink.get())) return;
        if (delayTimer <= 0) {
            delayTimer = delay.get();
        } else {
            delayTimer--;
            return;
        }
        doPlace(string);
    }


    private void doPlace(FindItemResult string) {
        wpt = 0;
        ArrayList<Vec3i> p = new ArrayList<>(places);
        if (!ignoreFeet.get()) p.add(new Vec3i(0, 0, 0));
        BlockPos ppos = mc.player.getBlockPos();
        for (Vec3i b: p) {
            if (wpt >= webPerTick.get()) break;
            BlockPos bb = ppos.add(b.getX(), b.getY(), b.getZ());
            BlockUtils.place(bb, string, 50, false);
            wpt++;
        }

    }
}
