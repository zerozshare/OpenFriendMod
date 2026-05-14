/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.screen;

import jp.zpw.openfriend.common.model.Friend;
import jp.zpw.openfriend.common.model.PresenceStatus;
import jp.zpw.openfriend.common.state.FriendsState;
import jp.zpw.openfriend.common.ui.UButton;
import jp.zpw.openfriend.common.ui.UComponent;
import jp.zpw.openfriend.common.ui.UInput;
import jp.zpw.openfriend.common.ui.UPanel;
import jp.zpw.openfriend.common.ui.URenderer;
import jp.zpw.openfriend.common.ui.UScrollPane;
import jp.zpw.openfriend.common.ui.UTheme;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class FriendsTab implements FriendsOverlayScreen.Tab {

    private final FriendsState state;
    private final FriendEntry.Actions friendActions;
    private final AddFriendTab.Actions addActions;
    private final Runnable onRefresh;
    private final jp.zpw.openfriend.common.notice.NoticeSink notice;

    private final UScrollPane scroll = new UScrollPane();
    private final UPanel content = new UPanel().setPadding(0);

    private final UInput input = new UInput();
    private final UButton sendBtn;

    private AddFriendTab.State addState = AddFriendTab.State.IDLE;
    private String addMessage = "";
    private String addQuery = "";

    private final Runnable stateListener = this::markDirty;
    private volatile boolean dirty = true;
    private int lastVisibleFriendCount = -1;

    public FriendsTab(FriendsState state,
                      FriendEntry.Actions friendActions,
                      AddFriendTab.Actions addActions,
                      Runnable onRefresh,
                      jp.zpw.openfriend.common.notice.NoticeSink notice) {
        this.state = state;
        this.friendActions = friendActions;
        this.addActions = addActions;
        this.onRefresh = onRefresh;
        this.notice = notice == null ? new jp.zpw.openfriend.common.notice.NoticeSink() {} : notice;
        this.scroll.setContent(content);
        input.setPlaceholder("Enter Profile Name");
        input.setMaxLength(16);
        input.setOnSubmit(s -> onSubmit());
        sendBtn = new UButton(">", this::onSubmit).setStyle(UButton.Style.PRIMARY);
    }

    @Override public String id()    { return "friends"; }
    @Override public String label() { return "Friends"; }
    @Override public int badge()    { return 0; }

    @Override
    public UComponent body() { return new BodyPanel(); }

    @Override
    public void onShow() {
        state.addChangeListener(stateListener);
        dirty = true;
        if (onRefresh != null) onRefresh.run();
    }

    @Override
    public void onHide() {
        state.removeChangeListener(stateListener);
    }

    private void markDirty() { dirty = true; }

    private void onSubmit() {
        String q = input.getText() == null ? "" : input.getText().trim();
        if (q.isEmpty()) return;
        addQuery = q;
        setAddState(AddFriendTab.State.SEARCHING, "Sending request to " + q + "...");
        addActions.add(q, ok -> {
            if (Boolean.TRUE.equals(ok)) {
                setAddState(AddFriendTab.State.IDLE, "");
                input.setText("");
                addQuery = "";
                notice.success("Friend request sent", "Sent request to " + q + ".");
            } else {
                setAddState(AddFriendTab.State.IDLE, "");
                notice.error("Friend request failed", "Could not reach " + q + ". Check the gamertag.");
            }
        });
    }

    private void setAddState(AddFriendTab.State s, String msg) {
        this.addState = s;
        this.addMessage = msg == null ? "" : msg;
    }

    private int messageColor() {
        switch (addState) {
            case FOUND:           return UTheme.ACCENT;
            case SENT:            return UTheme.ONLINE;
            case NOT_FOUND:
            case ERROR:           return UTheme.DANGER;
            case ALREADY_FRIEND:
            case INCOMING_EXISTS:
            case OUTGOING_EXISTS: return UTheme.WARN;
            default:              return UTheme.TEXT_DIM;
        }
    }

    private void rebuildIfNeeded() {
        int currentCount = state.friends().size();
        if (!dirty && currentCount == lastVisibleFriendCount) return;
        dirty = false;
        lastVisibleFriendCount = currentCount;
        content.clearChildren();

        List<Friend> rows = new ArrayList<>(state.friends().values());
        rows.sort(Comparator
                .comparing((Friend f) -> rankOf(state.presenceOf(f.profileId)))
                .thenComparing(f -> f.name.toLowerCase()));

        for (Friend f : rows) {
            boolean isBlocked = state.blocks().contains(f.profileId);
            FriendEntry e = new FriendEntry(f, friendActions)
                    .setPresence(state.presenceOf(f.profileId))
                    .setBlocked(isBlocked);
            e.setOnMenuToggle(this::relayoutEntries);
            content.addChild(e);
        }
        relayoutEntries();
    }

    private void relayoutEntries() {
        int y0 = content.getY();
        int contentH = 0;
        for (jp.zpw.openfriend.common.ui.UComponent c : content.children()) {
            if (!(c instanceof FriendEntry)) continue;
            FriendEntry e = (FriendEntry) c;
            int h = e.totalHeight();
            e.setBounds(content.getX(), y0, content.getWidth(), h);
            y0 += h;
            contentH += h;
        }
        content.setBounds(content.getX(), content.getY(), content.getWidth(), Math.max(1, contentH));
    }

    private static int rankOf(PresenceStatus s) {
        if (s == null) return 4;
        switch (s) {
            case PLAYING_HOSTED_SERVER: return 0;
            case PLAYING_SERVER:
            case PLAYING_REALMS:
            case PLAYING_OFFLINE:       return 1;
            case ONLINE:                return 2;
            case OFFLINE:               return 3;
            default:                    return 4;
        }
    }

    private final class BodyPanel extends UPanel {
        private static final int INPUT_H = 20;
        private static final int SEND_W  = 22;
        private static final int PAD     = 8;
        private static final int MSG_H   = 14;

        BodyPanel() {
            setBackground(Background.NONE);
            addChild(input);
            addChild(sendBtn);
            addChild(scroll);
        }

        @Override
        protected void onLayout() {
            int rowY = y + PAD;
            input.setBounds(x + PAD, rowY, width - PAD * 2 - SEND_W - 4, INPUT_H);
            sendBtn.setBounds(x + width - PAD - SEND_W, rowY, SEND_W, INPUT_H);

            int msgY = rowY + INPUT_H + 2;
            int hasMsg = (addState != AddFriendTab.State.IDLE) ? MSG_H : 0;
            int listY = msgY + hasMsg + 4;
            int listH = Math.max(20, (y + height) - listY - PAD);
            scroll.setBounds(x, listY, width, listH);
            content.setBounds(x, listY, width, content.getHeight());
            if (!content.children().isEmpty()) relayoutEntries();
        }

        @Override
        public void render(URenderer r) {
            rebuildIfNeeded();

            super.render(r);

            if (addState != AddFriendTab.State.IDLE) {
                int msgY = y + PAD + INPUT_H + 4;
                r.drawText(x + PAD, msgY, addMessage, messageColor());
            }

            if (state.friends().isEmpty() && addState == AddFriendTab.State.IDLE) {
                int textY = scroll.getY() + scroll.getHeight() / 2 - r.textHeight() / 2;
                r.drawTextCentered(x, textY, width, "No friends yet — search by name above.", UTheme.TEXT_DIM);
            }
        }
    }
}
