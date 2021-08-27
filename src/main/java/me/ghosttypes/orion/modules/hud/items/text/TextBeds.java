package me.ghosttypes.orion.modules.hud.items.text;

import me.ghosttypes.orion.utils.misc.ItemCounter;
import meteordevelopment.meteorclient.systems.modules.render.hud.HUD;
import meteordevelopment.meteorclient.systems.modules.render.hud.modules.DoubleTextHudElement;

public class TextBeds extends DoubleTextHudElement {
    public TextBeds(HUD hud) {
        super(hud, "bed-count", "Display the amount of beds in your inventory", "Beds: ");
    }

    @Override
    protected String getRight() {
        return String.valueOf(ItemCounter.beds());
    }
}
