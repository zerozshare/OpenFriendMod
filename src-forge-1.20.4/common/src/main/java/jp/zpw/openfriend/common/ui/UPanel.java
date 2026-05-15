/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.ui;

import java.util.ArrayList;
import java.util.List;

public class UPanel extends UComponent {
    public enum Background { NONE, SURFACE, SURFACE_ALT, OVERLAY }

    private final List<UComponent> children = new ArrayList<>();
    private Background background = Background.NONE;
    private boolean bordered = false;
    private int padding = 0;

    public UPanel setBackground(Background b)   { this.background = b; return this; }
    public UPanel setBordered(boolean v)        { this.bordered = v; return this; }
    public UPanel setPadding(int p)             { this.padding = p; return this; }

    public int padding() { return padding; }
    public int innerX() { return x + padding; }
    public int innerY() { return y + padding; }
    public int innerW() { return Math.max(0, width  - padding * 2); }
    public int innerH() { return Math.max(0, height - padding * 2); }

    public UPanel addChild(UComponent c) {
        c.parent = this;
        children.add(c);
        return this;
    }

    public UPanel removeChild(UComponent c) {
        children.remove(c);
        if (c.parent == this) c.parent = null;
        return this;
    }

    public void clearChildren() {
        for (UComponent c : children) if (c.parent == this) c.parent = null;
        children.clear();
    }

    public List<UComponent> children() { return children; }

    @Override
    public void render(URenderer r) {
        if (!visible) return;
        switch (background) {
            case SURFACE:     r.fillRect(x, y, width, height, UTheme.SURFACE);     break;
            case SURFACE_ALT: r.fillRect(x, y, width, height, UTheme.SURFACE_ALT); break;
            case OVERLAY:     r.fillRect(x, y, width, height, UTheme.OVERLAY_BG);  break;
            case NONE: default: break;
        }
        if (bordered) r.strokeRect(x, y, width, height, UTheme.BORDER);

        r.pushClip(innerX(), innerY(), innerW(), innerH());
        try {
            for (UComponent c : children) {
                if (c.isVisible()) c.render(r);
            }
        } finally {
            r.popClip();
        }
    }

    @Override
    public void tickHover(double mouseX, double mouseY) {
        super.tickHover(mouseX, mouseY);
        for (UComponent c : children) c.tickHover(mouseX, mouseY);
    }

    @Override
    public boolean mouseClick(double mouseX, double mouseY, int button) {
        if (!visible || !enabled) return false;
        for (int i = children.size() - 1; i >= 0; i--) {
            UComponent c = children.get(i);
            if (c.isVisible() && c.mouseClick(mouseX, mouseY, button)) return true;
        }
        return false;
    }

    @Override
    public boolean mouseRelease(double mouseX, double mouseY, int button) {
        boolean handled = false;
        for (UComponent c : children) {
            if (c.mouseRelease(mouseX, mouseY, button)) handled = true;
        }
        return handled;
    }

    @Override
    public boolean mouseScroll(double mouseX, double mouseY, double deltaY) {
        if (!visible) return false;
        if (!containsPoint(mouseX, mouseY)) return false;
        for (int i = children.size() - 1; i >= 0; i--) {
            UComponent c = children.get(i);
            if (c.isVisible() && c.mouseScroll(mouseX, mouseY, deltaY)) return true;
        }
        return false;
    }

    @Override
    public boolean keyPress(int keyCode, int scanCode, int mods) {
        for (UComponent c : children) {
            if (!c.isVisible() || !c.isEnabled()) continue;
            if (c.keyPress(keyCode, scanCode, mods)) return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char ch, int mods) {
        for (UComponent c : children) {
            if (!c.isVisible() || !c.isEnabled()) continue;
            if (c.charTyped(ch, mods)) return true;
        }
        return false;
    }
}
