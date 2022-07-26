package me.ghosttypes.orion.modules.hud;

import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.RainbowColor;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.util.Identifier;

import me.ghosttypes.orion.Orion;

public class Logo extends HudElement {
    public static final HudElementInfo<Logo> INFO = new HudElementInfo<>(Orion.HUD_GROUP, "Orion-logo", "Displays the Orion logo.", Logo::new);

    private static final Identifier LOGO = new Identifier("orion", "logo.png");
    private static final Identifier LOGO_FLAT = new Identifier("orion", "logo_flat.png");

    private static final RainbowColor RAINBOW = new RainbowColor();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder().name("scale").description("The scale.").defaultValue(2).min(1).sliderMin(1).sliderMax(5).onChanged(setting -> update()).build());
    public final Setting<Boolean> chroma = sgGeneral.add(new BoolSetting.Builder().name("chroma").description("Chroma logo animation.").defaultValue(false).build());
    private final Setting<Double> chromaSpeed = sgGeneral.add(new DoubleSetting.Builder().name("chroma-speed").description("Speed of the chroma animation.").defaultValue(0.09).min(0.01).sliderMax(5).decimalPlaces(2).onChanged(setting -> RAINBOW.setSpeed(setting / 100)).build());
    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder().name("logo-color").description("Color of the logo.").defaultValue(new SettingColor(255, 255, 255)).build());

    public Logo() {
        super(INFO);
        update();
        RAINBOW.setSpeed(chromaSpeed.get() / 100);
    }

    public void update() {
        setSize(78 * scale.get(), 96 * scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        if (!Utils.canUpdate()) return;
        if (chroma.get()) {
            GL.bindTexture(LOGO_FLAT);
        } else {
            GL.bindTexture(LOGO);
        }
        Renderer2D.TEXTURE.begin();
        if (chroma.get()) {
            Renderer2D.TEXTURE.texQuad(x, y, getWidth(), getHeight(), RAINBOW.getNext());
        } else {
            Renderer2D.TEXTURE.texQuad(x, y, getWidth(), getHeight(), color.get());
        }
        Renderer2D.TEXTURE.render(null);
    }
}
