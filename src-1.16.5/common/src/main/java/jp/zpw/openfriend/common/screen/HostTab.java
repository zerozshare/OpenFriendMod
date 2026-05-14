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

public final class HostTab implements FriendsOverlayScreen.Tab {

    public enum Mode { BRIDGE, LOCAL_PUBLISH }

    public interface Actions {
        void startBridge(String target, boolean useBypass);
        void startLocalPublish(boolean useBypass);
        void stop();
    }

    private final FriendsState state;
    private final Actions actions;

    private final UButton modeBridge;
    private final UButton modeLocal;
    private final UInput targetInput = new UInput();
    private final UButton bypassToggle;
    private final UButton primary;

    private Mode mode = Mode.BRIDGE;
    private boolean useBypass = false;

    private final Runnable stateListener = this::refreshButtons;

    public HostTab(FriendsState state, Actions actions) {
        this.state = state;
        this.actions = actions;

        targetInput.setPlaceholder("host:port (e.g. 127.0.0.1:25565)");
        targetInput.setMaxLength(64);

        modeBridge = new UButton("Bridge", () -> setMode(Mode.BRIDGE));
        modeLocal = new UButton("Local world", () -> setMode(Mode.LOCAL_PUBLISH));
        bypassToggle = new UButton("Bypass: off", this::toggleBypass).setStyle(UButton.Style.SUBTLE);
        primary = new UButton("Start hosting", this::onPrimary).setStyle(UButton.Style.PRIMARY);

        refreshButtons();
    }

    @Override public String id()    { return "host"; }
    @Override public String label() { return "Host"; }
    @Override public int badge()    { return state.host().running ? 1 : 0; }

    @Override
    public UComponent body() { return new BodyPanel(); }

    @Override
    public void onShow() {
        state.addChangeListener(stateListener);
        refreshButtons();
    }

    @Override
    public void onHide() {
        state.removeChangeListener(stateListener);
    }

    private void setMode(Mode m) {
        this.mode = m;
        refreshButtons();
    }

    private void toggleBypass() {
        useBypass = !useBypass;
        refreshButtons();
    }

    private void refreshButtons() {
        modeBridge.setStyle(mode == Mode.BRIDGE ? UButton.Style.PRIMARY : UButton.Style.SUBTLE);
        modeLocal.setStyle(mode == Mode.LOCAL_PUBLISH ? UButton.Style.PRIMARY : UButton.Style.SUBTLE);
        bypassToggle.setLabel(useBypass ? "Bypass: on" : "Bypass: off");
        boolean running = state.host().running;
        primary.setLabel(running ? "Stop hosting" : "Start hosting");
        primary.setStyle(running ? UButton.Style.DANGER : UButton.Style.PRIMARY);
        targetInput.setEnabled(!running && mode == Mode.BRIDGE);
        modeBridge.setEnabled(!running);
        modeLocal.setEnabled(!running);
        bypassToggle.setEnabled(!running);
    }

    private void onPrimary() {
        if (state.host().running) {
            actions.stop();
            return;
        }
        if (mode == Mode.BRIDGE) {
            String target = targetInput.getText().trim();
            if (target.isEmpty()) return;
            actions.startBridge(target, useBypass);
        } else {
            actions.startLocalPublish(useBypass);
        }
    }

    private final class BodyPanel extends UPanel {
        BodyPanel() {
            setBackground(Background.NONE);
            setPadding(16);
            addChild(modeBridge);
            addChild(modeLocal);
            addChild(targetInput);
            addChild(bypassToggle);
            addChild(primary);
        }

        @Override
        protected void onLayout() {
            int innerX = x + padding();
            int innerW = width - padding() * 2;
            int row = y + padding();

            int modeBtnW = (innerW - 8) / 2;
            modeBridge.setBounds(innerX, row, modeBtnW, 24);
            modeLocal.setBounds(innerX + modeBtnW + 8, row, innerW - modeBtnW - 8, 24);
            row += 24 + 12;

            targetInput.setBounds(innerX, row, innerW, 22);
            row += 22 + 12;

            bypassToggle.setBounds(innerX, row, 110, 22);
            row += 22 + 14;

            primary.setBounds(innerX + innerW - 130, row, 130, 24);
        }

        @Override
        public void render(URenderer r) {
            super.render(r);
            int hintY = y + height - r.textHeight() - 12;
            String status;
            int color;
            if (state.host().running) {
                status = "Hosting → " + state.host().target;
                color = UTheme.ONLINE;
            } else if (mode == Mode.LOCAL_PUBLISH) {
                status = "Opens your current single-player world to LAN, then bridges Friends joins to it.";
                color = UTheme.TEXT_DIM;
            } else {
                status = "Bridges Friends List joins to any TCP Minecraft server.";
                color = UTheme.TEXT_DIM;
            }
            r.drawTextCentered(x + padding(), hintY, width - padding() * 2, status, color);
        }
    }
}
