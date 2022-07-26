package me.ghosttypes.orion.modules.hud;

import me.ghosttypes.orion.Orion;
import me.ghosttypes.orion.modules.chat.ChatTweaks;
import me.ghosttypes.orion.utils.misc.Stats;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.starscript.value.Value;
import meteordevelopment.starscript.value.ValueMap;

public class OrionHud {
    public static final HudElementInfo<TextHud> INFO = new HudElementInfo<>(Orion.HUD_GROUP, "orion-text", "Displays arbitrary text with Starscript.", OrionHud::create);

    public static final HudElementInfo<TextHud>.Preset WELCOME;
    public static final HudElementInfo<TextHud>.Preset WATERMARK;

    public static final HudElementInfo<TextHud>.Preset KILLS;
    public static final HudElementInfo<TextHud>.Preset DEATHS;
    public static final HudElementInfo<TextHud>.Preset HIGHSCORE;
    public static final HudElementInfo<TextHud>.Preset KDRATIO;
    public static final HudElementInfo<TextHud>.Preset KILLSTREAK;

    static {
        WELCOME = addPreset("Welcome", "Welcome to Orion, #1{player}");
        WATERMARK = addPreset("Watermark", "{orion_prefix} #1{orion_version}");

        KILLS = addPreset("Kills", "Kills: #1{stats.kills}");
        DEATHS = addPreset("Deaths", "Deaths: #1{stats.deaths}");
        HIGHSCORE = addPreset("Highscore", "Highscore: #1{stats.highscore}");
        KDRATIO = addPreset("KD", "K/D: #1{stats.deaths == 0 ? stats.kills : round(stats.kills / stats.deaths, 2)}");
        KILLSTREAK = addPreset("Kill Streak", "Kill Streak: #1{stats.kill_streak}");
    }

    private static TextHud create() {
        return new TextHud(INFO);
    }

    private static HudElementInfo<TextHud>.Preset addPreset(String title, String text) {
        return INFO.addPreset(title, textHud -> {
            if (text != null) textHud.text.set(text);
        });
    }

    public static void starscriptAdd() {
        MeteorStarscript.ss.set("orion_version", Orion.VERSION);
        MeteorStarscript.ss.set("orion_prefix", () -> {
            ChatTweaks chatTweaks = Modules.get().get(ChatTweaks.class);
            if (chatTweaks.isActive() && chatTweaks.customPrefix.get()) {
                return Value.string(chatTweaks.prefixText.get());
            }
            return Value.string("Orion");
        });
        MeteorStarscript.ss.set("stats", new ValueMap()
            .set("kills", () -> Value.number(Stats.kills))
            .set("deaths", () -> Value.number(Stats.deaths))
            .set("kill_streak", () -> Value.number(Stats.killStreak))
            .set("highscore", () -> Value.number(Stats.highscore))
        );
    }
}
