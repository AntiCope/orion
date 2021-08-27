package me.ghosttypes.orion.mixins;

import me.ghosttypes.orion.modules.chat.ChatTweaks;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.RainbowColor;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatUtils.class)
public class ChatUtilsMixin {
    private static final RainbowColor prefixChroma = new RainbowColor();
    @Inject(method = "getMeteorPrefix", at = @At("HEAD"), cancellable = true, remap = false)
    private static void getGhostwarePrefix(CallbackInfoReturnable<BaseText> cir) {
        ChatTweaks chatTweaks = Modules.get().get(ChatTweaks.class);
        BaseText logo = new LiteralText("");
        BaseText prefix = new LiteralText("");

        if (chatTweaks.isActive()) {
            String logoT = "Orion";
            if (chatTweaks.customPrefix.get()) logoT = chatTweaks.prefixText.get();
            if (chatTweaks.customPrefixColor.get() && !chatTweaks.chromaPrefix.get()) logo.append(new LiteralText(logoT).setStyle(logo.getStyle().withColor(TextColor.fromRgb(chatTweaks.prefixColor.get().getPacked()))));
            if (chatTweaks.chromaPrefix.get() && !chatTweaks.customPrefixColor.get()) {
                prefixChroma.setSpeed(chatTweaks.chromaSpeed.get() / 100);
                for(int i = 0, n = logoT.length() ; i < n ; i++) logo.append(new LiteralText(String.valueOf(logoT.charAt(i)))).setStyle(logo.getStyle().withColor(TextColor.fromRgb(prefixChroma.getNext().getPacked())));
            }
            if (!chatTweaks.customPrefixColor.get() && !chatTweaks.chromaPrefix.get()) {
                if (chatTweaks.customPrefix.get()) { logo.append(chatTweaks.prefixText.get());
                } else { logo.append("Orion"); }
                logo.setStyle(logo.getStyle().withFormatting(Formatting.RED));
            }
        } else {
            logo.append("Orion");
            logo.setStyle(logo.getStyle().withFormatting(Formatting.RED));
        }
        if (chatTweaks.themeBrackets.get()) {
            if (chatTweaks.customPrefixColor.get() && !chatTweaks.chromaPrefix.get()) prefix.setStyle(prefix.getStyle().withColor(TextColor.fromRgb(chatTweaks.prefixColor.get().getPacked())));
            if (chatTweaks.chromaPrefix.get() && !chatTweaks.customPrefixColor.get()) {
                prefixChroma.setSpeed(chatTweaks.chromaSpeed.get() / 100);
                prefix.setStyle(prefix.getStyle().withColor(TextColor.fromRgb(prefixChroma.getNext().getPacked())));
            }
            if (chatTweaks.customBrackets.get()) {
                prefix.append(chatTweaks.leftBracket.get());
                prefix.append(logo);
                prefix.append(chatTweaks.rightBracket.get() + " ");
            } else {
                prefix.append("[");
                prefix.append(logo);
                prefix.append("] ");
            }
        } else {
            prefix.setStyle(prefix.getStyle().withFormatting(Formatting.GRAY));
            prefix.append("[");
            prefix.append(logo);
            prefix.append("] ");
        }
        cir.setReturnValue(prefix);
    }
}
