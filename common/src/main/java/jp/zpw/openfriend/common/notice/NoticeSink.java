/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.notice;

public interface NoticeSink {
    enum Level { INFO, SUCCESS, WARN, ERROR }

    default void show(Level level, String title, String body) {}

    default void info(String title, String body)    { show(Level.INFO, title, body); }
    default void success(String title, String body) { show(Level.SUCCESS, title, body); }
    default void warn(String title, String body)    { show(Level.WARN, title, body); }
    default void error(String title, String body)   { show(Level.ERROR, title, body); }
}
