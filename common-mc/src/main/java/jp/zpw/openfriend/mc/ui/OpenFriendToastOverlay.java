/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 * Variant for group-b (Minecraft 1.19 - 1.19.4): PoseStack-based rendering.
 */
package jp.zpw.openfriend.mc.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import jp.zpw.openfriend.common.notice.NoticeSink;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class OpenFriendToastOverlay {

    public static final int MAX_VISIBLE = 5;
    public static final int WIDTH       = 160;
    public static final int HEIGHT      = 32;
    public static final int GAP         = 2;
    public static final int MARGIN      = 8;
    public static final int CLOSE_W     = 10;
    public static final long TTL_MS     = 5_000L;
    public static final long SLIDE_IN_MS = 220L;

    public static final int DETAIL_W   = 240;
    public static final int DETAIL_PAD = 12;

    private static final Object LOCK = new Object();
    private static final List<Entry> entries = new ArrayList<>();
    private static volatile Entry activeDetail = null;

    private OpenFriendToastOverlay() {}

    public static void push(NoticeSink.Level level, String title, String body) {
        Entry e = new Entry(level, title == null ? "" : title, body == null ? "" : body, System.currentTimeMillis());
        synchronized (LOCK) {
            entries.add(e);
        }
    }

    public static void render(PoseStack pose, int screenWidth) {
        long now = System.currentTimeMillis();
        List<Entry> snapshot;
        synchronized (LOCK) {
            if (activeDetail == null) {
                for (Iterator<Entry> it = entries.iterator(); it.hasNext();) {
                    if (now - it.next().startMs > TTL_MS) it.remove();
                }
            }
            int n = Math.min(entries.size(), MAX_VISIBLE);
            snapshot = new ArrayList<>(n);
            for (int i = 0; i < n; i++) snapshot.add(entries.get(i));
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        Font font = mc.font;
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int mouseX = (int) (mc.mouseHandler.xpos() * screenWidth / Math.max(1, mc.getWindow().getScreenWidth()));
        int mouseY = (int) (mc.mouseHandler.ypos() * screenHeight / Math.max(1, mc.getWindow().getScreenHeight()));

        for (int i = 0; i < snapshot.size(); i++) {
            Entry e = snapshot.get(i);
            float slide = Math.min(1f, (now - e.startMs) / (float) SLIDE_IN_MS);
            slide = easeOut(slide);
            int slideOffset = (int) ((1f - slide) * (WIDTH + MARGIN));
            int x = screenWidth - WIDTH - MARGIN + slideOffset;
            int y = MARGIN + i * (HEIGHT + GAP);
            renderToast(pose, font, x, y, e, mouseX, mouseY);
        }

        Entry detail = activeDetail;
        if (detail != null) {
            renderDetail(pose, font, screenWidth, screenHeight, detail);
        }
    }

    public static boolean handleClick(int mouseX, int mouseY, int screenWidth) {
        if (activeDetail != null) {
            activeDetail = null;
            return true;
        }
        synchronized (LOCK) {
            int n = Math.min(entries.size(), MAX_VISIBLE);
            for (int i = 0; i < n; i++) {
                Entry e = entries.get(i);
                int x = screenWidth - WIDTH - MARGIN;
                int y = MARGIN + i * (HEIGHT + GAP);
                int closeX = x + WIDTH - CLOSE_W - 4;
                int closeY = y + 4;
                if (mouseX >= closeX && mouseX < closeX + CLOSE_W &&
                    mouseY >= closeY && mouseY < closeY + CLOSE_W) {
                    entries.remove(i);
                    return true;
                }
                if (mouseX >= x && mouseX < x + WIDTH && mouseY >= y && mouseY < y + HEIGHT) {
                    activeDetail = e;
                    return true;
                }
            }
        }
        return false;
    }

    private static float easeOut(float t) {
        return 1f - (1f - t) * (1f - t);
    }

    private static void renderToast(PoseStack pose, Font font, int x, int y, Entry e, int mouseX, int mouseY) {
        int bg     = 0xF00A0A0A;
        int border = borderFor(e.level);
        GuiComponent.fill(pose, x, y, x + WIDTH, y + HEIGHT, bg);
        GuiComponent.fill(pose, x, y, x + WIDTH, y + 1, border);
        GuiComponent.fill(pose, x, y + HEIGHT - 1, x + WIDTH, y + HEIGHT, border);
        GuiComponent.fill(pose, x, y, x + 1, y + HEIGHT, border);
        GuiComponent.fill(pose, x + WIDTH - 1, y, x + WIDTH, y + HEIGHT, border);

        int textPadL = 8;
        int textPadR = CLOSE_W + 8;
        int textMaxW = WIDTH - textPadL - textPadR;
        int titleColor = titleColorFor(e.level);
        String title = ellipsize(font, e.title, textMaxW);
        String body  = ellipsize(font, e.body,  textMaxW);
        font.draw(pose, title, (float)(x + textPadL), (float)(y + 6),                       titleColor);
        font.draw(pose, body,  (float)(x + textPadL), (float)(y + 6 + font.lineHeight + 2), 0xFFCCCCCC);

        int closeX = x + WIDTH - CLOSE_W - 4;
        int closeY = y + 4;
        boolean hover = mouseX >= closeX && mouseX < closeX + CLOSE_W &&
                        mouseY >= closeY && mouseY < closeY + CLOSE_W;
        int xColor = hover ? 0xFFFFFFFF : 0xFFAAAAAA;
        font.draw(pose, "x", (float)(closeX + 2), (float)(closeY + 1), xColor);
    }

    private static void renderDetail(PoseStack pose, Font font, int sw, int sh, Entry e) {
        GuiComponent.fill(pose, 0, 0, sw, sh, 0xC0000000);

        int innerW = DETAIL_W - DETAIL_PAD * 2;
        List<net.minecraft.util.FormattedCharSequence> bodyLines  = font.split(net.minecraft.network.chat.FormattedText.of(e.body),  innerW);
        List<net.minecraft.util.FormattedCharSequence> titleLines = font.split(net.minecraft.network.chat.FormattedText.of(e.title), innerW);
        int titleH = titleLines.size() * font.lineHeight;
        int bodyH  = bodyLines.size()  * font.lineHeight;
        int hintH  = font.lineHeight;
        int contentH = titleH + 8 + bodyH + 12 + hintH;
        int panelH = contentH + DETAIL_PAD * 2;

        int panelX = (sw - DETAIL_W) / 2;
        int panelY = (sh - panelH) / 2;

        int border = borderFor(e.level);
        GuiComponent.fill(pose, panelX, panelY, panelX + DETAIL_W, panelY + panelH, 0xF00A0A0A);
        GuiComponent.fill(pose, panelX, panelY, panelX + DETAIL_W, panelY + 1, border);
        GuiComponent.fill(pose, panelX, panelY + panelH - 1, panelX + DETAIL_W, panelY + panelH, border);
        GuiComponent.fill(pose, panelX, panelY, panelX + 1, panelY + panelH, border);
        GuiComponent.fill(pose, panelX + DETAIL_W - 1, panelY, panelX + DETAIL_W, panelY + panelH, border);

        int textX = panelX + DETAIL_PAD;
        int cy = panelY + DETAIL_PAD;
        int titleColor = titleColorFor(e.level);
        for (net.minecraft.util.FormattedCharSequence line : titleLines) {
            font.draw(pose, line, (float) textX, (float) cy, titleColor);
            cy += font.lineHeight;
        }
        cy += 8;
        for (net.minecraft.util.FormattedCharSequence line : bodyLines) {
            font.draw(pose, line, (float) textX, (float) cy, 0xFFFFFFFF);
            cy += font.lineHeight;
        }
        cy += 12;
        String hint = "Click anywhere to dismiss";
        int hintW = font.width(hint);
        font.draw(pose, hint, (float)(textX + (innerW - hintW) / 2), (float) cy, 0xFF777777);
    }

    private static String ellipsize(Font font, String s, int maxWidth) {
        if (s == null) return "";
        if (font.width(s) <= maxWidth) return s;
        String ell = "...";
        int ellW = font.width(ell);
        int budget = Math.max(0, maxWidth - ellW);
        StringBuilder b = new StringBuilder();
        int acc = 0;
        for (int i = 0; i < s.length(); i++) {
            int cw = font.width(String.valueOf(s.charAt(i)));
            if (acc + cw > budget) break;
            b.append(s.charAt(i));
            acc += cw;
        }
        return b + ell;
    }

    private static int borderFor(NoticeSink.Level level) {
        switch (level) {
            case ERROR:   return 0xFFE05656;
            case WARN:    return 0xFFF5B35A;
            case SUCCESS: return 0xFF6CD27A;
            default:      return 0xFFFFCC2E;
        }
    }

    private static int titleColorFor(NoticeSink.Level level) {
        switch (level) {
            case ERROR:   return 0xFFE05656;
            case WARN:    return 0xFFF5B35A;
            case SUCCESS: return 0xFF6CD27A;
            default:      return 0xFFFFFFFF;
        }
    }

    private static final class Entry {
        final NoticeSink.Level level;
        final String title;
        final String body;
        final long startMs;
        Entry(NoticeSink.Level level, String title, String body, long startMs) {
            this.level = level == null ? NoticeSink.Level.INFO : level;
            this.title = title;
            this.body  = body;
            this.startMs = startMs;
        }
    }
}
