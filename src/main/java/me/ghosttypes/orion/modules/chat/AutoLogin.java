package me.ghosttypes.orion.modules.chat;

import me.ghosttypes.orion.Orion;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;

import java.util.ArrayList;

public class AutoLogin extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> password = sgGeneral.add(new StringSetting.Builder().name("password").description("The password to log in with.").defaultValue("password").build());

    private final ArrayList<String> loginMessages = new ArrayList<String>() {{
        add("/login ");
        add("/login <password>");
    }};

    public AutoLogin() {
        super(Orion.CATEGORY, "auto-login", "Automatically log into servers that use /login.");
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMessageRecieve(ReceiveMessageEvent event) {
        if (mc.world == null || mc.player == null) return;
        String msg = event.getMessage().getString();
        if (msg.startsWith(">")) return; //ignore chat messages
        for (String loginMsg: loginMessages) {
            if (msg.contains(loginMsg)) {
                mc.player.sendCommand("login " + password.get());
                break;
            }
        }
    }

}
