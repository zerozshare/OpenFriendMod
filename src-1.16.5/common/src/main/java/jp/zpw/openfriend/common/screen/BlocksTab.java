/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.screen;

import jp.zpw.openfriend.common.model.Friend;
import jp.zpw.openfriend.common.state.FriendsState;
import jp.zpw.openfriend.common.ui.UButton;
import jp.zpw.openfriend.common.ui.UComponent;
import jp.zpw.openfriend.common.ui.UPanel;
import jp.zpw.openfriend.common.ui.URenderer;
import jp.zpw.openfriend.common.ui.UScrollPane;
import jp.zpw.openfriend.common.ui.UTheme;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class BlocksTab implements FriendsOverlayScreen.Tab {

    public interface Actions {
        void unblock(UUID profileId);
    }

    private final FriendsState state;
    private final Actions actions;

    private final UScrollPane scroll = new UScrollPane();
    private final UPanel content = new UPanel();
    private final EmptyState empty = new EmptyState();

    private final Runnable stateListener = this::markDirty;
    private boolean dirty = true;

    public BlocksTab(FriendsState state, Actions actions) {
        this.state = state;
        this.actions = actions;
        this.scroll.setContent(content);
    }

    @Override public String id()    { return "blocks"; }
    @Override public String label() { return "Blocked"; }
    @Override public int badge()    { return state.blocks().size(); }

    @Override
    public UComponent body() { return new BodyPanel(); }

    @Override
    public void onShow() {
        state.addChangeListener(stateListener);
        markDirty();
    }

    @Override
    public void onHide() {
        state.removeChangeListener(stateListener);
    }

    private void markDirty() { dirty = true; }

    private void rebuildIfNeeded() {
        if (!dirty) return;
        dirty = false;
        content.clearChildren();

        List<UUID> ids = new ArrayList<>(state.blocks());
        ids.sort(Comparator.comparing(id -> nameFor(id).toLowerCase()));

        int y = content.getY();
        for (UUID id : ids) {
            BlockEntry row = new BlockEntry(id, nameFor(id), actions);
            row.setBounds(content.getX(), y, content.getWidth(), BlockEntry.ROW_HEIGHT);
            content.addChild(row);
            y += BlockEntry.ROW_HEIGHT;
        }
        int contentH = Math.max(1, y - content.getY());
        content.setBounds(content.getX(), content.getY(), content.getWidth(), contentH);
    }

    private String nameFor(UUID id) {
        Friend f = state.friends().get(id);
        if (f != null) return f.name;
        return id.toString().substring(0, 8);
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
        }

        @Override
        public void render(URenderer r) {
            rebuildIfNeeded();
            boolean any = !state.blocks().isEmpty();
            scroll.setVisible(any);
            empty.setVisible(!any);
            super.render(r);
        }
    }

    static final class BlockEntry extends UComponent {
        static final int ROW_HEIGHT = 32;
        private static final int PAD_X = 10;

        private final UUID profileId;
        private final String name;
        private final UButton unblockBtn;

        BlockEntry(UUID profileId, String name, Actions actions) {
            this.profileId = profileId;
            this.name = name;
            this.unblockBtn = new UButton("Unblock", () -> actions.unblock(profileId)).setStyle(UButton.Style.SUBTLE);
        }

        @Override
        protected void onLayout() {
            int btnW = 76;
            int btnH = 20;
            unblockBtn.setBounds(x + width - PAD_X - btnW, y + (height - btnH) / 2, btnW, btnH);
        }

        @Override
        public void render(URenderer r) {
            if (!visible) return;
            if (hovered) r.fillRect(x, y, width, height, UTheme.SURFACE_ALT);
            int textY = y + (height - r.textHeight()) / 2;
            r.drawText(x + PAD_X, textY, name, UTheme.TEXT);
            unblockBtn.tickHover(r.currentMouseX(), r.currentMouseY());
            unblockBtn.render(r);
            r.fillRect(x + PAD_X, y + height - 1, width - PAD_X * 2, 1, UTheme.BORDER);
        }

        @Override
        public boolean mouseClick(double mouseX, double mouseY, int button) {
            return unblockBtn.mouseClick(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseRelease(double mouseX, double mouseY, int button) {
            return unblockBtn.mouseRelease(mouseX, mouseY, button);
        }
    }

    private static final class EmptyState extends UPanel {
        EmptyState() { setBackground(Background.NONE); }

        @Override
        public void render(URenderer r) {
            if (!isVisible()) return;
            int textY = y + height / 2 - r.textHeight() / 2;
            r.drawTextCentered(x, textY, width, "No blocked users.", UTheme.TEXT_DIM);
        }
    }
}
