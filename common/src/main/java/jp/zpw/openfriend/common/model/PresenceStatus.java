/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.model;

public enum PresenceStatus {
    UNKNOWN,
    ONLINE,
    PLAYING_OFFLINE,
    PLAYING_REALMS,
    PLAYING_SERVER,
    PLAYING_HOSTED_SERVER,
    OFFLINE;

    public static PresenceStatus parse(String s) {
        if (s == null) return UNKNOWN;
        try {
            return PresenceStatus.valueOf(s);
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    public boolean isHosting()    { return this == PLAYING_HOSTED_SERVER; }
    public boolean isOnline()     { return this == ONLINE || isPlaying(); }
    public boolean isPlaying()    {
        switch (this) {
            case PLAYING_OFFLINE:
            case PLAYING_REALMS:
            case PLAYING_SERVER:
            case PLAYING_HOSTED_SERVER:
                return true;
            default:
                return false;
        }
    }
}
