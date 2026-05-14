/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.ui;

public final class UDivider extends UComponent {

    public enum Orientation { HORIZONTAL, VERTICAL }

    private Orientation orientation = Orientation.HORIZONTAL;
    private int color = UTheme.BORDER;

    public UDivider setOrientation(Orientation o) { this.orientation = o; return this; }
    public UDivider setColor(int argb)            { this.color = argb; return this; }

    @Override
    public void render(URenderer r) {
        if (!visible) return;
        if (orientation == Orientation.HORIZONTAL) {
            r.fillRect(x, y, width, 1, color);
        } else {
            r.fillRect(x, y, 1, height, color);
        }
    }
}
