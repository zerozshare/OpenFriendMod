/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.screen;

import jp.zpw.openfriend.common.model.Friend;
import jp.zpw.openfriend.common.model.PresenceStatus;
import jp.zpw.openfriend.common.ui.UButton;
import jp.zpw.openfriend.common.ui.UComponent;
import jp.zpw.openfriend.common.ui.URenderer;
import jp.zpw.openfriend.common.ui.UTheme;

public class FriendEntry extends UComponent {

    public static final int BASE_ROW_HEIGHT = 36;
    public static final int MENU_STRIP_HEIGHT = 24;
    public static final int ROW_HEIGHT = BASE_ROW_HEIGHT;

    private static final int HEAD_SIZE = 24;
    private static final int PAD_X = 10;
    private static final int BTN_W = 24;
    private static final int BTN_H = 22;
    private static final int BTN_GAP = 4;

    public interface Actions {
        default void onJoin(Friend f)    {}
        default void onRemove(Friend f)  {}
        default void onBlock(Friend f)   {}
        default void onUnblock(Friend f) {}
    }

    private final Friend friend;
    private PresenceStatus status = PresenceStatus.UNKNOWN;
    private boolean blocked;
    private final Actions actions;
    private final UButton joinBtn;
    private final UButton menuBtn;
    private final UButton removeBtn;
    private final UButton blockBtn;
    private final UButton cancelBtn;

    private boolean menuOpen;
    private Runnable onMenuToggle;

    public FriendEntry(Friend friend, Actions actions) {
        this.friend = friend;
        this.actions = actions == null ? new Actions() {} : actions;
        this.joinBtn   = new UButton(">", this::triggerJoin).setStyle(UButton.Style.PRIMARY);
        this.joinBtn.setVisible(false);
        this.menuBtn   = new UButton("...",    this::toggleMenu).setStyle(UButton.Style.SUBTLE);
        this.removeBtn = new UButton("Remove", () -> { actions.onRemove(friend); closeMenu(); })
                .setStyle(UButton.Style.DANGER);
        this.blockBtn  = new UButton("Block",  () -> {
                if (blocked) actions.onUnblock(friend); else actions.onBlock(friend);
                closeMenu();
            }).setStyle(UButton.Style.SUBTLE);
        this.cancelBtn = new UButton("Cancel", this::closeMenu).setStyle(UButton.Style.GHOST);
        this.removeBtn.setVisible(false);
        this.blockBtn.setVisible(false);
        this.cancelBtn.setVisible(false);
    }

    public Friend friend() { return friend; }
    public boolean isMenuOpen() { return menuOpen; }
    public int totalHeight() { return menuOpen ? BASE_ROW_HEIGHT + MENU_STRIP_HEIGHT : BASE_ROW_HEIGHT; }

    public FriendEntry setOnMenuToggle(Runnable r) { this.onMenuToggle = r; return this; }

    public FriendEntry setPresence(PresenceStatus s) {
        this.status = s == null ? PresenceStatus.UNKNOWN : s;
        joinBtn.setVisible(this.status.isHosting() && !blocked);
        return this;
    }

    public FriendEntry setBlocked(boolean v) {
        this.blocked = v;
        joinBtn.setVisible(this.status.isHosting() && !blocked);
        return this;
    }

    private void triggerJoin() {
        actions.onJoin(friend);
    }

    private void toggleMenu() {
        menuOpen = !menuOpen;
        removeBtn.setVisible(menuOpen);
        blockBtn.setVisible(menuOpen);
        cancelBtn.setVisible(menuOpen);
        if (onMenuToggle != null) onMenuToggle.run();
    }

    public void closeMenu() {
        if (!menuOpen) return;
        menuOpen = false;
        removeBtn.setVisible(false);
        blockBtn.setVisible(false);
        cancelBtn.setVisible(false);
        if (onMenuToggle != null) onMenuToggle.run();
    }

    @Override
    protected void onLayout() {
        int right = x + width - PAD_X;
        int rowBy = y + (BASE_ROW_HEIGHT - BTN_H) / 2;
        menuBtn.setBounds(right - BTN_W, rowBy, BTN_W, BTN_H);
        if (joinBtn.isVisible()) {
            joinBtn.setBounds(right - BTN_W - BTN_GAP - BTN_W, rowBy, BTN_W, BTN_H);
        }

        if (menuOpen) {
            int stripY = y + BASE_ROW_HEIGHT;
            int btnH = MENU_STRIP_HEIGHT - 4;
            int stripBy = stripY + (MENU_STRIP_HEIGHT - btnH) / 2;
            int rw = 60;
            int bw = 60;
            int cw = 60;
            int gap = 6;
            int totalW = rw + bw + cw + gap * 2;
            int sx = x + width - PAD_X - totalW;
            removeBtn.setBounds(sx,                      stripBy, rw, btnH);
            blockBtn .setBounds(sx + rw + gap,           stripBy, bw, btnH);
            cancelBtn.setBounds(sx + rw + bw + gap * 2,  stripBy, cw, btnH);
        }
    }

