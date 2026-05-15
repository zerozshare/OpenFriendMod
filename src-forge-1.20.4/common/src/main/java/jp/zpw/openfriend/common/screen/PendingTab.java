/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.screen;

import jp.zpw.openfriend.common.model.Friend;
import jp.zpw.openfriend.common.state.FriendsState;
import jp.zpw.openfriend.common.ui.UComponent;
import jp.zpw.openfriend.common.ui.UPanel;
import jp.zpw.openfriend.common.ui.URenderer;
import jp.zpw.openfriend.common.ui.UScrollPane;
import jp.zpw.openfriend.common.ui.UTheme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class PendingTab implements FriendsOverlayScreen.Tab {

    private static final int SECTION_HEADER_HEIGHT = 22;

    private final FriendsState state;
    private final PendingEntry.Actions actions;

    private final UScrollPane scroll = new UScrollPane();
    private final UPanel content = new UPanel();
    private final UPanel empty = new EmptyState();

    private final Runnable stateListener = this::markDirty;
    private boolean dirty = true;

    public PendingTab(FriendsState state, PendingEntry.Actions actions) {
        this.state = state;
        this.actions = actions;
        this.scroll.setContent(content);
    }

    @Override public String id()    { return "pending"; }
    @Override public String label() { return "Requests (" + state.incoming().size() + ")"; }
    @Override public int badge()    { return 0; }

    @Override
    public UComponent body() {
        return new BodyPanel();
    }

    @Override
    public void onShow() {
        state.addChangeListener(stateListener);
        markDirty();
    }

    @Override
    public void onHide() {
        state.removeChangeListener(stateListener);
    }

    private void markDirty() {
        dirty = true;
    }

    private boolean isEmpty() {
        return state.incoming().isEmpty() && state.outgoing().isEmpty();
    }

    private void rebuildIfNeeded() {
        if (!dirty) return;
        dirty = false;
        content.clearChildren();

        int y = content.getY();
        y = appendSection(y, "Incoming",  state.incoming().values(),  PendingEntry.Direction.INCOMING);
        y = appendSection(y, "Outgoing",  state.outgoing().values(),  PendingEntry.Direction.OUTGOING);

        int contentH = Math.max(1, y - content.getY());
        content.setBounds(content.getX(), content.getY(), content.getWidth(), contentH);
    }

    private int appendSection(int y, String title, Collection<Friend> friends, PendingEntry.Direction dir) {
        if (friends.isEmpty()) return y;

        SectionHeader header = new SectionHeader(title, friends.size());
        header.setBounds(content.getX(), y, content.getWidth(), SECTION_HEADER_HEIGHT);
        content.addChild(header);
        y += SECTION_HEADER_HEIGHT;

        List<Friend> sorted = new ArrayList<>(friends);
        sorted.sort(Comparator.comparing(f -> f.name.toLowerCase()));
        for (Friend f : sorted) {
            PendingEntry row = new PendingEntry(f, dir, actions);
            row.setBounds(content.getX(), y, content.getWidth(), PendingEntry.ROW_HEIGHT);
            content.addChild(row);
            y += PendingEntry.ROW_HEIGHT;
        }
        return y;
    }

    private final class BodyPanel extends UPanel {
        BodyPanel() {
            setBackground(Background.NONE);
            addChild(scroll);
            addChild(empty);
        }

        @Override
        protected void onLayout() {
            scroll.setBounds(x, y, width, height);
            content.setBounds(x, y, width, content.getHeight());
            empty.setBounds(x, y, width, height);
            markDirty();
        }

        @Override
        public void render(URenderer r) {
            rebuildIfNeeded();
            boolean any = !isEmpty();
            scroll.setVisible(any);
            empty.setVisible(!any);
            super.render(r);
        }
    }

    private static final class SectionHeader extends UComponent {
        private final String title;
        private final int count;

        SectionHeader(String title, int count) {
            this.title = title;
            this.count = count;
        }

        @Override
        public void render(URenderer r) {
            if (!visible) return;
            int textY = y + (height - r.textHeight()) / 2;
            r.drawText(x + 10, textY, title.toUpperCase(), UTheme.ACCENT_DIM);
            String c = Integer.toString(count);
            r.drawTextRight(x, textY, width - 10, c, UTheme.TEXT_DIM);
            r.fillRect(x + 10, y + height - 1, width - 20, 1, UTheme.BORDER);
        }
    }

    private static final class EmptyState extends UPanel {
        EmptyState() { setBackground(Background.NONE); }

        @Override
        public void render(URenderer r) {
            if (!isVisible()) return;
            int textY = y + height / 2 - r.textHeight() / 2;
            r.drawTextCentered(x, textY, width, "No pending friend requests", UTheme.TEXT_DIM);
        }
    }
}
