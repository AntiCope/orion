package me.ghosttypes.orion.modules.main;

import me.ghosttypes.orion.Orion;
import me.ghosttypes.orion.utils.Wrapper;
import me.ghosttypes.orion.utils.world.BlockHelper;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class SelfTrapPlus extends Module {
    public enum modes {
        AntiFacePlace,
        Full,
        Top,
        None
    }


    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    // General

    private final Setting<modes> mode = sgGeneral.add(new EnumSetting.Builder<modes>().name("mode").description("Which positions to place on your top half.").defaultValue(modes.Top).build());
    private final Setting<Boolean> antiCev = sgGeneral.add(new BoolSetting.Builder().name("anti-cev-breaker").description("Protect yourself from cev breaker.").defaultValue(true).build());
    private final Setting<Integer> blockPerTick = sgGeneral.add(new IntSetting.Builder().name("blocks-per-tick").description("How many block placements per tick.").defaultValue(4).sliderMin(1).sliderMax(10).build());
    private final Setting<Boolean> center = sgGeneral.add(new BoolSetting.Builder().name("center").description("Centers you on the block you are standing on before placing.").defaultValue(true).build());
    private final Setting<Boolean> turnOff = sgGeneral.add(new BoolSetting.Builder().name("turn-off").description("Turns off after placing.").defaultValue(true).build());
    private final Setting<Boolean> toggleOnMove = sgGeneral.add(new BoolSetting.Builder().name("toggle-on-move").description("Turns off if you move (chorus, pearl phase etc).").defaultValue(true).build());
    private final Setting<Boolean> onlyInHole = sgGeneral.add(new BoolSetting.Builder().name("only-in-hole").description("Won't place unless you're in a hole").defaultValue(true).build());
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder().name("rotate").description("Sends rotation packets to the server when placing.").defaultValue(true).build());

    // Render

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder().name("render").description("Renders a block overlay where the obsidian will be placed.").defaultValue(true).build());
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).build());
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder().name("side-color").description("The color of the sides of the blocks being rendered.").defaultValue(new SettingColor(204, 0, 0, 10)).build());
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder().name("line-color").description("The color of the lines of the blocks being rendered.").defaultValue(new SettingColor(204, 0, 0, 255)).build());

    private final List<BlockPos> placePositions = new ArrayList<>();
    private BlockPos startPos;
    private int bpt;

    private final ArrayList<Vec3d> full = new ArrayList<Vec3d>() {{
        add(new Vec3d(0, 2, 0));
        add(new Vec3d(1, 1, 0));
        add(new Vec3d(-1, 1, 0));
        add(new Vec3d(0, 1, 1));
        add(new Vec3d(0, 1, -1));
    }};

    private final ArrayList<Vec3d> antiFacePlace = new ArrayList<Vec3d>() {{
        add(new Vec3d(1, 1, 0));
        add(new Vec3d(-1, 1, 0));
        add(new Vec3d(0, 1, 1));
        add(new Vec3d(0, 1, -1));
    }};


    public SelfTrapPlus(){
        super(Orion.CATEGORY, "self-trap-plus", "Places obsidian around your head.");
    }

    @Override
    public void onActivate() {
        if (!placePositions.isEmpty()) placePositions.clear();
        if (center.get()) PlayerUtils.centerPlayer();
        startPos = mc.player.getBlockPos();
        bpt = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        bpt = 0;
        FindItemResult obsidian = InvUtils.findInHotbar(Items.OBSIDIAN);
        if (!obsidian.found()) { error("No obsidian in hotbar!"); toggle(); return; }
        if (BlockHelper.isVecComplete(getTrapDesign()) && turnOff.get()) { info("Finished self trap."); toggle(); return;}
        if (toggleOnMove.get() && startPos != mc.player.getBlockPos()) { toggle(); return; }
        if (onlyInHole.get() && !Wrapper.isInHole(mc.player)) { toggle(); return; }
        for (Vec3d b : getTrapDesign()) {
            if (bpt >= blockPerTick.get()) return;
            BlockPos ppos = mc.player.getBlockPos();
            BlockPos bb = ppos.add((int) b.x, (int) b.y, (int) b.z);
            if (BlockHelper.getBlock(bb) == Blocks.AIR) {
                BlockUtils.place(bb, obsidian, rotate.get(), 100, true);
                bpt++;
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!render.get() || BlockHelper.isVecComplete(getTrapDesign())) return;
        for (Vec3d b: getTrapDesign()) {
            BlockPos ppos = mc.player.getBlockPos();
            BlockPos bb = ppos.add((int) b.x, (int) b.y, (int) b.z);
            if (BlockHelper.getBlock(bb) == Blocks.AIR) event.renderer.box(bb, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        }
    }

    private ArrayList<Vec3d> getTrapDesign() {
        ArrayList<Vec3d> trapDesign = new ArrayList<Vec3d>();
        switch (mode.get()) {
            case Full -> { trapDesign.addAll(full); }
            case Top -> { trapDesign.add(new Vec3d(0, 2, 0)); }
            case AntiFacePlace -> { trapDesign.addAll(antiFacePlace); }
        }
        if (antiCev.get()) { trapDesign.add(new Vec3d(0, 3, 0));}
        return trapDesign;
    }
}
