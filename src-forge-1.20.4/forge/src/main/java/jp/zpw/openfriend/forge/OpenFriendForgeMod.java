/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.forge;

import jp.zpw.openfriend.mc.OpenFriendMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod("openfriend")
public final class OpenFriendForgeMod {
    public OpenFriendForgeMod() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            OpenFriendMod.bootstrap();
            MinecraftForge.EVENT_BUS.register(new ForgeScreenHandler());
        }
    }
}
