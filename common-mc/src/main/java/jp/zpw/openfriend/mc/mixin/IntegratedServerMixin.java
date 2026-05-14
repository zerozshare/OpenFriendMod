/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.mc.mixin;

import jp.zpw.openfriend.mc.OpenFriendMod;
import jp.zpw.openfriend.mc.ui.MCScreenOpener;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {

    @Inject(method = "publishServer", at = @At("RETURN"))
    private void openfriend$onPublish(GameType gameType, boolean cheats, int port,
                                      CallbackInfoReturnable<Boolean> cir) {
        try {
            if (!Boolean.TRUE.equals(cir.getReturnValue())) return;
            IntegratedServer self = (IntegratedServer) (Object) this;
            int actualPort = self.getPort();
            MCScreenOpener opener = OpenFriendMod.opener();
            if (opener != null) opener.onServerPublished(actualPort);
        } catch (Throwable t) {
            OpenFriendMod.LOG.error("OpenFriend publish hook failed", t);
        }
    }
}
