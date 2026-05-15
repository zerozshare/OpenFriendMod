/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.screen;

import jp.zpw.openfriend.common.model.Friend;
import jp.zpw.openfriend.common.ui.UButton;
import jp.zpw.openfriend.common.ui.UComponent;
import jp.zpw.openfriend.common.ui.URenderer;
import jp.zpw.openfriend.common.ui.UTheme;

public class PendingEntry extends UComponent {

    public static final int ROW_HEIGHT = 36;
    private static final int HEAD_SIZE = 24;
    private static final int PAD_X = 10;

    public enum Direction { INCOMING, OUTGOING }

    public interface Actions {
        default void onAccept(Friend f)  {}
        default void onDecline(Friend f) {}
        default void onCancel(Friend f)  {}
    }

    private final Friend friend;
    private final Direction direction;
    private final Actions actions;
    private final UButton primary;
    private final UButton secondary;

    public PendingEntry(Friend friend, Direction direction, Actions actions) {
        this.friend = friend;
        this.direction = direction;
        this.actions = actions == null ? new Actions() {} : actions;
        if (direction == Direction.INCOMING) {
            this.primary   = new UButton("Accept",  () -> this.actions.onAccept(friend)).setStyle(UButton.Style.PRIMARY);
            this.secondary = new UButton("Decline", () -> this.actions.onDecline(friend)).setStyle(UButton.Style.GHOST);
        } else {
            this.primary   = new UButton("Cancel",  () -> this.actions.onCancel(friend)).setStyle(UButton.Style.SUBTLE);
            this.secondary = null;
        }
    }

    public Friend friend()        { return friend; }
    public Direction direction()  { return direction; }

    @Override
    protected void onLayout() {
        int btnH = 22;
        int btnW = 56;
        int rightX = x + width - PAD_X - btnW;
        primary.setBounds(rightX, y + (height - btnH) / 2, btnW, btnH);
        if (secondary != null) {
            int sw = 60;
            secondary.setBounds(rightX - sw - 6, y + (height - btnH) / 2, sw, btnH);
        }
    }

    @Override
    public void render(URenderer r) {
        if (!visible) return;

        if (hovered) r.fillRect(x, y, width, height, UTheme.SURFACE_ALT);

        int headY = y + (height - HEAD_SIZE) / 2;
        r.fillRect(x + PAD_X, headY, HEAD_SIZE, HEAD_SIZE, UTheme.SURFACE);
        r.drawHead(x + PAD_X, headY, HEAD_SIZE, friend.profileId.toString());

        int textX = x + PAD_X + HEAD_SIZE + 10;
        int textY = y + (height - r.textHeight() * 2 - 2) / 2;
        r.drawText(textX, textY, friend.name, UTheme.TEXT);
        r.drawText(textX, textY + r.textHeight() + 2, label(), UTheme.TEXT_DIM);

        primary.tickHover(r.currentMouseX(), r.currentMouseY());
        primary.render(r);
        if (secondary != null) {
            secondary.tickHover(r.currentMouseX(), r.currentMouseY());
            secondary.render(r);
        }

        r.fillRect(x + PAD_X, y + height - 1, width - PAD_X * 2, 1, UTheme.BORDER);
    }

    private String label() {
        return direction == Direction.INCOMING ? "Wants to be friends" : "Request sent";
    }

    @Override
    public boolean mouseClick(double mouseX, double mouseY, int button) {
        if (!visible || !enabled) return false;
        if (primary.mouseClick(mouseX, mouseY, button)) return true;
        if (secondary != null && secondary.mouseClick(mouseX, mouseY, button)) return true;
        return false;
    }

    @Override
    public boolean mouseRelease(double mouseX, double mouseY, int button) {
        boolean handled = primary.mouseRelease(mouseX, mouseY, button);
        if (secondary != null) handled |= secondary.mouseRelease(mouseX, mouseY, button);
        return handled;
    }
}
