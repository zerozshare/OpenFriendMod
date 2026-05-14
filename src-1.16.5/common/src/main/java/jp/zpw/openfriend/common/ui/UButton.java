/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.ui;

public class UButton extends UComponent {
    public enum Style { PRIMARY, GHOST, DANGER, SUBTLE }

    private String label;
    private Runnable onClick;
    private Style style = Style.PRIMARY;
    private boolean pressed;

    public UButton(String label, Runnable onClick) {
        this.label = label;
        this.onClick = onClick;
    }

    public UButton setLabel(String l)          { this.label = l; return this; }
    public UButton setOnClick(Runnable cb)     { this.onClick = cb; return this; }
    public UButton setStyle(Style s)           { this.style = s; return this; }
    public String getLabel()                   { return label; }
    public Style getStyle()                    { return style; }

    @Override
    public void render(URenderer r) {
        if (!visible) return;

        int bg, border, fg;
        switch (style) {
            case PRIMARY: {
                bg     = enabled ? (hovered ? lighten(UTheme.ACCENT) : UTheme.ACCENT) : UTheme.SURFACE;
                border = enabled ? bg : UTheme.BORDER;
                fg     = enabled ? 0xFF000000 : UTheme.TEXT_FAINT;
                break;
            }
            case DANGER: {
                bg     = enabled ? (hovered ? lighten(UTheme.DANGER) : UTheme.DANGER) : UTheme.SURFACE;
                border = enabled ? bg : UTheme.BORDER;
                fg     = enabled ? 0xFFFFFFFF : UTheme.TEXT_FAINT;
                break;
            }
            case SUBTLE: {
                bg     = hovered ? UTheme.SURFACE_ALT : UTheme.SURFACE;
                border = UTheme.BORDER;
                fg     = enabled ? UTheme.TEXT : UTheme.TEXT_FAINT;
                break;
            }
            case GHOST:
            default: {
                bg     = hovered ? UTheme.SURFACE : 0x00000000;
                border = hovered ? UTheme.BORDER_HOV : UTheme.BORDER;
                fg     = enabled ? UTheme.TEXT : UTheme.TEXT_FAINT;
                break;
            }
        }

        r.fillRect(x, y, width, height, bg);
        r.strokeRect(x, y, width, height, border);
        if (label != null) {
            int ty = y + (height - r.textHeight()) / 2;
            r.drawTextCentered(x, ty, width, label, fg);
        }
    }

    @Override
    public boolean mouseClick(double mouseX, double mouseY, int button) {
        if (!visible || !enabled || button != 0) return false;
        if (!containsPoint(mouseX, mouseY)) return false;
        pressed = true;
        if (onClick != null) onClick.run();
        return true;
    }

    @Override
    public boolean mouseRelease(double mouseX, double mouseY, int button) {
        if (pressed && button == 0) {
            pressed = false;
            return true;
        }
        return false;
    }

    private static int lighten(int argb) {
        int a = (argb >>> 24) & 0xFF;
        int r = Math.min(0xFF, ((argb >> 16) & 0xFF) + 0x18);
        int g = Math.min(0xFF, ((argb >>  8) & 0xFF) + 0x18);
        int b = Math.min(0xFF, ( argb        & 0xFF) + 0x18);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
