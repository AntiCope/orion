package me.ghosttypes.orion.modules.hud.items.text;

import me.ghosttypes.orion.utils.misc.ItemCounter;
import meteordevelopment.meteorclient.systems.modules.render.hud.HUD;
import meteordevelopment.meteorclient.systems.modules.render.hud.modules.DoubleTextHudElement;

public class TextXP extends DoubleTextHudElement {
    public TextXP(HUD hud) {
        super(hud, "xp-count", "Display the amount of xp in your inventory", "XP: ");
    }

    @Override
    protected String getRight() {
        return String.valueOf(ItemCounter.xp());
    }
}