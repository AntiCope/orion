package me.ghosttypes.orion.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;

import me.ghosttypes.orion.utils.UpdateUtil;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;

@Mixin(value = TitleScreen.class)
public class TitleScreenMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {
        MeteorExecutor.execute(() -> {
            UpdateUtil.checkUpdate();
        });
    }
}
