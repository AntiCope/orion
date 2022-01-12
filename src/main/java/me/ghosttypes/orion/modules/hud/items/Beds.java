package me.ghosttypes.orion.modules.hud.items;

import me.ghosttypes.orion.utils.misc.ItemCounter;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.modules.HudElement;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import net.minecraft.item.BedItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class Beds extends HudElement {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder().name("scale").description("Scale of the counter.").defaultValue(3).min(1).sliderMin(1).sliderMax(4).build());

    public Beds(HUD hud) {
        super(hud, "beds", "Displays the amount of beds in your inventory.", false);
    }

    @Override
    public void update(HudRenderer renderer) {
        box.setSize(16 * scale.get(), 16 * scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        if (!Utils.canUpdate()) return;
        double x = box.getX();
        double y = box.getY();

        if (isInEditor()) {
            RenderUtils.drawItem(Items.RED_BED.getDefaultStack(), (int) x, (int) y, scale.get(), true);
        } else if (InvUtils.find(itemStack -> itemStack.getItem() instanceof BedItem).count() > 0) {
            RenderUtils.drawItem(new ItemStack(Items.RED_BED, ItemCounter.beds()), (int) x, (int) y, scale.get(), true);
        }
    }
}
