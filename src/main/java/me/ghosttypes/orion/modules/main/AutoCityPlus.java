package me.ghosttypes.orion.modules.main;

import me.ghosttypes.orion.Orion;
import me.ghosttypes.orion.utils.Wrapper;
import me.ghosttypes.orion.utils.world.BlockHelper;
import me.ghosttypes.orion.utils.player.AutomationUtils;
import me.ghosttypes.orion.utils.player.ItemHelper;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;


public class AutoCityPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSurrBreak = settings.createGroup("SurroundBreak");
    private final SettingGroup sgRender = settings.createGroup("Render");

    //General
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder().name("delay").description("The delay between city attempts.").defaultValue(10).min(0).sliderMax(50).build());
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder().name("range").description("The maximum range to check for a city block.").defaultValue(5).min(0).sliderMax(20).build());
    private final Setting<Boolean> support = sgGeneral.add(new BoolSetting.Builder().name("support").description("Place a block below the city block if needed.").defaultValue(true).build());
    private final Setting<Boolean> silent = sgGeneral.add(new BoolSetting.Builder().name("silent-switch").description("Don't switch to a pick until the city block is ready to be broken.").defaultValue(true).build());
    private final Setting<Boolean> instant = sgGeneral.add(new BoolSetting.Builder().name("instant").description("Insta mines the city block if it's replaced by the target.").defaultValue(false).build());
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder().name("rotate").description("Rotate on block interactions.").defaultValue(true).build());
    private final Setting<Boolean> randomize = sgGeneral.add(new BoolSetting.Builder().name("randomize").description("Randomize the block being mined each time.").defaultValue(true).build());
    private final Setting<Boolean> autoToggle = sgGeneral.add(new BoolSetting.Builder().name("disable-after").description("Disable after the first city attempt.").defaultValue(true).build());

    //SurroundBreak
    private final Setting<Boolean> sbInside = sgSurrBreak.add(new BoolSetting.Builder().name("place-crystal-inside").description("Place a crystal after the surround is mined.").defaultValue(false).build());
    //Render
    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder().name("render").description("Renders the current block being mined.").defaultValue(true).build());
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).build());
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder().name("side-color").description("The side color.").defaultValue(new SettingColor(15, 255, 211,75)).build());
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder().name("line-color").description("The line color.").defaultValue(new SettingColor(15, 255, 211)).build());

    public AutoCityPlus() {
        super(Orion.CATEGORY, "auto-city-plus", "Auto city but better.");
    }

    private PlayerEntity target;
    private BlockPos cityBlock;
    private boolean isMining, triedSupport, didFirstMine;
    private FindItemResult pick;
    private int mineTimer, timeMining;

    @Override
    public void onActivate() {
        target = null;
        cityBlock = null;
        mineTimer = 0;
        timeMining = 0;
        isMining = false;
        triedSupport = false;
        didFirstMine = false;
        pick = ItemHelper.findPick();
        if (!pick.found()) {
            error( "No pickaxe in hotbar.");
            this.toggle();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        target = TargetUtils.getPlayerTarget(range.get(), SortPriority.ClosestAngle);
        if (target == null) {
            error( "No target in range.");
            toggle();
            return;
        }
        if (AutomationUtils.canCrystal(target)) {
            if (cityBlock != null && sbInside.get()) placeCrystal(cityBlock.down());
            isMining = false;
            mineTimer = delay.get();
            timeMining = 0;
            cityBlock = null;
            didFirstMine = false;
            return;
        }
        if (cityBlock == null) {
            triedSupport = false;
            cityBlock = BlockHelper.getCityBlock(target, randomize.get());
        }
        if (cityBlock == null) {
            if (didFirstMine && autoToggle.get()) error( "City complete.");
            error( "No city block available.");
            toggle();
            return; }
        if (outOfRange()) {
            error( "City block is unreachable.");
            toggle();
            return; }
        if (isMining) timeMining++;
        if (mineTimer > 0) { mineTimer--; return; }
        if (support.get() && needsSupport() && !triedSupport) placeSupport();
        if (silent.get() && timeMining >= 55) Wrapper.updateSlot(pick.getSlot());
        if (!silent.get()) Wrapper.updateSlot(pick.getSlot());
        if (!isMining) {
            isMining = true;
            if (!didFirstMine) didFirstMine = true;
            info("Citying " + target.getEntityName() + ".");
            if (rotate.get()) {
                Rotations.rotate(Rotations.getYaw(cityBlock), Rotations.getPitch(cityBlock), () -> mine(cityBlock));
            } else {
                mine(cityBlock);
            }
        }
        if (!AutomationUtils.isSurroundBlock(cityBlock)) {
            cityBlock = null;
            mineTimer = delay.get();
            timeMining = 0;
            isMining = false;
            if (autoToggle.get()) { info("City complete, disabling."); toggle(); }
        }
    }


    private boolean outOfRange() {
        return MathHelper.sqrt((float) mc.player.squaredDistanceTo(cityBlock.getX(), cityBlock.getY(), cityBlock.getZ())) > mc.interactionManager.getReachDistance();
    }

    private void mine(BlockPos blockPos) {
        if (!instant.get())
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
    }

    private void placeSupport() {
        triedSupport = true;
        FindItemResult obbySlot = InvUtils.findInHotbar(Items.OBSIDIAN);
        BlockPos blockPos = cityBlock.down();
        if (!BlockUtils.canPlace(blockPos)
                && BlockHelper.getBlock(blockPos) != Blocks.OBSIDIAN
                && BlockHelper.getBlock(blockPos) != Blocks.BEDROCK
                ) {
            warning("Couldn't place support block, mining anyway.");
        } else {
            if (!obbySlot.found()) {
                warning("No obsidian found for support, mining anyway.");
            } else {
                BlockUtils.place(blockPos, obbySlot, rotate.get(), 10, true);
            }
        }
    }

    private boolean needsSupport() {
        BlockPos supportPos = cityBlock.down();
        return BlockHelper.getBlock(supportPos) != Blocks.OBSIDIAN || BlockHelper.getBlock(supportPos) != Blocks.BEDROCK;
    }

    private void placeCrystal(BlockPos placePos) {
        FindItemResult crystalSlot = InvUtils.findInHotbar(Items.END_CRYSTAL);
        Hand hand;
        if (mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL) { hand = Hand.OFF_HAND; } else { hand = Hand.MAIN_HAND; }
        if (hand == Hand.MAIN_HAND && !crystalSlot.found()) return;
        if (hand == Hand.MAIN_HAND) Wrapper.updateSlot(crystalSlot.getSlot());
        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(mc.player.getPos(), getDirection(placePos), placePos, false)));
        Wrapper.swingHand(hand != Hand.MAIN_HAND);
    }

    private Direction getDirection(BlockPos pos) { //stolen from supakeks ez
        Vec3d eyesPos = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());
        for (Direction direction : Direction.values()) {
            RaycastContext raycastContext = new RaycastContext(eyesPos, new Vec3d(pos.getX() + 0.5 + direction.getVector().getX() * 0.5,
                    pos.getY() + 0.5 + direction.getVector().getY() * 0.5,
                    pos.getZ() + 0.5 + direction.getVector().getZ() * 0.5), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
            BlockHitResult result = mc.world.raycast(raycastContext);
            if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(pos)) {
                return direction;
            }
        }
        if ((double) pos.getY() > eyesPos.y) {
            return Direction.DOWN; // The player can never see the top of a block if they are under it
        }
        return Direction.UP;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (render.get() && cityBlock != null) { event.renderer.box(cityBlock, sideColor.get(), lineColor.get(), shapeMode.get(), 0); }
    }

    @Override
    public String getInfoString() {
        if (target != null) return target.getEntityName();
        return null;
    }
}



