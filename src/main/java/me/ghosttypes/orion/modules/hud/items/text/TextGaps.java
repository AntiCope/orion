package me.ghosttypes.orion.modules.hud.items.text;

import me.ghosttypes.orion.utils.misc.ItemCounter;
import meteordevelopment.meteorclient.systems.modules.render.hud.HUD;
import meteordevelopment.meteorclient.systems.modules.render.hud.modules.DoubleTextHudElement;

public class TextGaps extends DoubleTextHudElement {
    public TextGaps(HUD hud) {
        super(hud, "egap-count", "Display the amount of egaps in your inventory", "EGaps: ");
    }

    @Override
    protected String getRight() {
        return String.valueOf(ItemCounter.egaps());
    }
}