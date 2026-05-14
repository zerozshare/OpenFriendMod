/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.ui;

public class UScrollPane extends UComponent {

    private UPanel content;
    private int scrollY;
    private int scrollStep = 18;
    private boolean showScrollbar = true;

    public UScrollPane setContent(UPanel content) {
        if (this.content != null) this.content.parent = null;
        this.content = content;
        if (content != null) content.parent = this;
        clampScroll();
        return this;
    }

    public UScrollPane setScrollStep(int px)          { this.scrollStep = Math.max(1, px); return this; }
    public UScrollPane setShowScrollbar(boolean show) { this.showScrollbar = show; return this; }

    public UPanel content() { return content; }

    public int scrollY()    { return scrollY; }
    public int maxScroll()  { return content == null ? 0 : Math.max(0, content.getHeight() - height); }

    public void scrollBy(int delta) {
        scrollY += delta;
        clampScroll();
    }

    public void scrollTo(int y) {
        scrollY = y;
        clampScroll();
    }

    private void clampScroll() {
        int max = maxScroll();
        if (scrollY < 0)   scrollY = 0;
        if (scrollY > max) scrollY = max;
    }

    @Override
    protected void onLayout() {
        if (content != null) content.setBounds(x, y, width, content.getHeight());
        clampScroll();
    }

    @Override
    public void render(URenderer r) {
        if (!visible) return;
        if (content == null) return;

        content.setBounds(x, y, width, content.getHeight());

        r.pushClip(x, y, width, height);
        r.pushTranslate(0, -scrollY);
        try {
            content.render(r);
        } finally {
            r.popTranslate();
            r.popClip();
        }

        if (showScrollbar) renderScrollbar(r);
    }

    private void renderScrollbar(URenderer r) {
        int max = maxScroll();
        if (max <= 0) return;
        int trackW = 3;
        int trackX = x + width - trackW - 2;
        int trackY = y + 2;
        int trackH = height - 4;
        r.fillRect(trackX, trackY, trackW, trackH, UTheme.BORDER);
        int thumbH = Math.max(16, (int) ((long) trackH * height / Math.max(1, content.getHeight())));
        int thumbY = trackY + (int) ((long) (trackH - thumbH) * scrollY / max);
        r.fillRect(trackX, thumbY, trackW, thumbH, UTheme.TEXT_DIM);
    }

    @Override
    public void tickHover(double mouseX, double mouseY) {
        super.tickHover(mouseX, mouseY);
        if (content != null) {
            if (containsPoint(mouseX, mouseY)) {
                content.tickHover(mouseX, mouseY + scrollY);
            } else {
                content.tickHover(-1, -1);
            }
        }
    }

    @Override
    public boolean mouseClick(double mouseX, double mouseY, int button) {
        if (!visible || !enabled || content == null) return false;
        if (!containsPoint(mouseX, mouseY)) return false;
        return content.mouseClick(mouseX, mouseY + scrollY, button);
    }

    @Override
    public boolean mouseRelease(double mouseX, double mouseY, int button) {
        return content != null && content.mouseRelease(mouseX, mouseY + scrollY, button);
    }

    @Override
    public boolean mouseScroll(double mouseX, double mouseY, double deltaY) {
        if (!visible || !containsPoint(mouseX, mouseY)) return false;
        scrollBy(-(int) Math.round(deltaY * scrollStep));
        return true;
    }

    @Override
    public boolean keyPress(int keyCode, int scanCode, int mods) {
        return content != null && content.keyPress(keyCode, scanCode, mods);
    }

    @Override
    public boolean charTyped(char ch, int mods) {
        return content != null && content.charTyped(ch, mods);
    }
}
