/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 * Variant for group-b (Minecraft 1.19 - 1.19.4): Toast.render takes PoseStack.
 */
package jp.zpw.openfriend.mc.toast;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public final class FriendsToast implements Toast {

    public static final int WIDTH = 160;
    public static final int HEIGHT = 32;
    private static final long TTL_MS = 5_000L;

    private final Component title;
    private final Component message;

    public FriendsToast(Component title, Component message) {
        this.title = title;
        this.message = message == null ? TextComponent.EMPTY : message;
    }

    @Override
    public int width() { return WIDTH; }

    @Override
    public int height() { return HEIGHT; }

    @Override
    public Visibility render(PoseStack pose, ToastComponent host, long elapsedMs) {
        int bg     = 0xF00A0A0A;
        int border = 0xFF4F8FFF;
        GuiComponent.fill(pose, 0, 0,            WIDTH,     HEIGHT,    bg);
        GuiComponent.fill(pose, 0, 0,            WIDTH,     1,         border);
        GuiComponent.fill(pose, 0, HEIGHT - 1,   WIDTH,     HEIGHT,    border);
        GuiComponent.fill(pose, 0, 0,            1,         HEIGHT,    border);
        GuiComponent.fill(pose, WIDTH - 1, 0,    WIDTH,     HEIGHT,    border);

        Font font = host.getMinecraft().font;
        font.drawShadow(pose, title.getString(),   8f, 6f,                       0xFFFFFFFF);
        font.drawShadow(pose, message.getString(), 8f, 6f + font.lineHeight + 2, 0xFF7BC8FF);

        return elapsedMs >= TTL_MS ? Visibility.HIDE : Visibility.SHOW;
    }
}
