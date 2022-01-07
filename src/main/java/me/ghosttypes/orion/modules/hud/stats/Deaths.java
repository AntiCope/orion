package me.ghosttypes.orion.modules.hud.stats;

import me.ghosttypes.orion.utils.misc.Stats;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.systems.hud.modules.DoubleTextHudElement;

public class Deaths extends DoubleTextHudElement {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> fakeDeaths = sgGeneral.add(new BoolSetting.Builder().name("fake").description("Always display 0 for le troll").defaultValue(false).build());
    public Deaths(HUD hud) {
        super(hud, "deaths", "Display your total deaths", "Deaths: ");
    }

    @Override
    protected String getRight() {
        if (fakeDeaths.get()) return "0";
        return String.valueOf(Stats.deaths);
    }
}