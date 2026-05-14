/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.mc.toast;

import com.google.gson.JsonObject;
import jp.zpw.openfriend.common.ipc.IpcListener;
import jp.zpw.openfriend.mc.OpenFriendMod;
import jp.zpw.openfriend.mc.ui.SignInScreen;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public final class ToastDispatcher implements IpcListener {

    @Override
    public void onNotification(String method, JsonObject params) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        String name = params.has("name") && !params.get("name").isJsonNull()
                ? params.get("name").getAsString()
                : "";
        switch (method) {
            case "auth.deviceCode": {
                String code = params.has("userCode") ? params.get("userCode").getAsString() : "";
                String uri  = params.has("verificationUri") ? params.get("verificationUri").getAsString() : "";
                OpenFriendMod.LOG.info("OpenFriend sign-in: visit {} and enter {}", uri, code);
                mc.execute(() -> {
                    SignInScreen scr = new SignInScreen(uri, code);
                    SignInScreen.setCurrent(scr);
                    mc.setScreen(scr);
                    if (!uri.isEmpty()) {
                        try { Util.getPlatform().openUri(uri); } catch (Throwable ignored) {}
                    }
                });
                break;
            }
            case "auth.signedIn": {
                final String displayName = name;
                mc.execute(() -> {
                    SignInScreen cur = SignInScreen.current();
                    if (cur != null) cur.markSignedIn(displayName);
                    ToastComponent toasts = mc.getToasts();
                    if (toasts != null) {
                        toasts.addToast(new FriendsToast(
                                new TextComponent("Signed in"),
                                new TextComponent(displayName)));
                    }
                });
                break;
            }
            case "friend.requestIncoming":
            case "friend.added":
            case "friend.joined": {
                ToastComponent toasts = mc.getToasts();
                if (toasts == null) return;
                Component title;
                Component body;
                if (method.equals("friend.requestIncoming")) {
                    if (name.isEmpty()) return;
                    title = new TextComponent("Friend request");
                    body  = new TextComponent(name);
                } else if (method.equals("friend.added")) {
                    if (name.isEmpty()) return;
                    title = new TextComponent("Friend added");
                    body  = new TextComponent(name);
                } else {
                    String peer = params.has("pmid") ? params.get("pmid").getAsString().substring(0, 8) : "Someone";
                    title = new TextComponent("Friend joined");
                    body  = new TextComponent(peer + "…");
                }
                toasts.addToast(new FriendsToast(title, body));
                break;
            }
            default:
        }
    }
}
