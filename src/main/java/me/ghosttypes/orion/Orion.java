package me.ghosttypes.orion;

import com.mojang.logging.LogUtils;
import me.ghosttypes.orion.modules.chat.*;
import me.ghosttypes.orion.modules.hud.Logo;
import me.ghosttypes.orion.modules.hud.OrionHud;
import me.ghosttypes.orion.modules.main.*;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Items;
import org.slf4j.Logger;

public class Orion extends MeteorAddon {
	public static final Logger LOG = LogUtils.getLogger();
	public static final Category CATEGORY = new Category("Orion", Items.OBSIDIAN.getDefaultStack());
    public static final HudGroup HUD_GROUP = new HudGroup("Orion");
	public static final String VERSION = "0.1";

	@Override
	public void onInitialize() {
		LOG.info("Initializing Orion");

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
        Modules.get().add(new PopCounter());
        Modules.get().add(new RPC());
        Modules.get().add(new SelfTrapPlus());
        Modules.get().add(new SurroundPlus());

        OrionHud.starscriptAdd();
        //HUD
        Hud hud = Systems.get(Hud.class);
        hud.register(OrionHud.INFO);
        hud.register(Logo.INFO);
	}

	@Override
	public void onRegisterCategories() {
		Modules.registerCategory(CATEGORY);
	}

    @Override
    public String getPackage() {
        return "me.ghosttypes.orion";
    }

    @Override
    public String getWebsite() {
        return "https://github.com/AntiCope/orion";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("AntiCope", "orion");
    }

    @Override
    public String getCommit() {
        String commit = FabricLoader
            .getInstance()
            .getModContainer("orion")
            .get().getMetadata()
            .getCustomValue("github:sha")
            .getAsString();
        return commit.isEmpty() ? null : commit.trim();
    }
}