    @Override
    public void render(URenderer r) {
        if (!visible) return;

        int rowH = BASE_ROW_HEIGHT;

        if (hovered) r.fillRect(x, y, width, rowH, UTheme.SURFACE_ALT);

        r.fillRect(x + PAD_X, y + (rowH - HEAD_SIZE) / 2, HEAD_SIZE, HEAD_SIZE, UTheme.SURFACE);
        r.drawHead(x + PAD_X, y + (rowH - HEAD_SIZE) / 2, HEAD_SIZE, friend.profileId.toString());

        int textX = x + PAD_X + HEAD_SIZE + 10;
        int textY = y + (rowH - r.textHeight() * 2 - 2) / 2;
        int nameColor = blocked ? UTheme.TEXT_FAINT : UTheme.TEXT;
        int rightEdge = menuBtn.getX();
        if (joinBtn.isVisible()) rightEdge = Math.min(rightEdge, joinBtn.getX());
        int textMaxW = Math.max(0, rightEdge - textX - 6);
        r.drawTextClipped(textX, textY,                      textMaxW, friend.name, nameColor);
        r.drawTextClipped(textX, textY + r.textHeight() + 2, textMaxW, statusLabel(), statusColor());

        if (joinBtn.isVisible()) {
            joinBtn.tickHover(r.currentMouseX(), r.currentMouseY());
            joinBtn.render(r);
        }
        menuBtn.tickHover(r.currentMouseX(), r.currentMouseY());
        menuBtn.render(r);

        if (menuOpen) {
            int stripY = y + BASE_ROW_HEIGHT;
            r.fillRect(x, stripY, width, MENU_STRIP_HEIGHT, UTheme.SURFACE);
            blockBtn.setLabel(blocked ? "Unblock" : "Block");
            removeBtn.tickHover(r.currentMouseX(), r.currentMouseY());
            blockBtn .tickHover(r.currentMouseX(), r.currentMouseY());
            cancelBtn.tickHover(r.currentMouseX(), r.currentMouseY());
            removeBtn.render(r);
            blockBtn .render(r);
            cancelBtn.render(r);
        }

        r.fillRect(x + PAD_X, y + totalHeight() - 1, width - PAD_X * 2, 1, UTheme.BORDER);
    }

    private String statusLabel() {
        if (blocked) return "Blocked";
        switch (status) {
            case ONLINE:                return "Online";
            case PLAYING_HOSTED_SERVER: return "In a joinable world";
            case PLAYING_SERVER:        return "Playing on a server";
            case PLAYING_REALMS:        return "Playing on Realms";
            case PLAYING_OFFLINE:       return "Playing offline";
            case OFFLINE:
            default:                    return "Offline";
        }
    }

    private int statusColor() {
        if (blocked) return UTheme.TEXT_FAINT;
        switch (status) {
            case ONLINE:                return UTheme.ONLINE;
            case PLAYING_HOSTED_SERVER: return UTheme.ONLINE;
            case PLAYING_SERVER:
            case PLAYING_REALMS:
            case PLAYING_OFFLINE:       return UTheme.PLAYING;
            case OFFLINE:
            default:                    return UTheme.OFFLINE;
        }
    }

    @Override
    public boolean containsPoint(double px, double py) {
        return visible && px >= x && px < x + width && py >= y && py < y + totalHeight();
    }

    @Override
    public boolean mouseClick(double mouseX, double mouseY, int button) {
        if (!visible || !enabled) return false;
        if (joinBtn.isVisible() && joinBtn.mouseClick(mouseX, mouseY, button)) return true;
        if (menuBtn.mouseClick(mouseX, mouseY, button)) return true;
        if (menuOpen) {
            if (removeBtn.mouseClick(mouseX, mouseY, button)) return true;
            if (blockBtn .mouseClick(mouseX, mouseY, button)) return true;
            if (cancelBtn.mouseClick(mouseX, mouseY, button)) return true;
        }
        return false;
    }

    @Override
    public boolean mouseRelease(double mouseX, double mouseY, int button) {
        boolean a = joinBtn.mouseRelease(mouseX, mouseY, button);
        boolean m = menuBtn.mouseRelease(mouseX, mouseY, button);
        boolean r = false, b = false, c = false;
        if (menuOpen) {
            r = removeBtn.mouseRelease(mouseX, mouseY, button);
            b = blockBtn .mouseRelease(mouseX, mouseY, button);
            c = cancelBtn.mouseRelease(mouseX, mouseY, button);
        }
        return a || m || r || b || c;
    }
}
