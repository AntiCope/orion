package me.ghosttypes.orion.modules.hud.visual;


import me.ghosttypes.orion.Orion;
import me.ghosttypes.orion.modules.chat.ChatTweaks;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.systems.hud.modules.DoubleTextHudElement;

public class Watermark extends DoubleTextHudElement {
    public Watermark(HUD hud) {
        super(hud, "Orion-watermark", "Display Orion Watermark!.", "");
    }

    @Override
    protected String getRight() {
        ChatTweaks chatTweaks = Modules.get().get(ChatTweaks.class);
        if (chatTweaks.isActive() && chatTweaks.customPrefix.get()) {
            return chatTweaks.prefixText.get() + " " + Orion.VERSION;
        }
        return "Orion " + Orion.VERSION; }
}
