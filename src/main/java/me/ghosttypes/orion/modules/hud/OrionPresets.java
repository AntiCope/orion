package me.ghosttypes.orion.modules.hud;

import me.ghosttypes.orion.Orion;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;

public class OrionPresets {
    public static final HudElementInfo<TextHud> INFO = new HudElementInfo<>(Orion.HUD_GROUP, "orion-text", "Displays arbitrary text with Starscript.", OrionPresets::create);

    public static final HudElementInfo<TextHud>.Preset WELCOME;
    public static final HudElementInfo<TextHud>.Preset WATERMARK;

    static {
        WELCOME = addPreset("Welcome", "Welcome to Orion, #1{player}");
        WATERMARK = addPreset("Watermark", "{orion_prefix} #1{orion_version}");
    }

    private static TextHud create() {
        return new TextHud(INFO);
    }

    private static HudElementInfo<TextHud>.Preset addPreset(String title, String text) {
        return INFO.addPreset(title, textHud -> {
            if (text != null) textHud.text.set(text);
        });
    }
}
