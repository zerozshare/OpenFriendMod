/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.state;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jp.zpw.openfriend.common.ipc.IpcClient;
import jp.zpw.openfriend.common.ipc.IpcListener;
import jp.zpw.openfriend.common.model.Friend;
import jp.zpw.openfriend.common.model.PresenceStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class FriendsState implements IpcListener {

    public static final class HostInfo {
        public boolean running;
        public String target = "";
        public boolean useBypass;
    }

    public static final class JoinInfo {
        public boolean running;
        public String peer = "";
        public String pmid = "";
        public String listen = "";
    }

    public static final class AuthInfo {
        public boolean authenticated;
        public UUID profileId;
        public String name = "";
    }

    private final Map<UUID, Friend> friends = new LinkedHashMap<>();
    private final Map<UUID, Friend> incoming = new LinkedHashMap<>();
    private final Map<UUID, Friend> outgoing = new LinkedHashMap<>();
    private final Map<UUID, PresenceStatus> presence = new HashMap<>();
    private final Set<UUID> blocks = new HashSet<>();
    private final HostInfo host = new HostInfo();
    private final JoinInfo join = new JoinInfo();
    private final AuthInfo auth = new AuthInfo();

    private final List<Runnable> listeners = new ArrayList<>();
    private final Object listenersLock = new Object();

    public Map<UUID, Friend> friends()  { return Collections.unmodifiableMap(friends); }
    public Map<UUID, Friend> incoming() { return Collections.unmodifiableMap(incoming); }
    public Map<UUID, Friend> outgoing() { return Collections.unmodifiableMap(outgoing); }
    public Set<UUID> blocks()           { return Collections.unmodifiableSet(blocks); }
    public HostInfo host()              { return host; }
    public JoinInfo join()              { return join; }
    public AuthInfo auth()              { return auth; }

    public PresenceStatus presenceOf(UUID profileId) {
        return presence.getOrDefault(profileId, PresenceStatus.UNKNOWN);
    }

    public void addChangeListener(Runnable r) {
        synchronized (listenersLock) {
            if (!listeners.contains(r)) listeners.add(r);
        }
    }

    public void removeChangeListener(Runnable r) {
        synchronized (listenersLock) {
            listeners.remove(r);
        }
    }

    private void fireChange() {
        List<Runnable> snapshot;
        synchronized (listenersLock) {
            snapshot = new ArrayList<>(listeners);
        }
        for (Runnable r : snapshot) {
            try { r.run(); } catch (Throwable ignored) {}
        }
    }

    public void removePending(UUID profileId) {
        boolean changed;
        synchronized (this) {
            boolean a = incoming.remove(profileId) != null;
            boolean b = outgoing.remove(profileId) != null;
            changed = a || b;
        }
        if (changed) fireChange();
    }

    public void primeFromList(IpcClient ipc) {
        java.util.logging.Logger log = java.util.logging.Logger.getLogger("openfriend.state");
        ipc.requestAsync("friends.list", null).whenComplete((r, err) -> {
            if (err != null) {
                log.warning("friends.list failed: " + err.getMessage());
            } else if (r != null) {
                int f = r.has("friends") ? r.getAsJsonArray("friends").size() : 0;
                log.info("friends.list ok: " + f + " friends");
                applyFriendsListResult(r);
            }
        });
        ipc.requestAsync("blocks.list", null).whenComplete((r, err) -> {
            if (err != null) log.warning("blocks.list failed: " + err.getMessage());
            else if (r != null) applyBlocksListResult(r);
        });
        ipc.requestAsync("auth.status", null).whenComplete((r, err) -> {
            if (err != null) log.warning("auth.status failed: " + err.getMessage());
            else if (r != null) applyAuthStatusResult(r);
        });
        ipc.requestAsync("host.status", null).whenComplete((r, err) -> {
            if (err != null) log.warning("host.status failed: " + err.getMessage());
            else if (r != null) applyHostStatusResult(r);
        });
    }

    public void applyFriendsListResult(JsonObject result) {
        synchronized (this) {
            friends.clear();
            incoming.clear();
            outgoing.clear();
            consumeFriendArray(result.getAsJsonArray("friends"), friends);
            consumeFriendArray(result.getAsJsonArray("incoming"), incoming);
            consumeFriendArray(result.getAsJsonArray("outgoing"), outgoing);
        }
        fireChange();
    }

    public void applyBlocksListResult(JsonObject result) {
        synchronized (this) {
            blocks.clear();
            JsonArray arr = result.getAsJsonArray("blocks");
            if (arr != null) {
                for (JsonElement e : arr) {
                    if (!e.isJsonObject()) continue;
                    UUID id = parseUuid(e.getAsJsonObject().get("profileId"));
                    if (id != null) blocks.add(id);
                }
            }
        }
        fireChange();
    }

    public void applyAuthStatusResult(JsonObject result) {
        synchronized (this) {
            auth.authenticated = result.has("authenticated") && result.get("authenticated").getAsBoolean();
            auth.profileId = auth.authenticated ? parseUuid(result.get("profileId")) : null;
            auth.name = result.has("name") ? result.get("name").getAsString() : "";
        }
        fireChange();
    }

    public void applyHostStatusResult(JsonObject result) {
        synchronized (this) {
            host.running = result.has("running") && result.get("running").getAsBoolean();
            host.target = result.has("target") ? result.get("target").getAsString() : "";
            host.useBypass = result.has("useBypass") && result.get("useBypass").getAsBoolean();
        }
        fireChange();
    }

    @Override
    public void onNotification(String method, JsonObject params) {
        boolean changed = true;
        synchronized (this) {
            switch (method) {
                case "friends.snapshot":
                    friends.clear(); incoming.clear(); outgoing.clear();
                    consumeFriendArray(params.getAsJsonArray("friends"), friends);
                    consumeFriendArray(params.getAsJsonArray("incoming"), incoming);
                    consumeFriendArray(params.getAsJsonArray("outgoing"), outgoing);
                    break;
                case "friend.added":
                    putFriend(params, friends);
                    incoming.remove(parseUuid(params.get("profileId")));
                    outgoing.remove(parseUuid(params.get("profileId")));
                    break;
                case "friend.removed":
                    friends.remove(parseUuid(params.get("profileId")));
                    break;
                case "friend.requestIncoming":
                    putFriend(params, incoming);
                    break;
                case "friend.requestIncomingResolved":
                    incoming.remove(parseUuid(params.get("profileId")));
                    break;
                case "friend.requestOutgoing":
                    putFriend(params, outgoing);
                    break;
                case "friend.requestOutgoingResolved":
                    outgoing.remove(parseUuid(params.get("profileId")));
                    break;
                case "presence.changed": {
                    UUID id = parseUuid(params.get("profileId"));
                    if (id != null) {
                        String s = params.has("status") ? params.get("status").getAsString() : null;
                        presence.put(id, PresenceStatus.parse(s));
                    } else {
                        changed = false;
                    }
                    break;
                }
                case "auth.signedIn":
                    auth.authenticated = true;
                    auth.profileId = parseUuid(params.get("profileId"));
                    auth.name = params.has("name") ? params.get("name").getAsString() : "";
                    break;
                case "host.started":
                    host.running = true;
                    host.target = params.has("target") ? params.get("target").getAsString() : "";
                    host.useBypass = params.has("useBypass") && params.get("useBypass").getAsBoolean();
                    break;
                case "host.stopped":
                    host.running = false;
                    host.target = "";
                    host.useBypass = false;
                    break;
                case "join.started":
                    join.running = true;
                    join.peer = params.has("peer") ? params.get("peer").getAsString() : "";
                    join.pmid = params.has("pmid") ? params.get("pmid").getAsString() : "";
                    join.listen = params.has("listen") ? params.get("listen").getAsString() : "";
                    break;
                case "join.stopped":
                    join.running = false;
                    join.peer = "";
                    join.pmid = "";
                    join.listen = "";
                    break;
                default:
                    changed = false;
            }
        }
        if (changed) fireChange();
    }

    private void consumeFriendArray(JsonArray arr, Map<UUID, Friend> target) {
        if (arr == null) return;
        for (JsonElement e : arr) {
            if (!e.isJsonObject()) continue;
            JsonObject o = e.getAsJsonObject();
            UUID id = parseUuid(o.get("profileId"));
            if (id == null) continue;
            String name = o.has("name") ? o.get("name").getAsString() : "";
            target.put(id, new Friend(id, name));
        }
    }

    private void putFriend(JsonObject o, Map<UUID, Friend> target) {
        UUID id = parseUuid(o.get("profileId"));
        if (id == null) return;
        String name = o.has("name") ? o.get("name").getAsString() : "";
        target.put(id, new Friend(id, name));
    }

    private static UUID parseUuid(JsonElement e) {
        if (e == null || e.isJsonNull()) return null;
        try { return UUID.fromString(e.getAsString()); }
        catch (Exception ex) { return null; }
    }
}
