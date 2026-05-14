/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.fabric;

import jp.zpw.openfriend.mc.OpenFriendMod;
import net.fabricmc.api.ClientModInitializer;

public final class OpenFriendFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        OpenFriendMod.bootstrap();
    }
}
