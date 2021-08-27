package me.ghosttypes.orion.modules.hud.items.text;

import me.ghosttypes.orion.utils.misc.ItemCounter;
import meteordevelopment.meteorclient.systems.modules.render.hud.HUD;
import meteordevelopment.meteorclient.systems.modules.render.hud.modules.DoubleTextHudElement;

public class TextCrystals extends DoubleTextHudElement {
    public TextCrystals(HUD hud) {
        super(hud, "crystal-count", "Display the amount of crystals in your inventory", "Crystals: ");
    }

    @Override
    protected String getRight() {
        return String.valueOf(ItemCounter.crystals());
    }
}