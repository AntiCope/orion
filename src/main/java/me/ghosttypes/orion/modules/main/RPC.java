package me.ghosttypes.orion.modules.main;

import me.ghosttypes.orion.Orion;
import me.ghosttypes.orion.utils.misc.Placeholders;
import me.ghosttypes.orion.utils.misc.Stats;
import meteordevelopment.DiscordIPC;
import meteordevelopment.RichPresence;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.DiscordPresence;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;

import java.util.Collections;
import java.util.List;

public class RPC extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> title = sgGeneral.add(new StringSetting.Builder().name("title").description("What to display as the RPC's title.").defaultValue("Orion {oversion}").onChanged(booleanSetting -> alertTitle()).build());
    private final Setting<List<String>> messages = sgGeneral.add(new StringListSetting.Builder().name("line-1").description("Messages for the first RPC line.").defaultValue(Collections.emptyList()).build());
    private final Setting<List<String>> messages2 = sgGeneral.add(new StringListSetting.Builder().name("line-2").description("Messages for the second RPC line.").defaultValue(Collections.emptyList()).build());
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder().name("update-delay").description("How many seconds before switching to a new RPC message.").defaultValue(5).min(0).sliderMax(30).build());

    public static final RichPresence rpc = new RichPresence();

    public RPC() {
        super(Orion.CATEGORY, "RPC", "Orion RPC for Discord!");
    }
    private int alertDelay = 0;
    private int updateDelay;
    private int messageI, messageI2;

    @Override
    public void onActivate() {
        if (Modules.get().isActive(DiscordPresence.class)) {
            error( "Default Meteor RPC is already enabled! Overriding.");
            Modules.get().get(DiscordPresence.class).toggle();
        }
        if (messages.get().isEmpty() || messages2.get().isEmpty()) {
            error( "Your RPC messages are empty!");
            this.toggle();
            return;
        }
        updateDelay = delay.get() * 20;
        DiscordIPC.start(880625940336627743L, null);
        rpc.setStart(Stats.rpcStart);
        rpc.setLargeImage("orion", Placeholders.apply(title.get()));
        updateDetails();
        messageI = 0;
        messageI2 = 0;
    }

    @Override
    public void onDeactivate() {
        DiscordIPC.stop();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!Utils.canUpdate()) return;
        updateDelay--;
        alertDelay--;
        if (updateDelay <= 1) {
            updateDetails();
            updateDelay = delay.get() * 20;
        }
    }

    private void updateDetails() {
        if (isActive() && Utils.canUpdate()) {
            if (messageI >= messages.get().size()) messageI = 0;
            if (messageI2 >= messages2.get().size()) messageI2 = 0;
            int i = messageI++;
            int i2 = messageI2++;
            rpc.setDetails(Placeholders.apply(messages.get().get(i)));
            rpc.setState(Placeholders.apply(messages2.get().get(i2)));
            DiscordIPC.setActivity(rpc);
        }
    }

    private void alertTitle() {
        if (alertDelay <= 0) {
            warning("Restart RPC to apply your new title.");
            alertDelay = 200;
        }
    }
}
