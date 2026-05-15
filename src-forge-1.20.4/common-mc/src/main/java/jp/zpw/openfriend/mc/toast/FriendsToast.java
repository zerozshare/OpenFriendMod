/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.mc.toast;

import jp.zpw.openfriend.common.ui.UTheme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;

public final class FriendsToast implements Toast {

    public static final int WIDTH = 160;
    public static final int HEIGHT = 32;
    private static final long TTL_MS = 5_000L;

    private final Component title;
    private final Component message;

    public FriendsToast(Component title, Component message) {
        this.title = title;
        this.message = message == null ? Component.empty() : message;
    }

    @Override
    public int width() { return WIDTH; }

    @Override
    public int height() { return HEIGHT; }

    @Override
    public Visibility render(GuiGraphics g, ToastComponent host, long elapsedMs) {
        int border = UTheme.ACCENT_BLUE;
        g.fill(0, 0, WIDTH, HEIGHT, UTheme.TOAST_BG);
        g.fill(0, 0, WIDTH, 1, border);
        g.fill(0, HEIGHT - 1, WIDTH, HEIGHT, border);
        g.fill(0, 0, 1, HEIGHT, border);
        g.fill(WIDTH - 1, 0, WIDTH, HEIGHT, border);

        Font font = host.getMinecraft().font;
        g.drawString(font, title,   8, 6,             UTheme.TEXT,        false);
        g.drawString(font, message, 8, 6 + font.lineHeight + 2, UTheme.ACCENT_CYAN, false);

        return elapsedMs >= TTL_MS ? Visibility.HIDE : Visibility.SHOW;
    }
}
