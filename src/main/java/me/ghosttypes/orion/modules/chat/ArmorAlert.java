package me.ghosttypes.orion.modules.chat;

import me.ghosttypes.orion.Orion;
import me.ghosttypes.orion.utils.player.ArmorUtil;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;

public class ArmorAlert extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> threshold = sgGeneral.add(new DoubleSetting.Builder().name("durability").description("How low an armor piece needs to be to alert you.").defaultValue(2).min(1).sliderMin(1).sliderMax(100).max(100).build());

    public ArmorAlert() {
        super(Orion.CATEGORY, "armor-alert", "Alerts you when your armor pieces are low.");
    }

    private boolean alertedHelm;
    private boolean alertedChest;
    private boolean alertedLegs;
    private boolean alertedBoots;

    @Override
    public void onActivate() {
        alertedHelm = false;
        alertedChest = false;
        alertedLegs = false;
        alertedBoots = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        Iterable<ItemStack> armorPieces = mc.player.getArmorItems();
        for (ItemStack armorPiece : armorPieces){
            if (ArmorUtil.checkThreshold(armorPiece, threshold.get())) {
                if (ArmorUtil.isHelm(armorPiece) && !alertedHelm) {
                    warning("Your helmet is low!");
                    alertedHelm = true;
                }
                if (ArmorUtil.isChest(armorPiece) && !alertedChest) {
                    warning("Your chestplate is low!");
                    alertedChest = true;
                }
                if (ArmorUtil.isLegs(armorPiece) && !alertedLegs) {
                    warning("Your leggings are low!");
                    alertedLegs = true;
                }
                if (ArmorUtil.isBoots(armorPiece) && !alertedBoots) {
                    warning("Your boots are low!");
                    alertedBoots = true;
                }
            }
            if (!ArmorUtil.checkThreshold(armorPiece, threshold.get())) {
                if (ArmorUtil.isHelm(armorPiece) && alertedHelm) alertedHelm = false;
                if (ArmorUtil.isChest(armorPiece) && alertedChest) alertedChest = false;
                if (ArmorUtil.isLegs(armorPiece) && alertedLegs) alertedLegs = false;
                if (ArmorUtil.isBoots(armorPiece) && alertedBoots) alertedBoots = false;
            }
        }
    }
}
