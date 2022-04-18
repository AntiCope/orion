package me.ghosttypes.orion.utils.player;

import me.ghosttypes.orion.modules.chat.PopCounter;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerHelper {

    public static int getPops(PlayerEntity p) {
        PopCounter popCounter = Modules.get().get(PopCounter.class);
        if (popCounter == null) return 0;
        if (!popCounter.isActive()) return 0;
        if (!popCounter.totemPops.containsKey(p.getUuid())) return 0;
        return popCounter.totemPops.getOrDefault(p.getUuid(), 0);
    }

}
