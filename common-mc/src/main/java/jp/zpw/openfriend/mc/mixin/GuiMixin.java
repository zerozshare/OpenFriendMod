/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 * Variant for group-b (Minecraft 1.19 - 1.19.4): ToastComponent.render takes PoseStack.
 */
package jp.zpw.openfriend.mc.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import jp.zpw.openfriend.mc.OpenFriendMod;
import jp.zpw.openfriend.mc.ui.OpenFriendToastOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastComponent.class)
public abstract class GuiMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void openfriend$renderOurToasts(PoseStack pose, CallbackInfo ci) {
        try {
            Minecraft mc = Minecraft.getInstance();
            int sw = mc == null ? 320 : mc.getWindow().getGuiScaledWidth();
            OpenFriendToastOverlay.render(pose, sw);
        } catch (Throwable t) {
            OpenFriendMod.LOG.error("OpenFriend toast overlay render failed", t);
        }
    }
}
