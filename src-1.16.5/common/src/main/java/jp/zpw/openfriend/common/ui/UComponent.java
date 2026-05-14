/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.ui;

public abstract class UComponent {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected boolean visible = true;
    protected boolean enabled = true;
    protected boolean focused = false;
    protected boolean hovered = false;
    protected UComponent parent;

    public final int getX() { return x; }
    public final int getY() { return y; }
    public final int getWidth() { return width; }
    public final int getHeight() { return height; }

    public UComponent setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        onLayout();
        return this;
    }

    public UComponent setVisible(boolean v)     { this.visible = v; return this; }
    public UComponent setEnabled(boolean e)     { this.enabled = e; return this; }
    public boolean isVisible()                  { return visible; }
    public boolean isEnabled()                  { return enabled; }
    public boolean isFocused()                  { return focused; }
    public boolean isHovered()                  { return hovered; }

    public boolean containsPoint(double px, double py) {
        return visible
            && px >= x && px < x + width
            && py >= y && py < y + height;
    }

    public void tickHover(double mouseX, double mouseY) {
        hovered = containsPoint(mouseX, mouseY);
    }

    public abstract void render(URenderer r);

    public boolean mouseClick(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseRelease(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseScroll(double mouseX, double mouseY, double deltaY) {
        return false;
    }

    public boolean keyPress(int keyCode, int scanCode, int mods) {
        return false;
    }

    public boolean charTyped(char ch, int mods) {
        return false;
    }

    protected void onLayout() {}
}
