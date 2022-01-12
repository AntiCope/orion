package me.ghosttypes.orion.utils.misc;

import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.item.BedItem;
import net.minecraft.item.Items;

public class ItemCounter {

    public static int crystals() {
        return InvUtils.find(Items.END_CRYSTAL).count();
    }

    public static int egaps() {
        return InvUtils.find(Items.ENCHANTED_GOLDEN_APPLE).count();
    }

    public static int xp() {
        return InvUtils.find(Items.EXPERIENCE_BOTTLE).count();
    }

    public static int totem() {
        return InvUtils.find(Items.TOTEM_OF_UNDYING).count();
    }

    public static int beds() {
        return InvUtils.find(itemStack -> itemStack.getItem() instanceof BedItem).count();
    }

}
