/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.model;

import java.util.Objects;
import java.util.UUID;

public final class Friend {
    public final UUID profileId;
    public final String name;

    public Friend(UUID profileId, String name) {
        this.profileId = profileId;
        this.name = name == null ? "" : name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Friend)) return false;
        Friend other = (Friend) o;
        return Objects.equals(profileId, other.profileId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(profileId);
    }
}
