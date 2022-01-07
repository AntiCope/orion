package me.ghosttypes.orion.modules.hud.visual;

import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.modules.HudElement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class VisualBinds extends HudElement {
    public VisualBinds(HUD hud) {
        super(hud, "visual-binds", "Display keybound modules and their bind.", false);
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SortMode> sortMode = sgGeneral.add(new EnumSetting.Builder<SortMode>().name("sort-mode").description("How to sort the binds list.").defaultValue(SortMode.Shortest).build());

    private final ArrayList<String> binds = new ArrayList<>();

    private void updateBinds() {
        binds.clear();
        List<Module> modules = Modules.get().getAll().stream()
            .filter(module -> module.keybind.isSet())
            .collect(Collectors.toList());
        for (Module module : modules) binds.add(module.title + ": [" + module.keybind.toString() + "]");
        if (sortMode.get().equals(SortMode.Shortest)) {
            binds.sort(Comparator.comparing(String::length));
        } else {
            binds.sort(Comparator.comparing(String::length).reversed());
        }
    }


    @Override
    public void update(HudRenderer renderer) {
        updateBinds();
        double width = 0;
        double height = 0;
        int i = 0;
        if (binds.isEmpty()) {
            String t = "You have no keybound modules.";
            width = Math.max(width, renderer.textWidth(t));
            height += renderer.textHeight();
        } else {
            for (String bind : binds) {
                width = Math.max(width, renderer.textWidth(bind));
                height += renderer.textHeight();
                if (i > 0) height += 2;
                i++;
            }
        }
        box.setSize(width, height);
    }

    @Override
    public void render(HudRenderer renderer) {
        updateBinds();
        double x = box.getX();
        double y = box.getY();
        if (isInEditor()) {
            renderer.text("Keybinds", x, y, hud.secondaryColor.get());
            return;
        }
        int i = 0;
        if (binds.isEmpty()) {
            String t = "You have no keybound modules.";
            renderer.text(t, x + box.alignX(renderer.textWidth(t)), y, hud.secondaryColor.get());
        } else {
            for (String bind: binds) {
                renderer.text(bind, x + box.alignX(renderer.textWidth(bind)), y, hud.secondaryColor.get());
                y += renderer.textHeight();
                if (i > 0) y += 2;
                i++;
            }
        }
    }

    public enum SortMode {
        Longest,
        Shortest
    }
}
