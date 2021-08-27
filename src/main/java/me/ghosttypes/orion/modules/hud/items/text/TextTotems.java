package me.ghosttypes.orion.modules.hud.items.text;

import me.ghosttypes.orion.utils.misc.ItemCounter;
import meteordevelopment.meteorclient.systems.modules.render.hud.HUD;
import meteordevelopment.meteorclient.systems.modules.render.hud.modules.DoubleTextHudElement;

public class TextTotems extends DoubleTextHudElement {
    public TextTotems(HUD hud) {
        super(hud, "totem-count", "Display the amount of totems in your inventory", "Totems: ");
    }

    @Override
    protected String getRight() {
        return String.valueOf(ItemCounter.totem());
    }
}