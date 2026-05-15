/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.screen;

import jp.zpw.openfriend.common.state.FriendsState;
import jp.zpw.openfriend.common.ui.UButton;
import jp.zpw.openfriend.common.ui.UComponent;
import jp.zpw.openfriend.common.ui.UDivider;
import jp.zpw.openfriend.common.ui.UPanel;
import jp.zpw.openfriend.common.ui.URenderer;
import jp.zpw.openfriend.common.ui.UTabBar;
import jp.zpw.openfriend.common.ui.UTheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FriendsOverlayScreen {

    public static final int PANEL_WIDTH  = 320;
    public static final int PANEL_HEIGHT = 208;
    public static final int HEADER_HEIGHT = 22;
    public static final int TABBAR_HEIGHT = 20;
    public static final int MIN_PANEL_W = 240;
    public static final int MIN_PANEL_H = 160;
    public static final int SCREEN_MARGIN = 16;

    public interface Tab {
        String id();
        String label();
        int badge();
        UComponent body();
        default void onShow() {}
        default void onHide() {}
    }

    private final FriendsState state;
    private final Runnable onClose;

    private final List<Tab> tabs = new ArrayList<>();
    private final UPanel root = new UPanel();
    private final UPanel modal = new UPanel();
    private final UPanel header = new UPanel();
    private final UTabBar tabBar = new UTabBar();
    private final UDivider headerDiv = new UDivider();
    private final UDivider tabDiv = new UDivider();
    private final UPanel contentSlot = new UPanel();
    private final UButton closeBtn;

    private int activeIndex = 0;
    private UComponent activeBody;

    public FriendsOverlayScreen(FriendsState state, Runnable onClose) {
        this.state = state;
        this.onClose = onClose;

        root.setBackground(UPanel.Background.OVERLAY);
        modal.setBackground(UPanel.Background.SURFACE);
        modal.setBordered(true);

        header.setBackground(UPanel.Background.SURFACE_ALT);

        closeBtn = new UButton("✕", () -> {
            if (this.onClose != null) this.onClose.run();
        }).setStyle(UButton.Style.GHOST);

        modal.addChild(header);
        modal.addChild(headerDiv);
        modal.addChild(tabBar);
        modal.addChild(tabDiv);
        modal.addChild(contentSlot);

        header.addChild(closeBtn);
        root.addChild(modal);

        tabBar.setOnChange(this::switchTab);
    }

    public FriendsState state() { return state; }
    public List<Tab> tabs()     { return tabs; }
    public UPanel rootPanel()   { return root; }

    public void setTabs(Tab... defs) {
        tabs.clear();
        tabs.addAll(Arrays.asList(defs));
        applyTabBar();
        if (!tabs.isEmpty()) switchTab(0);
    }

    public void refreshBadges() {
        applyTabBar();
    }

    private void applyTabBar() {
        List<UTabBar.Tab> bar = new ArrayList<>(tabs.size());
        for (Tab t : tabs) bar.add(new UTabBar.Tab(t.id(), t.label(), t.badge()));
        tabBar.setTabs(bar);
        tabBar.setSelected(activeIndex);
    }

    private void switchTab(int newIndex) {
        if (newIndex < 0 || newIndex >= tabs.size()) return;
        if (newIndex != activeIndex && activeIndex < tabs.size()) {
            tabs.get(activeIndex).onHide();
        }
        activeIndex = newIndex;
        contentSlot.clearChildren();
        Tab t = tabs.get(activeIndex);
        activeBody = t.body();
        if (activeBody != null) {
            activeBody.setBounds(contentSlot.innerX(), contentSlot.innerY(), contentSlot.innerW(), contentSlot.innerH());
            contentSlot.addChild(activeBody);
        }
        t.onShow();
    }

    public void layout(int screenWidth, int screenHeight) {
        root.setBounds(0, 0, screenWidth, screenHeight);

        int maxW = Math.max(MIN_PANEL_W, screenWidth  - SCREEN_MARGIN * 2);
        int maxH = Math.max(MIN_PANEL_H, screenHeight - SCREEN_MARGIN * 2);
        int panelW = Math.min(PANEL_WIDTH,  maxW);
        int panelH = Math.min(PANEL_HEIGHT, maxH);

        int mx = (screenWidth  - panelW) / 2;
        int my = (screenHeight - panelH) / 2;
        modal.setBounds(mx, my, panelW, panelH);

        int btn = HEADER_HEIGHT - 6;
        header.setBounds(mx, my, panelW, HEADER_HEIGHT);
        closeBtn.setBounds(mx + panelW - btn - 4, my + (HEADER_HEIGHT - btn) / 2, btn, btn);
        headerDiv.setBounds(mx, my + HEADER_HEIGHT, panelW, 1);

        tabBar.setBounds(mx + 8, my + HEADER_HEIGHT + 1, panelW - 16, TABBAR_HEIGHT);
        tabDiv.setBounds(mx, my + HEADER_HEIGHT + 1 + TABBAR_HEIGHT, panelW, 1);

        int contentY = my + HEADER_HEIGHT + 2 + TABBAR_HEIGHT;
        int contentH = panelH - (contentY - my);
        contentSlot.setBounds(mx, contentY, panelW, contentH);

        if (activeBody != null) {
            activeBody.setBounds(contentSlot.innerX(), contentSlot.innerY(),
                                 contentSlot.innerW(), contentSlot.innerH());
        }
    }

    public void render(URenderer r) {
        root.tickHover(r.currentMouseX(), r.currentMouseY());
        root.render(r);

        int titleY = header.getY() + (header.getHeight() - r.textHeight()) / 2;
        r.drawText(header.getX() + 14, titleY, "Friends", UTheme.TEXT);
    }

    public boolean mouseClick(double mouseX, double mouseY, int button) {
        if (button == 0 && !modal.containsPoint(mouseX, mouseY)) {
            if (onClose != null) onClose.run();
            return true;
        }
        return root.mouseClick(mouseX, mouseY, button);
    }

    public boolean mouseRelease(double mouseX, double mouseY, int button) {
        return root.mouseRelease(mouseX, mouseY, button);
    }

    public boolean mouseScroll(double mouseX, double mouseY, double deltaY) {
        return root.mouseScroll(mouseX, mouseY, deltaY);
    }

    public boolean keyPress(int keyCode, int scanCode, int mods) {
        if (keyCode == 256) {
            if (onClose != null) onClose.run();
            return true;
        }
        return root.keyPress(keyCode, scanCode, mods);
    }

    public boolean charTyped(char ch, int mods) {
        return root.charTyped(ch, mods);
    }
}
