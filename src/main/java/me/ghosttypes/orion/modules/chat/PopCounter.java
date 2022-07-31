package me.ghosttypes.orion.modules.chat;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.ghosttypes.orion.Orion;
import me.ghosttypes.orion.utils.Wrapper;
import me.ghosttypes.orion.utils.chat.EzUtil;
import me.ghosttypes.orion.utils.misc.Stats;
import me.ghosttypes.orion.utils.misc.StringHelper;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.starscript.Script;
import meteordevelopment.starscript.compiler.Parser;
import meteordevelopment.starscript.compiler.Compiler;

import meteordevelopment.starscript.utils.StarscriptError;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Formatting;

import java.util.*;

public class PopCounter extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAutoEz = settings.createGroup("AutoEz");
    private final SettingGroup sgMessages = settings.createGroup("Messages");

    private final Setting<Boolean> own = sgGeneral.add(new BoolSetting.Builder().name("own").description("Notifies you of your own totem pops.").defaultValue(false).build());
    private final Setting<Boolean> friends = sgGeneral.add(new BoolSetting.Builder().name("friends").description("Notifies you of your friends totem pops.").defaultValue(true).build());
    private final Setting<Boolean> others = sgGeneral.add(new BoolSetting.Builder().name("others").description("Notifies you of other players totem pops.").defaultValue(true).build());
    private final Setting<Boolean> announceOthers = sgGeneral.add(new BoolSetting.Builder().name("announce").description("Announces when other players pop totems in global chat.").defaultValue(false).visible(others::get).build());
    private final Setting<Boolean> pmOthers = sgGeneral.add(new BoolSetting.Builder().name("pm").description("Message players when they pop a totem.").defaultValue(false).visible(announceOthers::get).build());
    private final Setting<Integer> announceDelay = sgGeneral.add(new IntSetting.Builder().name("announce-delay").description("How many seconds between announcing totem pops.").defaultValue(5).min(1).sliderMax(100).visible(announceOthers::get).build());
    private final Setting<Double> announceRange = sgGeneral.add(new DoubleSetting.Builder().name("announce-range").description("How close players need to be to announce pops or AutoEz.").defaultValue(3).min(0).sliderMax(10).visible(announceOthers::get).build());
    private final Setting<Boolean> dontAnnounceFriends = sgGeneral.add(new BoolSetting.Builder().name("dont-announce-friends").description("Don't annnounce when your friends pop.").defaultValue(true).build());
    public final Setting<Boolean> autoEz = sgAutoEz.add(new BoolSetting.Builder().name("auto-ez").description("Sends a message when you kill players.").defaultValue(false).build());
    public final Setting<Boolean> suffix = sgAutoEz.add(new BoolSetting.Builder().name("suffix").description("Add Orion suffix to the end of pop messages.").defaultValue(false).visible(autoEz::get).build());
    public final Setting<String> suffixMessage = sgAutoEz.add(new StringSetting.Builder().name("suffix-message").description("The suffix to be added at the end of pop messages.").renderer(StarscriptTextBoxRenderer.class).defaultValue(" | {orion_prefix} {orion_version}").visible(suffix::get).onChanged(e -> updateSuffixScript()).build());
    public final Setting<Boolean> killStr = sgAutoEz.add(new BoolSetting.Builder().name("killstreak").description("Add your killstreak to the end of autoez messages").defaultValue(false).visible(autoEz::get).build());
    public final Setting<Boolean> pmEz = sgAutoEz.add(new BoolSetting.Builder().name("pm-ez").description("Send the autoez message to the player's dm.").defaultValue(false).visible(autoEz::get).build());
    public final Setting<List<String>> popMessages = sgMessages.add(new StringListSetting.Builder().name("pop-messages").description("Messages to use when announcing pops.").renderer(StarscriptTextBoxRenderer.class).defaultValue(Collections.emptyList()).onChanged(e -> updatePopScripts()).build());
    public final Setting<List<String>> ezMessages = sgMessages.add(new StringListSetting.Builder().name("ez-messages").description("Messages to use for autoez.").renderer(StarscriptTextBoxRenderer.class).defaultValue(Collections.emptyList()).visible(() -> autoEz.get() || announceOthers.get()).onChanged(e -> updateEzScripts()).build());

    public final Object2IntMap<UUID> totemPops = new Object2IntOpenHashMap<>();
    private final Object2IntMap<UUID> chatIds = new Object2IntOpenHashMap<>();

    private static final Random RANDOM = new Random();
    private int updateWait = 45;
    public Script suffixScript;
    private final List<Script> popScripts = new ArrayList<>();
    public final List<Script> ezScripts = new ArrayList<>();

    public PopCounter() {
        super(Orion.CATEGORY, "pop-counter", "Count player's totem pops.");
        updateSuffixScript();
        updateEzScripts();
        updatePopScripts();
    }

    private void updateSuffixScript() {
        String suffix = suffixMessage.get();
        suffixScript = compile(suffix);
    }

    private void updatePopScripts() {
        List<String> list = popMessages.get();
        popScripts.clear();
        for (var entry : list) {
            var script = compile(entry);
            if (script != null) popScripts.add(script);
        }
    }

    private void updateEzScripts() {
        List<String> list = ezMessages.get();
        ezScripts.clear();
        for (var entry : list) {
            var script = compile(entry);
            if (script != null) ezScripts.add(script);
        }
    }
    private int announceWait;

    @Override
    public void onActivate() {
        EzUtil.updateTargets();
        totemPops.clear();
        chatIds.clear();
        announceWait = announceDelay.get() * 20;
    }

    @EventHandler
    private void onGameJoin(GameJoinedEvent event) {
        Stats.reset();
        totemPops.clear();
        chatIds.clear();
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (!(event.packet instanceof EntityStatusS2CPacket p)) return;

        if (p.getStatus() != 35) return;

        Entity entity = p.getEntity(mc.world);
        if (entity != null && ! (entity instanceof PlayerEntity)) return;
        if (entity == null
                || (entity.equals(mc.player) && !own.get())
                || (Friends.get().isFriend(((PlayerEntity) entity)) && !others.get())
                || (!Friends.get().isFriend(((PlayerEntity) entity)) && !friends.get())
        ) return;

        synchronized (totemPops) {
            int pops = totemPops.getOrDefault(entity.getUuid(), 0);
            totemPops.put(entity.getUuid(), ++pops);

            ChatUtils.sendMsg(getChatId(entity), Formatting.GRAY, "(highlight)%s (default)popped (highlight)%d (default)%s.", entity.getEntityName(), pops, pops == 1 ? "totem" : "totems");
        }
        if (announceOthers.get() && announceWait <= 1 && mc.player.distanceTo(entity) <= announceRange.get()) {
            if (dontAnnounceFriends.get() && Friends.get().isFriend((PlayerEntity) entity)) return;

            try {
                StringBuilder sb = new StringBuilder(getPopMessage((PlayerEntity) entity));
                if (suffix.get() && suffixScript != null) sb.append(MeteorStarscript.ss.run(suffixScript).toString());
                String popMessage = sb.toString();
                ChatUtils.sendPlayerMsg(popMessage);

                if (pmOthers.get()) {
                    String name = entity.getEntityName();
                    Wrapper.messagePlayer(name, StringHelper.stripName(name, popMessage));
                }
            } catch (StarscriptError error) {
                MeteorStarscript.printChatError(error);
            }

            announceWait = announceDelay.get() * 20;
        }
    }


    @EventHandler
    private void onTick(TickEvent.Post event) {
        updateWait--;
        if (updateWait <= 0) {
            EzUtil.updateTargets();
            updateWait = 45;
        }
        announceWait--;
        synchronized (totemPops) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (!totemPops.containsKey(player.getUuid())) continue;

                if (player.deathTime > 0 || player.getHealth() <= 0) {
                    int pops = totemPops.removeInt(player.getUuid());

                    ChatUtils.sendMsg(getChatId(player), Formatting.GRAY, "(highlight)%s (default)popped (highlight)%d (default)%s.", player.getEntityName(), pops, pops == 1 ? "totem" : "totems");
                    chatIds.removeInt(player.getUuid());
                    if (EzUtil.currentTargets.contains(player.getEntityName())) EzUtil.sendAutoEz(player.getEntityName());
                }
            }
        }
    }

    private int getChatId(Entity entity) throws StarscriptError {
        return chatIds.computeIfAbsent(entity.getUuid(), value -> RANDOM.nextInt());
    }

    private String getPopMessage(PlayerEntity p) {
        MeteorStarscript.ss.set("pops", totemPops.getOrDefault(p.getUuid(), 0));
        MeteorStarscript.ss.set("killed", p.getEntityName());

        if (popScripts.isEmpty()) {
            ChatUtils.warning("Your pop message list is empty!");
            return "Ez pop";
        }

        Script script = popScripts.get(RANDOM.nextInt(popScripts.size()));

        return MeteorStarscript.ss.run(script).toString();
    }

    private static Script compile(String script) {
        if (script == null) return null;
        Parser.Result result = Parser.parse(script);
        if (result.hasErrors()) {
            MeteorStarscript.printChatError(result.errors.get(0));
            return null;
        }
        return Compiler.compile(result);
    }
}
