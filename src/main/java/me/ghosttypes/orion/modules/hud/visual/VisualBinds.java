package me.ghosttypes.orion.modules.hud.visual;

import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.hud.HUD;
import meteordevelopment.meteorclient.systems.modules.render.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.render.hud.modules.HudElement;

import java.util.List;
import java.util.stream.Collectors;

public class VisualBinds extends HudElement {
    public VisualBinds(HUD hud) {
        super(hud, "visual-binds", "Display keybound modules and their bind.", false);
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();


    @Override
    public void update(HudRenderer renderer) {
        List<Module> modules = Modules.get().getAll().stream()
                .filter(module -> module.keybind.isSet())
                .collect(Collectors.toList());
        if (isInEditor()) {
            box.setSize(renderer.textWidth("Keybinds"), renderer.textHeight());
            return;
        }

        double width = 0;
        double height = 0;

        int i = 0;
        for (Module module : modules) {
            String bind = module.title + ": [" + module.keybind.toString() + "]";
            width = Math.max(width, renderer.textWidth(bind));
            height += renderer.textHeight();
            if (i > 0) height += 2;
            i++;
        }

        box.setSize(width, height);
    }

    @Override
    public void render(HudRenderer renderer) {
        List<Module> modules = Modules.get().getAll().stream()
                .filter(module -> module.keybind.isSet())
                .collect(Collectors.toList());
        double x = box.getX();
        double y = box.getY();
        if (isInEditor()) {
            renderer.text("Keybinds", x, y, hud.secondaryColor.get());
            return;
        }

        int i = 0;
        for (Module module : modules) {
            String bind = module.title + ": [" + module.keybind.toString() + "]";
            renderer.text(bind, x + box.alignX(renderer.textWidth(bind)), y, hud.secondaryColor.get());
            y += renderer.textHeight();
            if (i > 0) y += 2;
            i++;
        }
    }
}
