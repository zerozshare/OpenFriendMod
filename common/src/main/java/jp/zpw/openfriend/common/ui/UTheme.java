/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.ui;

import jp.zpw.openfriend.common.notice.NoticeSink;

public final class UTheme {

    public static final int TRANSPARENT  = 0x00000000;

    public static final int BG           = 0xFF000000;
    public static final int SURFACE      = 0xFF0A0A0A;
    public static final int SURFACE_ALT  = 0xFF111111;
    public static final int BORDER       = 0xFF1A1A1A;
    public static final int BORDER_HOV   = 0xFF2A2A2A;

    public static final int TEXT         = 0xFFFFFFFF;
    public static final int TEXT_DIM     = 0xFF777777;
    public static final int TEXT_FAINT   = 0xFF444444;
    public static final int TEXT_ON_LIGHT = 0xFF000000;

    public static final int ACCENT       = 0xFFFFCC2E;
    public static final int ACCENT_DIM   = 0xFFE5B520;
    public static final int ACCENT_BLUE  = 0xFF4F8FFF;
    public static final int ACCENT_CYAN  = 0xFF7BC8FF;

    public static final int ONLINE       = 0xFF6CD27A;
    public static final int PLAYING      = 0xFFFFCC2E;
    public static final int OFFLINE      = 0xFF555555;

    public static final int WARN         = 0xFFF5B35A;
    public static final int DANGER       = 0xFFE05656;

    public static final int OVERLAY_BG   = 0xC8000000;
    public static final int DIM_OVERLAY  = 0xC0000000;
    public static final int TOAST_BG     = 0xF00A0A0A;
    public static final int HOVER_GHOST  = 0x40FFFFFF;
    public static final int TOAST_BODY   = 0xFFCCCCCC;
    public static final int TOAST_CLOSE  = 0xFFAAAAAA;

    public static int noticeBorder(NoticeSink.Level level) {
        if (level == null) return ACCENT;
        switch (level) {
            case ERROR:   return DANGER;
            case WARN:    return WARN;
            case SUCCESS: return ONLINE;
            default:      return ACCENT;
        }
    }

    public static int noticeTitle(NoticeSink.Level level) {
        if (level == null) return TEXT;
        switch (level) {
            case ERROR:   return DANGER;
            case WARN:    return WARN;
            case SUCCESS: return ONLINE;
            default:      return TEXT;
        }
    }

    private UTheme() {}
}
