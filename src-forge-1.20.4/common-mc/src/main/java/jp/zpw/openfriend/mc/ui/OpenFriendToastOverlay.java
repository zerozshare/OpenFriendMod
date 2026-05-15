/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.mc.ui;

import jp.zpw.openfriend.common.notice.NoticeSink;
import jp.zpw.openfriend.common.ui.UTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

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
    private static volatile boolean firstPushLogged = false;
    private static volatile boolean firstRenderLogged = false;
    private static volatile long renderCalls = 0;

    private OpenFriendToastOverlay() {}

    public static void push(NoticeSink.Level level, String title, String body) {
        Entry e = new Entry(level, title == null ? "" : title, body == null ? "" : body, System.currentTimeMillis());
        synchronized (LOCK) {
            entries.add(e);
        }
        if (!firstPushLogged) {
            firstPushLogged = true;
            org.slf4j.LoggerFactory.getLogger("openfriend").info("OpenFriendToastOverlay.push fired: level={} title={} (entries={}, renderCalls={})",
                level, title, entries.size(), renderCalls);
        }
    }

    public static int entryCount() {
        synchronized (LOCK) { return entries.size(); }
    }

    public static long renderCallCount() {
        return renderCalls;
    }

    public static void render(GuiGraphics g, int screenWidth) {
        renderCalls++;
        if (!firstRenderLogged) {
            firstRenderLogged = true;
            org.slf4j.LoggerFactory.getLogger("openfriend").info("OpenFriendToastOverlay.render first call: screenWidth={} entries={}",
                screenWidth, entries.size());
        }
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
            renderToast(g, font, x, y, e, mouseX, mouseY);
        }

        Entry detail = activeDetail;
        if (detail != null) {
            renderDetail(g, font, screenWidth, screenHeight, detail);
        }
    }

    public static boolean isDetailOpen() {
        return activeDetail != null;
    }

    public static boolean dismissDetail() {
        if (activeDetail == null) return false;
        activeDetail = null;
        return true;
    }

    public static boolean handleKey(int keyCode) {
        if (keyCode == 256 && activeDetail != null) {
            activeDetail = null;
            return true;
        }
        return false;
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

    private static void drawBorderedBox(GuiGraphics g, int x, int y, int w, int h, int bg, int border) {
        g.fill(x, y, x + w, y + h, bg);
        g.fill(x, y, x + w, y + 1, border);
        g.fill(x, y + h - 1, x + w, y + h, border);
        g.fill(x, y, x + 1, y + h, border);
        g.fill(x + w - 1, y, x + w, y + h, border);
    }

    private static void renderToast(GuiGraphics g, Font font, int x, int y, Entry e, int mouseX, int mouseY) {
        int border = UTheme.noticeBorder(e.level);
        drawBorderedBox(g, x, y, WIDTH, HEIGHT, UTheme.TOAST_BG, border);

        int textPadL = 8;
        int textPadR = CLOSE_W + 8;
        int textMaxW = WIDTH - textPadL - textPadR;
        String title = ellipsize(font, e.title, textMaxW);
        String body  = ellipsize(font, e.body,  textMaxW);
        g.drawString(font, Component.literal(title), x + textPadL, y + 6,                       UTheme.noticeTitle(e.level), false);
        g.drawString(font, Component.literal(body),  x + textPadL, y + 6 + font.lineHeight + 2, UTheme.TOAST_BODY,           false);

        int closeX = x + WIDTH - CLOSE_W - 4;
        int closeY = y + 4;
        boolean hover = mouseX >= closeX && mouseX < closeX + CLOSE_W &&
                        mouseY >= closeY && mouseY < closeY + CLOSE_W;
        g.drawString(font, "x", closeX + 2, closeY + 1, hover ? UTheme.TEXT : UTheme.TOAST_CLOSE, false);
    }

    private static void renderDetail(GuiGraphics g, Font font, int sw, int sh, Entry e) {
        g.fill(0, 0, sw, sh, UTheme.DIM_OVERLAY);

        int innerW = DETAIL_W - DETAIL_PAD * 2;
        List<FormattedCharSequence> bodyLines  = font.split(FormattedText.of(e.body),  innerW);
        List<FormattedCharSequence> titleLines = font.split(FormattedText.of(e.title), innerW);
        int titleH = titleLines.size() * font.lineHeight;
        int bodyH  = bodyLines.size()  * font.lineHeight;
        int hintH  = font.lineHeight;
        int contentH = titleH + 8 + bodyH + 12 + hintH;
        int panelH   = contentH + DETAIL_PAD * 2;

        int panelX = (sw - DETAIL_W) / 2;
        int panelY = (sh - panelH) / 2;

        drawBorderedBox(g, panelX, panelY, DETAIL_W, panelH, UTheme.TOAST_BG, UTheme.noticeBorder(e.level));

        int textX = panelX + DETAIL_PAD;
        int cy = panelY + DETAIL_PAD;
        int titleColor = UTheme.noticeTitle(e.level);
        for (FormattedCharSequence line : titleLines) {
            g.drawString(font, line, textX, cy, titleColor, false);
            cy += font.lineHeight;
        }
        cy += 8;
        for (FormattedCharSequence line : bodyLines) {
            g.drawString(font, line, textX, cy, UTheme.TEXT, false);
            cy += font.lineHeight;
        }
        cy += 12;
        String hint = "Click anywhere to dismiss";
        int hintW = font.width(hint);
        g.drawString(font, Component.literal(hint), textX + (innerW - hintW) / 2, cy, UTheme.TEXT_DIM, false);
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
