package me.ghosttypes.orion;


import me.ghosttypes.orion.modules.chat.*;
import me.ghosttypes.orion.modules.hud.items.*;
import me.ghosttypes.orion.modules.hud.misc.Welcome;
import me.ghosttypes.orion.modules.hud.stats.*;
import me.ghosttypes.orion.modules.hud.visual.*;
import me.ghosttypes.orion.modules.main.*;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.hud.HUD;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.item.Items;

import java.lang.invoke.MethodHandles;


public class Orion extends MeteorAddon {
	public static final Logger LOG = LoggerFactory.getLogger("Orion");
	public static final Category CATEGORY = new Category("Orion", Items.OBSIDIAN.getDefaultStack());
	public static final String VERSION = "0.1";

	@Override
	public void onInitialize() {
		LOG.info("Initializing Orion");

		MeteorClient.EVENT_BUS.registerLambdaFactory("me.ghosttypes.orion", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

		//Modules
        Modules.get().add(new AutoBedCraft());
        Modules.get().add(new AutoCityPlus());
        Modules.get().add(new AutoLogin());
        Modules.get().add(new AutoXP());
        Modules.get().add(new AutoRespawn());
        Modules.get().add(new AnchorAura());
        Modules.get().add(new ArmorAlert());
        Modules.get().add(new BedAura());
        Modules.get().add(new BedDisabler());
        Modules.get().add(new BurrowAlert());
        Modules.get().add(new BurrowBreaker());
        Modules.get().add(new ChatTweaks());
        Modules.get().add(new NametagsPlus());
        Modules.get().add(new PopCounter());
        Modules.get().add(new RPC());
        Modules.get().add(new SelfTrapPlus());
        Modules.get().add(new SurroundPlus());

        //HUD
        HUD hud = Systems.get(HUD.class);
        //Item Counters
        hud.elements.add(new Beds(hud));
        hud.elements.add(new Crystals(hud));
        hud.elements.add(new Gaps(hud));
        hud.elements.add(new TextItems(hud));
        hud.elements.add(new XP(hud));
        //Stats
        hud.elements.add(new Deaths(hud));
        hud.elements.add(new Highscore(hud));
        hud.elements.add(new KDRatio(hud));
        hud.elements.add(new Killstreak(hud));
        hud.elements.add(new Kills(hud));
        //Visual
        hud.elements.add(new Logo(hud));
        hud.elements.add(new VisualBinds(hud));
        hud.elements.add(new Watermark(hud));
        hud.elements.add(new Welcome(hud));
	}

	@Override
	public void onRegisterCategories() {
		Modules.registerCategory(CATEGORY);
	}
}
