package me.ghosttypes.orion.utils.chat;

import me.ghosttypes.orion.modules.chat.PopCounter;
import me.ghosttypes.orion.modules.main.BedAura;
import me.ghosttypes.orion.utils.Wrapper;
import me.ghosttypes.orion.utils.misc.Stats;
import me.ghosttypes.orion.utils.misc.StringHelper;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.CrystalAura;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.starscript.Script;
import meteordevelopment.starscript.compiler.Compiler;
import meteordevelopment.starscript.compiler.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EzUtil {
    private static final Random RANDOM = new Random();
    public static List<String> currentTargets = new ArrayList<>();

    public static void sendAutoEz(String playerName) {
        increaseKC();
        MeteorStarscript.ss.set("killed", playerName);
        PopCounter popCounter = Modules.get().get(PopCounter.class);
        if (popCounter.ezScripts.isEmpty()) {
            ChatUtils.warning("Your auto ez message list is empty!");
            return;
        }

        var script = popCounter.ezScripts.get(RANDOM.nextInt(popCounter.ezScripts.size()));

        StringBuilder stringBuilder = new StringBuilder(MeteorStarscript.ss.run(script).toString());
        if (popCounter.killStr.get()) stringBuilder.append(" | Killstreak: ").append(Stats.killStreak);
        if (popCounter.suffix.get()) stringBuilder.append(MeteorStarscript.ss.run(popCounter.suffixScript).toString());

        String ezMessage = stringBuilder.toString();
        ChatUtils.sendPlayerMsg(ezMessage);
        if (popCounter.pmEz.get()) Wrapper.messagePlayer(playerName, StringHelper.stripName(playerName, ezMessage));
    }

    public static void increaseKC() {
        Stats.kills++;
        Stats.killStreak++;
    }

    public static void updateTargets() {
        currentTargets.clear();
        ArrayList<Module> modules = new ArrayList<>();
        modules.add(Modules.get().get(CrystalAura.class));
        modules.add(Modules.get().get(KillAura.class));
        modules.add(Modules.get().get(BedAura.class));
        for (Module module : modules) currentTargets.add(module.getInfoString());
    }
}
