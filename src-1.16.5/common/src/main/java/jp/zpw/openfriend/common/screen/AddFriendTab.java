/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.screen;

import jp.zpw.openfriend.common.ui.UButton;
import jp.zpw.openfriend.common.ui.UComponent;
import jp.zpw.openfriend.common.ui.UInput;
import jp.zpw.openfriend.common.ui.UPanel;
import jp.zpw.openfriend.common.ui.URenderer;
import jp.zpw.openfriend.common.ui.UTheme;

import java.util.function.Consumer;

public final class AddFriendTab implements FriendsOverlayScreen.Tab {

    public enum State { IDLE, SEARCHING, FOUND, ALREADY_FRIEND, INCOMING_EXISTS, OUTGOING_EXISTS, NOT_FOUND, SENT, ERROR }

    public interface Actions {
        void search(String name, Consumer<SearchResult> callback);
        void add(String name, Consumer<Boolean> ok);
    }

    public static final class SearchResult {
        public final State state;
        public final String name;
        public final String errorMessage;

        public SearchResult(State s, String name, String err) {
            this.state = s;
            this.name = name == null ? "" : name;
            this.errorMessage = err == null ? "" : err;
        }
    }

    private final Actions actions;
    private final UInput input = new UInput();
    private final UButton submit;
    private final UButton primary;

    private State state = State.IDLE;
    private String lastQuery = "";
    private String message = "";

    public AddFriendTab(Actions actions) {
        this.actions = actions;
        input.setPlaceholder("Gamertag");
        input.setMaxLength(16);
        input.setOnSubmit(this::triggerSearch);
        submit = new UButton("Search", () -> triggerSearch(input.getText())).setStyle(UButton.Style.PRIMARY);
        primary = new UButton("Send request", this::triggerAdd).setStyle(UButton.Style.PRIMARY);
        primary.setVisible(false);
    }

    @Override public String id()    { return "add"; }
    @Override public String label() { return "Add"; }
    @Override public int badge()    { return 0; }

    @Override
    public UComponent body() {
        return new BodyPanel();
    }

    private void setState(State s, String msg) {
        this.state = s;
        this.message = msg == null ? "" : msg;
        primary.setVisible(s == State.FOUND);
    }

    private void triggerSearch(String q) {
        String query = q == null ? "" : q.trim();
        if (query.isEmpty()) {
            setState(State.IDLE, "");
            return;
        }
        lastQuery = query;
        setState(State.SEARCHING, "Searching " + query + "…");
        actions.search(query, this::onSearchResult);
    }

    private void onSearchResult(SearchResult result) {
        if (result == null) {
            setState(State.ERROR, "Search failed.");
            return;
        }
        switch (result.state) {
            case FOUND:            setState(State.FOUND,            result.name); break;
            case ALREADY_FRIEND:   setState(State.ALREADY_FRIEND,   result.name + " is already a friend."); break;
            case INCOMING_EXISTS:  setState(State.INCOMING_EXISTS,  result.name + " already sent you a request."); break;
            case OUTGOING_EXISTS:  setState(State.OUTGOING_EXISTS,  "You already sent " + result.name + " a request."); break;
            case NOT_FOUND:        setState(State.NOT_FOUND,        "No player named " + lastQuery + "."); break;
            case ERROR:            setState(State.ERROR,            result.errorMessage); break;
            default:               setState(State.IDLE,             ""); break;
        }
    }

    private void triggerAdd() {
        if (state != State.FOUND || lastQuery.isEmpty()) return;
        String target = lastQuery;
        setState(State.SEARCHING, "Sending request to " + target + "…");
        actions.add(target, ok -> {
            if (Boolean.TRUE.equals(ok)) {
                setState(State.SENT, "Request sent to " + target + ".");
                input.setText("");
                lastQuery = "";
            } else {
                setState(State.ERROR, "Could not send request to " + target + ".");
            }
        });
    }

    private int messageColor() {
        switch (state) {
            case FOUND:           return UTheme.ACCENT_DIM;
            case SENT:            return UTheme.ONLINE;
            case NOT_FOUND:
            case ERROR:           return UTheme.DANGER;
            case ALREADY_FRIEND:
            case INCOMING_EXISTS:
            case OUTGOING_EXISTS: return UTheme.WARN;
            default:              return UTheme.TEXT_DIM;
        }
    }

    private final class BodyPanel extends UPanel {
        BodyPanel() {
            setBackground(Background.NONE);
            setPadding(16);
            addChild(input);
            addChild(submit);
            addChild(primary);
        }

        @Override
        protected void onLayout() {
            int inputH = 22;
            int btnW = 70;
            int gap = 8;
            int row1Y = y + padding();
            input.setBounds(x + padding(), row1Y, width - padding() * 2 - btnW - gap, inputH);
            submit.setBounds(x + width - padding() - btnW, row1Y, btnW, inputH);
            primary.setBounds(x + width - padding() - 110, y + padding() + inputH + 48, 110, 22);
        }

        @Override
        public void render(URenderer r) {
            super.render(r);
            int textY = y + padding() + 22 + 16;
            String msg = message.isEmpty() ? "Search by gamertag to send a friend request." : message;
            r.drawTextCentered(x + padding(), textY, width - padding() * 2, msg, messageColor());
        }
    }
}
