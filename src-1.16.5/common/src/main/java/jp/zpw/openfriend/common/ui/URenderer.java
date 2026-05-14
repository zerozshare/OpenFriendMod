/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.ui;

public interface URenderer {

    void fillRect(int x, int y, int width, int height, int argb);

    void strokeRect(int x, int y, int width, int height, int argb);

    void fillRoundedRect(int x, int y, int width, int height, int radius, int argb);

    void drawText(int x, int y, String text, int argb);

    void drawTextCentered(int x, int y, int width, String text, int argb);

    void drawTextRight(int x, int y, int width, String text, int argb);

    void drawTextClipped(int x, int y, int maxWidth, String text, int argb);

    String translate(String key);

    int textWidth(String text);

    int textHeight();

    void pushClip(int x, int y, int width, int height);

    void popClip();

    void pushTranslate(int dx, int dy);

    void popTranslate();

    void drawHead(int x, int y, int size, String profileId);

    int currentMouseX();

    int currentMouseY();

    float partialTick();
}
