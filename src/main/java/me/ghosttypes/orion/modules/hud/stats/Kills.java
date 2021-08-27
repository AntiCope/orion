package me.ghosttypes.orion.modules.hud.stats;

import me.ghosttypes.orion.utils.misc.Stats;
import meteordevelopment.meteorclient.systems.modules.render.hud.HUD;
import meteordevelopment.meteorclient.systems.modules.render.hud.modules.DoubleTextHudElement;

public class Kills extends DoubleTextHudElement {
    public Kills(HUD hud) {
        super(hud, "kills", "Display your total kills", "Kills: ");
    }

    @Override
    protected String getRight() {
        return String.valueOf(Stats.kills);
    }
}