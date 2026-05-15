/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 * Variant for group-c1b (Minecraft 1.20.2 - 1.20.4): ConnectScreen.startConnecting
 * takes 5 args (TransferState was added in 1.21.2).
 */
package jp.zpw.openfriend.mc.ui;

import jp.zpw.openfriend.common.ipc.IpcClient;
import jp.zpw.openfriend.common.screen.FriendsController;
import jp.zpw.openfriend.common.screen.FriendsOverlayScreen;
import jp.zpw.openfriend.mc.OpenFriendMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;

public final class MCScreenOpener implements FriendsController.MultiplayBridge, jp.zpw.openfriend.common.notice.NoticeSink {

    private FriendsController controller;

    public void setController(FriendsController c) { this.controller = c; }

    public boolean isReady() { return controller != null; }

    public void openFriendsOverlay() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        if (controller == null) {
            OpenFriendMod.LOG.warn("OpenFriend overlay requested but controller not initialised");
            return;
        }
        try {
            FriendsOverlayScreen overlay = controller.buildOverlay(() -> mc.setScreen(null));
            mc.setScreen(new MCScreenWrapper(overlay));
        } catch (Throwable t) {
            OpenFriendMod.LOG.error("OpenFriend overlay open failed", t);
        }
    }

    @Override
    public boolean canHost() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.getSingleplayerServer() != null;
    }

    @Override
    public boolean publishToFriends(FriendsController.GameMode mode, boolean allowCheats, int maxPlayers) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getSingleplayerServer() == null) {
            OpenFriendMod.LOG.warn("Open-to-Friends: not in a single-player world");
            return false;
        }
        if (mc.getSingleplayerServer().isPublished()) {
            mc.setScreen(null);
            return true;
        }
        GameType gameType;
        switch (mode) {
            case CREATIVE:  gameType = GameType.CREATIVE;  break;
            case ADVENTURE: gameType = GameType.ADVENTURE; break;
            case SPECTATOR: gameType = GameType.SPECTATOR; break;
            case SURVIVAL:
            default:        gameType = GameType.SURVIVAL;  break;
        }
        int port = HttpUtil.getAvailablePort();
        try {
            mc.getSingleplayerServer().getPlayerList().maxPlayers = Math.max(1, maxPlayers);
        } catch (Throwable t) {
            OpenFriendMod.LOG.warn("set maxPlayers failed: {}", t.getMessage());
        }
        mc.setScreen(null);
        boolean ok = mc.getSingleplayerServer().publishServer(gameType, allowCheats, port);
        if (ok) {
            sendChat(Component.literal("[OpenFriend] ")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal("Opened to LAN on port " + port + " (max " + maxPlayers + "). Bridging to your Friends list...")
                            .withStyle(ChatFormatting.WHITE)));
            onServerPublished(port);
        } else {
            sendChat(Component.literal("[OpenFriend] Failed to open to LAN (port " + port + ")")
                    .withStyle(ChatFormatting.RED));
        }
        return ok;
    }

    private volatile int lastBridgedPort = -1;



    public void onServerPublished(int port) {


        if (controller == null || controller.ipc() == null || !controller.ipc().isRunning()) return;


        if (lastBridgedPort == port) return;


        lastBridgedPort = port;
        String target = "127.0.0.1:" + port;
        OpenFriendMod.LOG.info("OpenFriend: bridging to LAN port {}", port);
        controller.ipc().requestAsync("host.start",
                IpcClient.params("target", target, "useBypass", false))
                .whenComplete((res, err) -> {
                    if (err != null) {
                        OpenFriendToastOverlay.push(
                                jp.zpw.openfriend.common.notice.NoticeSink.Level.ERROR,
                                "Bridge failed",
                                err.getMessage() == null ? "Unknown error" : err.getMessage());
                    } else {
                        OpenFriendToastOverlay.push(
                                jp.zpw.openfriend.common.notice.NoticeSink.Level.SUCCESS,
                                "World shared",
                                "Friends can join from their list.");
                    }
                });
    }

    private static void sendChat(Component msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gui == null) return;
        mc.gui.getChat().addMessage(msg);
    }

    @Override
    public void show(jp.zpw.openfriend.common.notice.NoticeSink.Level level, String title, String body) {
        OpenFriendToastOverlay.push(level, title, body);
    }

    public void connectToLocalAddress(String hostPort) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || hostPort == null || hostPort.isEmpty()) return;
        mc.execute(() -> {
            try {
                mc.setScreen(null);
                ServerAddress addr = ServerAddress.parseString(hostPort);
                ServerData data = new ServerData("OpenFriend join", hostPort, ServerData.Type.OTHER);
                ConnectScreen.startConnecting(null, mc, addr, data, false);
            } catch (Throwable t) {
                OpenFriendMod.LOG.error("OpenFriend connect-to-local failed", t);
            }
        });
    }
}
