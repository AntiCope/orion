package me.ghosttypes.orion.modules.main;

import me.ghosttypes.orion.Orion;
import me.ghosttypes.orion.modules.chat.BurrowAlert;
import me.ghosttypes.orion.utils.Wrapper;
import me.ghosttypes.orion.utils.chat.EzUtil;
import me.ghosttypes.orion.utils.misc.Stats;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DeathScreen;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AutoRespawn extends Module {
    private final SettingGroup sgRekit = settings.createGroup("Rekit");
    private final SettingGroup sgExcuse = settings.createGroup("AutoExcuse");
    private final SettingGroup sgHS = settings.createGroup("HighScore");

    private final Setting<Boolean> rekit = sgRekit.add(new BoolSetting.Builder().name("rekit").description("Rekit after dying on pvp servers.").defaultValue(false).build());
    private final Setting<String> kitName = sgRekit.add(new StringSetting.Builder().name("kit-name").description("The name of your kit.").defaultValue("default").build());

    private final Setting<Boolean> excuse = sgExcuse.add(new BoolSetting.Builder().name("excuse").description("Send an excuse to global chat after death.").defaultValue(false).build());
    private final Setting<Boolean> randomize = sgExcuse.add(new BoolSetting.Builder().name("randomize").description("Randomizes the excuse message.").defaultValue(false).build());
    private final Setting<List<String>> messages = sgExcuse.add(new StringListSetting.Builder().name("excuse-messages").description("Messages to use for AutoExcuse").defaultValue(Collections.emptyList()).build());

    private final Setting<Boolean> alertHS = sgHS.add(new BoolSetting.Builder().name("alert").description("Alerts you client side when you reach a new highscore.").defaultValue(false).build());
    private final Setting<Boolean> announceHS = sgHS.add(new BoolSetting.Builder().name("announce").description("Announce when you reach a new highscore.").defaultValue(false).build());



    private boolean shouldRekit = false;
    private boolean shouldExcuse = false;
    private boolean shouldHS = false;
    private int excuseWait = 50;
    private int rekitWait = 50;
    private int messageI = 0;

    public AutoRespawn() {
        super(Orion.CATEGORY, "auto-respawn", "Automatically respawns after death.");
    }

    @EventHandler
    private void onOpenScreenEvent(OpenScreenEvent event) {
        if (!(event.screen instanceof DeathScreen)) return;
        mc.player.requestRespawn();
        if (rekit.get()) shouldRekit = true;
        if (excuse.get()) shouldExcuse = true;
        Stats.deaths++;
        //clear these when we die
        BurrowAlert.burrowedPlayers.clear();
        EzUtil.ezdPlayers.clear();
        if (Stats.killStreak > Stats.highscore) {
            shouldHS = true;
            Stats.highscore = Stats.killStreak;
        }
        Stats.killStreak = 0;
        event.cancel();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (Wrapper.isLagging()) return;
        if (shouldRekit && rekitWait <= 1) {
            if (shouldHS) {
                if (alertHS.get()) info("You reached a new highscore of " + Stats.highscore + "!");
                if (announceHS.get()) mc.player.sendChatMessage("I reached a new highscore of " + Stats.highscore + " thanks to Orion!");
                shouldHS = false;
            }
            info("Rekitting with kit " + kitName.get());
            mc.player.sendChatMessage("/kit " + kitName.get());
            shouldRekit = false;
            shouldHS = false;
            rekitWait = 50;
            return;
        } else { rekitWait--; }
        if (shouldExcuse && excuseWait <= 1) {
            String excuseMessage = getExcuseMessage();
            mc.player.sendChatMessage(excuseMessage);
            shouldExcuse = false;
            excuseWait = 50;
        } else { excuseWait--; }
    }

    private String getExcuseMessage() {
        String excuseMessage;
        if (messages.get().isEmpty()) {
            error("Your excuse message list is empty!");
            return "Lag";
        } else {
            if (randomize.get()) {
                excuseMessage = messages.get().get(new Random().nextInt(messages.get().size()));
            } else {
                if (messageI >= messages.get().size()) messageI = 0;
                int i = messageI++;
                excuseMessage = messages.get().get(i);
            }
        }
        return excuseMessage;
    }


}

