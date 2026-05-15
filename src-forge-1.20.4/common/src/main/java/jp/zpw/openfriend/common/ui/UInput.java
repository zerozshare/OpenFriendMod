/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.ui;

import java.util.function.Consumer;

public class UInput extends UComponent {

    public static final int KEY_BACKSPACE = 259;
    public static final int KEY_DELETE    = 261;
    public static final int KEY_LEFT      = 263;
    public static final int KEY_RIGHT     = 262;
    public static final int KEY_HOME      = 268;
    public static final int KEY_END       = 269;
    public static final int KEY_ENTER     = 257;
    public static final int KEY_ESCAPE    = 256;

    private StringBuilder text = new StringBuilder();
    private int cursor;
    private int maxLength = 16;
    private String placeholder = "";
    private boolean digitsOnly = false;

    private Consumer<String> onChange;
    private Consumer<String> onSubmit;

    private long blinkAnchorMs = System.currentTimeMillis();

    public UInput setText(String s) {
        text.setLength(0);
        if (s != null) text.append(s);
        cursor = text.length();
        return this;
    }

    public UInput setMaxLength(int n)              { this.maxLength = Math.max(1, n); return this; }
    public UInput setPlaceholder(String p)         { this.placeholder = p == null ? "" : p; return this; }
    public UInput setDigitsOnly(boolean v)         { this.digitsOnly = v; return this; }
    public UInput setOnChange(Consumer<String> cb) { this.onChange = cb; return this; }
    public UInput setOnSubmit(Consumer<String> cb) { this.onSubmit = cb; return this; }

    public String getText()       { return text.toString(); }
    public boolean isEmpty()      { return text.length() == 0; }
    public int getMaxLength()     { return maxLength; }

    public void focus() {
        focused = true;
        blinkAnchorMs = System.currentTimeMillis();
    }

    public void blur() {
        focused = false;
    }

    @Override
    public void render(URenderer r) {
        if (!visible) return;

        int bg     = focused ? UTheme.SURFACE_ALT : UTheme.SURFACE;
        int border = focused ? UTheme.ACCENT_DIM : (hovered ? UTheme.BORDER_HOV : UTheme.BORDER);

        r.fillRect(x, y, width, height, bg);
        r.strokeRect(x, y, width, height, border);

        int paddingX = 6;
        int textY = y + (height - r.textHeight()) / 2;
        String shown = text.toString();
        boolean showPlaceholder = !focused && shown.isEmpty();
        if (showPlaceholder) {
            r.pushClip(x + paddingX, y, width - paddingX * 2, height);
            try {
                r.drawText(x + paddingX, textY, placeholder, UTheme.TEXT_FAINT);
            } finally {
                r.popClip();
            }
            return;
        }

        r.pushClip(x + paddingX, y, width - paddingX * 2, height);
        try {
            r.drawText(x + paddingX, textY, shown, UTheme.TEXT);
            if (focused && cursorVisible()) {
                int cx = x + paddingX + r.textWidth(shown.substring(0, cursor));
                r.fillRect(cx, textY - 1, 1, r.textHeight() + 2, UTheme.TEXT);
            }
        } finally {
            r.popClip();
        }
    }

    private boolean cursorVisible() {
        long elapsed = System.currentTimeMillis() - blinkAnchorMs;
        return (elapsed / 500L) % 2L == 0L;
    }

    @Override
    public boolean mouseClick(double mouseX, double mouseY, int button) {
        if (!visible || !enabled) return false;
        boolean inside = containsPoint(mouseX, mouseY);
        focused = inside;
        if (inside) blinkAnchorMs = System.currentTimeMillis();
        return inside;
    }

    @Override
    public boolean keyPress(int keyCode, int scanCode, int mods) {
        if (!focused) return false;
        switch (keyCode) {
            case KEY_BACKSPACE:
                if (cursor > 0) {
                    text.deleteCharAt(cursor - 1);
                    cursor--;
                    fireChange();
                }
                return true;
            case KEY_DELETE:
                if (cursor < text.length()) {
                    text.deleteCharAt(cursor);
                    fireChange();
                }
                return true;
            case KEY_LEFT:
                if (cursor > 0) cursor--;
                blinkAnchorMs = System.currentTimeMillis();
                return true;
            case KEY_RIGHT:
                if (cursor < text.length()) cursor++;
                blinkAnchorMs = System.currentTimeMillis();
                return true;
            case KEY_HOME:
                cursor = 0;
                blinkAnchorMs = System.currentTimeMillis();
                return true;
            case KEY_END:
                cursor = text.length();
                blinkAnchorMs = System.currentTimeMillis();
                return true;
            case KEY_ENTER:
                if (onSubmit != null) onSubmit.accept(text.toString());
                return true;
            case KEY_ESCAPE:
                focused = false;
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean charTyped(char ch, int mods) {
        if (!focused) return false;
        if (ch < 0x20 || ch == 0x7F) return false;
        if (digitsOnly && (ch < '0' || ch > '9')) return false;
        if (text.length() >= maxLength) return false;
        text.insert(cursor, ch);
        cursor++;
        fireChange();
        return true;
    }

    private void fireChange() {
        blinkAnchorMs = System.currentTimeMillis();
        if (onChange != null) onChange.accept(text.toString());
    }
}
