package me.ghosttypes.orion.modules.hud.stats;

import me.ghosttypes.orion.utils.misc.Stats;
import meteordevelopment.meteorclient.systems.modules.render.hud.HUD;
import meteordevelopment.meteorclient.systems.modules.render.hud.modules.DoubleTextHudElement;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class KDRatio extends DoubleTextHudElement {
    public KDRatio(HUD hud) {
        super(hud, "KD", "Display your kills/death ratio", "K/D: ");
    }

    @Override
    protected String getRight() { return getKD(); }

    private String getKD() {
        if (Stats.deaths == 0) return String.valueOf(Stats.kills); //make sure we don't try to divide by 0
        Double rawKD = (double) (Stats.kills / Stats.deaths);
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(rawKD);
    }
}
