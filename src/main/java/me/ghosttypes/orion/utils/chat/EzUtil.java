package me.ghosttypes.orion.utils.chat;

import me.ghosttypes.orion.Orion;
import me.ghosttypes.orion.modules.chat.PopCounter;
import me.ghosttypes.orion.utils.Wrapper;
import me.ghosttypes.orion.utils.misc.Placeholders;
import me.ghosttypes.orion.utils.misc.Stats;
import me.ghosttypes.orion.utils.misc.StringHelper;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static meteordevelopment.meteorclient.utils.Utils.mc;


public class EzUtil {
    public static List<String> ezdPlayers = new ArrayList<>();

    public static void sendAutoEz(String playerName) {
        if (ezdPlayers.contains(playerName)) return;
        PopCounter popCounter = Modules.get().get(PopCounter.class);
        List<String> ezMessages = popCounter.ezMessages.get();
        if (ezMessages.isEmpty()) {
            ChatUtils.warning("Your auto ez message list is empty!");
            return;
        }
        String ezMessage = ezMessages.get(new Random().nextInt(ezMessages.size()));
        ezdPlayers.add(playerName);
        if (ezMessage.contains("{player}")) ezMessage = ezMessage.replace("{player}", playerName);
        if (popCounter.doPlaceholders.get()) ezMessage = Placeholders.apply(ezMessage);
        if (popCounter.killStr.get()) { ezMessage = ezMessage + " | Killstreak: " + Stats.killStreak; }
        if (popCounter.suffix.get()) { ezMessage = ezMessage + " | Ghostware " + Orion.VERSION; }
        mc.player.sendChatMessage(ezMessage);
        if (popCounter.pmEz.get()) Wrapper.messagePlayer(playerName, StringHelper.stripName(playerName, ezMessage));
    }

    public static void sendBedEz(String playerName) {
        if (ezdPlayers.contains(playerName)) return;
        PopCounter popCounter = Modules.get().get(PopCounter.class);
        List<String> ezMessages = popCounter.bedEzMessages.get();
        if (ezMessages.isEmpty()) {
            ChatUtils.warning("Your auto ez message list is empty!");
            return;
        }
        String ezMessage = ezMessages.get(new Random().nextInt(ezMessages.size()));
        ezdPlayers.add(playerName);
        if (ezMessage.contains("{player}")) ezMessage = ezMessage.replace("{player}", playerName);
        if (popCounter.doPlaceholders.get()) ezMessage = Placeholders.apply(ezMessage);
        if (popCounter.killStr.get()) ezMessage = ezMessage + " | Killstreak: " + Stats.killStreak;
        if (popCounter.suffix.get()) { ezMessage = ezMessage + " | Ghostware " + Orion.VERSION; }
        mc.player.sendChatMessage(ezMessage);
        if (popCounter.pmEz.get()) Wrapper.messagePlayer(playerName, StringHelper.stripName(playerName, ezMessage));
    }

}
