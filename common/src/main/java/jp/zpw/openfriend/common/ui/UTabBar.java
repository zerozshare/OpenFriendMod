/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public final class UTabBar extends UComponent {

    public static final class Tab {
        public final String id;
        public final String label;
        public final int badge;

        public Tab(String id, String label) { this(id, label, 0); }
        public Tab(String id, String label, int badge) {
            this.id = id;
            this.label = label;
            this.badge = badge;
        }
    }

    private final List<Tab> tabs = new ArrayList<>();
    private int selected;
    private IntConsumer onChange;

    private int tabPaddingX = 14;
    private int gap = 4;
    private int underlineThickness = 2;

    private int[] cachedX = new int[0];
    private int[] cachedW = new int[0];

    public UTabBar setTabs(List<Tab> list) {
        tabs.clear();
        if (list != null) tabs.addAll(list);
        if (selected >= tabs.size()) selected = Math.max(0, tabs.size() - 1);
        cachedX = new int[tabs.size()];
        cachedW = new int[tabs.size()];
        return this;
    }

    public UTabBar setOnChange(IntConsumer cb) { this.onChange = cb; return this; }
    public UTabBar setSelected(int i)          { this.selected = clamp(i); return this; }
    public UTabBar setTabPaddingX(int p)       { this.tabPaddingX = Math.max(0, p); return this; }
    public UTabBar setGap(int g)               { this.gap = Math.max(0, g); return this; }

    public int getSelected()       { return selected; }
    public Tab getSelectedTab()    { return tabs.isEmpty() ? null : tabs.get(selected); }
    public int tabCount()          { return tabs.size(); }

    private int clamp(int i) {
        if (tabs.isEmpty()) return 0;
        if (i < 0) return 0;
        if (i >= tabs.size()) return tabs.size() - 1;
        return i;
    }

    @Override
    public void render(URenderer r) {
        if (!visible || tabs.isEmpty()) return;
        int n = tabs.size();
        int tabW = width / n;
        int leftover = width - tabW * n;
        int textY = y + (height - r.textHeight()) / 2;
        int cursorX = x;
        for (int i = 0; i < n; i++) {
            Tab t = tabs.get(i);
            int w = tabW + (i < leftover ? 1 : 0);
            cachedX[i] = cursorX;
            cachedW[i] = w;

            boolean active = (i == selected);
            int fg = active ? UTheme.TEXT : UTheme.TEXT_DIM;
            r.drawTextCentered(cursorX, textY, w, t.label, fg);

            if (t.badge > 0) {
                String b = t.badge > 99 ? "99+" : Integer.toString(t.badge);
                int bw = r.textWidth(b) + 6;
                int labelW = r.textWidth(t.label);
                int bx = cursorX + (w + labelW) / 2 + 2;
                int by = y + 2;
                r.fillRect(bx, by, bw, r.textHeight() + 2, UTheme.ACCENT);
                r.drawTextCentered(bx, by + 1, bw, b, 0xFF000000);
            }
            if (active) {
                int uy = y + height - underlineThickness;
                int labelW = r.textWidth(t.label);
                int underlineW = Math.min(w - 6, labelW + 8);
                int underlineX = cursorX + (w - underlineW) / 2;
                r.fillRect(underlineX, uy, underlineW, underlineThickness, UTheme.ACCENT);
            }
            cursorX += w;
        }
    }

    @Override
    public boolean mouseClick(double mouseX, double mouseY, int button) {
        if (!visible || !enabled || button != 0) return false;
        if (!containsPoint(mouseX, mouseY)) return false;
        for (int i = 0; i < tabs.size(); i++) {
            int tx = cachedX[i];
            int tw = cachedW[i];
            if (tw <= 0) continue;
            if (mouseX >= tx && mouseX < tx + tw) {
                if (i != selected) {
                    selected = i;
                    if (onChange != null) onChange.accept(i);
                }
                return true;
            }
        }
        return false;
    }
}
