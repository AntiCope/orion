package me.ghosttypes.orion.modules.hud.items;

import me.ghosttypes.orion.utils.misc.ItemCounter;
import me.ghosttypes.orion.utils.player.ItemHelper;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.modules.HudElement;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.item.BedItem;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TextItems extends HudElement {

    public TextItems(HUD hud) {
        super(hud, "item-counter", "Display the amount of selected items in your inventory.", false);
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<SortMode> sortMode = sgGeneral.add(new EnumSetting.Builder<SortMode>().name("sort-mode").description("How to sort the binds list.").defaultValue(SortMode.Shortest).build());
    private final Setting<Boolean> beds = sgGeneral.add(new BoolSetting.Builder().name("beds").description("Show count of all bed types.").defaultValue(false).build());
    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder().name("items").description("Which items to display in the counter list").defaultValue(new ArrayList<>(0)).build());

    private final ArrayList<String> itemCounter = new ArrayList<>();


    @Override
    public void update(HudRenderer renderer) {
        if (!Utils.canUpdate()) return;
        updateCounter();
        double width = 0;
        double height = 0;
        int i = 0;
        if (itemCounter.isEmpty()) {
            String t = "Item Counters";
            width = Math.max(width, renderer.textWidth(t));
            height += renderer.textHeight();
        } else {
            for (String counter : itemCounter) {
                width = Math.max(width, renderer.textWidth(counter));
                height += renderer.textHeight();
                if (i > 0) height += 2;
                i++;
            }
        }
        box.setSize(width, height);
    }

    @Override
    public void render(HudRenderer renderer) {
        if (!Utils.canUpdate()) return;
        updateCounter();
        double x = box.getX();
        double y = box.getY();
        if (isInEditor()) {
            renderer.text("Item Counters", x, y, hud.secondaryColor.get());
            return;
        }
        int i = 0;
        if (itemCounter.isEmpty()) {
            String t = "You have no items selected to count.";
            renderer.text(t, x + box.alignX(renderer.textWidth(t)), y, hud.secondaryColor.get());
        } else {
            for (String counter: itemCounter) {
                renderer.text(counter, x + box.alignX(renderer.textWidth(counter)), y, hud.secondaryColor.get());
                y += renderer.textHeight();
                if (i > 0) y += 2;
                i++;
            }
        }
    }


    private void updateCounter() {
        itemCounter.clear();
        for (Item item: items.get()) if (!(item instanceof BedItem)) itemCounter.add(ItemHelper.getCommonName(item) + ": " + InvUtils.find(item).getCount());
        if (beds.get()) itemCounter.add("Beds: " + ItemCounter.beds());
        if (sortMode.get().equals(SortMode.Shortest)) {
            itemCounter.sort(Comparator.comparing(String::length));
        } else {
            itemCounter.sort(Comparator.comparing(String::length).reversed());
        }
    }

    public enum SortMode {
        Longest,
        Shortest
    }


}
