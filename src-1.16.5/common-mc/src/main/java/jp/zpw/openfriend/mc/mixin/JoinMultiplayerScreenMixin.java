/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 * Variant for group-b0 (Minecraft 1.19 - 1.19.3): Tooltip class did not exist yet
 * (added in 1.19.4) — register the button without a tooltip.
 */
package jp.zpw.openfriend.mc.mixin;

import jp.zpw.openfriend.mc.OpenFriendMod;
import jp.zpw.openfriend.mc.ui.MCScreenOpener;
import jp.zpw.openfriend.mc.ui.OpenFriendIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public abstract class JoinMultiplayerScreenMixin extends Screen {
    protected JoinMultiplayerScreenMixin() { super(TextComponent.EMPTY); }

    @Inject(method = "init", at = @At("RETURN"))
    private void openfriend$addFriendsButton(CallbackInfo ci) {
        OpenFriendIconButton btn = new OpenFriendIconButton(this.width - 26, 6, 20, 20,
                b -> {
                    try {
                        MCScreenOpener o = OpenFriendMod.opener();
                        if (o != null) o.openFriendsOverlay();
                    } catch (Throwable t) {
                        OpenFriendMod.LOG.error("Friends button click failed", t);
                    }
                });
        addButton(btn);
    }
}
