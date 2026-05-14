/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.screen;

import jp.zpw.openfriend.common.state.FriendsState;
import jp.zpw.openfriend.common.ui.UButton;
import jp.zpw.openfriend.common.ui.UComponent;
import jp.zpw.openfriend.common.ui.UInput;
import jp.zpw.openfriend.common.ui.UPanel;
import jp.zpw.openfriend.common.ui.URenderer;
import jp.zpw.openfriend.common.ui.UTheme;

public final class MultiplayTab implements FriendsOverlayScreen.Tab {

    public interface Actions {
        boolean canHost();
        boolean isHosting();
        boolean publishToFriends(FriendsController.GameMode mode, boolean allowCheats, int maxPlayers);
        void stopHosting();
    }

    private final FriendsState state;
    private final Actions actions;

    private final Runnable stateListener = () -> {};

    private FriendsController.GameMode mode = FriendsController.GameMode.SURVIVAL;
    private boolean allowCheats = false;
    private final UInput maxPlayersInput = new UInput();

    public MultiplayTab(FriendsState state, Actions actions) {
        this.state = state;
        this.actions = actions;
        this.maxPlayersInput
                .setPlaceholder("8")
                .setMaxLength(3)
                .setDigitsOnly(true)
                .setText("8");
    }

    @Override public String id()    { return "multiplay"; }
    @Override public String label() { return "Multiplay"; }
    @Override public int badge()    { return state.host().running ? 1 : 0; }

    @Override
    public UComponent body() { return new BodyPanel(); }

    @Override
    public void onShow() {
        state.addChangeListener(stateListener);
    }

    @Override
    public void onHide() {
        state.removeChangeListener(stateListener);
    }

    private static String modeKey(FriendsController.GameMode m) {
        switch (m) {
            case CREATIVE:  return "selectWorld.gameMode.creative";
            case ADVENTURE: return "selectWorld.gameMode.adventure";
            case SPECTATOR: return "selectWorld.gameMode.spectator";
            case SURVIVAL:
            default:        return "selectWorld.gameMode.survival";
        }
    }

    private int parseMaxPlayers() {
        try {
            int n = Integer.parseInt(maxPlayersInput.getText().trim());
            if (n < 1)   return 1;
            if (n > 250) return 250;
            return n;
        } catch (NumberFormatException e) {
            return 8;
        }
    }

    private final class BodyPanel extends UPanel {
        private final UButton modeBtn;
        private final UButton cheatsBtn;
        private final UButton primary;

        BodyPanel() {
            setBackground(Background.NONE);
            setPadding(16);
            modeBtn   = new UButton("", this::cycleMode).setStyle(UButton.Style.SUBTLE);
            cheatsBtn = new UButton("", this::toggleCheats).setStyle(UButton.Style.SUBTLE);
            primary   = new UButton("",  this::onPrimary).setStyle(UButton.Style.PRIMARY);
            addChild(modeBtn);
            addChild(cheatsBtn);
            addChild(maxPlayersInput);
            addChild(primary);
        }

        private void cycleMode() {
            FriendsController.GameMode[] all = FriendsController.GameMode.values();
            mode = all[(mode.ordinal() + 1) % all.length];
        }

        private void toggleCheats() {
            allowCheats = !allowCheats;
        }

        private void onPrimary() {
            if (state.host().running) {
                actions.stopHosting();
            } else if (actions.canHost()) {
                actions.publishToFriends(mode, allowCheats, parseMaxPlayers());
            }
        }

        @Override
        protected void onLayout() {
            int rowW = Math.min(220, width - padding() * 2);
            int rowH = 20;
            int gap  = 6;
            int cx = x + width / 2;
            int totalH = rowH * 4 + gap * 3;
            int top = y + (height - totalH) / 2 + 8;

            modeBtn.setBounds  (cx - rowW / 2, top,                rowW, rowH);
            cheatsBtn.setBounds(cx - rowW / 2, top + rowH + gap,   rowW, rowH);

            int labelW = 110;
            int inputW = rowW - labelW;
            maxPlayersInput.setBounds(cx - rowW / 2 + labelW, top + (rowH + gap) * 2, inputW, rowH);

            primary.setBounds  (cx - rowW / 2, top + (rowH + gap) * 3, rowW, rowH);
        }

        @Override
        public void render(URenderer r) {
            boolean running = state.host().running;
            boolean inGame  = actions.canHost();

            String onOff = allowCheats ? r.translate("options.on") : r.translate("options.off");
            modeBtn.setLabel(r.translate("selectWorld.gameMode") + ": " + r.translate(modeKey(mode)));
            cheatsBtn.setLabel(r.translate("selectWorld.allowCommands") + ": " + onOff);
            modeBtn.setEnabled(!running && inGame);
            cheatsBtn.setEnabled(!running && inGame);
            maxPlayersInput.setEnabled(!running && inGame);

            if (running) {
                primary.setLabel("Stop hosting").setStyle(UButton.Style.DANGER).setEnabled(true);
            } else if (inGame) {
                primary.setLabel("Open to Friends").setStyle(UButton.Style.PRIMARY).setEnabled(true);
            } else {
                primary.setLabel("Open to Friends").setStyle(UButton.Style.SUBTLE).setEnabled(false);
            }

            super.render(r);

            int rowW = Math.min(220, width - padding() * 2);
            int rowH = 20;
            int gap  = 6;
            int cx = x + width / 2;
            int totalH = rowH * 4 + gap * 3;
            int top = y + (height - totalH) / 2 + 8;
            int labelW = 110;
            int labelY = top + (rowH + gap) * 2 + (rowH - r.textHeight()) / 2;
            r.drawText(cx - rowW / 2 + 8, labelY, "Max Players:", UTheme.TEXT);

            String line1;
            String line2;
            int color;
            if (running) {
                line1 = "Hosting -> " + state.host().target;
                line2 = "Friends can join from their Friends list.";
                color = UTheme.ONLINE;
            } else if (inGame) {
                line1 = "Share your single-player world with friends.";
                line2 = "They will see it on their Friends list and can join.";
                color = UTheme.TEXT_DIM;
            } else {
                line1 = "Load a single-player world first.";
                line2 = "Then open this tab to share it with friends.";
                color = UTheme.TEXT_DIM;
            }
            int textW = width - padding() * 2;
            int textX = x + padding();
            int textY = y + padding();
            r.drawTextCentered(textX, textY,                      textW, line1, color);
            r.drawTextCentered(textX, textY + r.textHeight() + 4, textW, line2, UTheme.TEXT_FAINT);
        }
    }
}
