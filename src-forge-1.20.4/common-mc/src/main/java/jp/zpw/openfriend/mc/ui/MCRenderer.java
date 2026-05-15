/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.mc.ui;

import com.mojang.authlib.GameProfile;
import jp.zpw.openfriend.common.ui.URenderer;
import jp.zpw.openfriend.common.ui.UTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public final class MCRenderer implements URenderer {

    private final Font font;
    private GuiGraphics graphics;
    private int mouseX;
    private int mouseY;
    private float partialTick;
    private int clipDepth;

    public MCRenderer(Font font) {
        this.font = font;
    }

    public void beginFrame(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.graphics = g;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.partialTick = partialTick;
        this.clipDepth = 0;
    }

    public void endFrame() {
        while (clipDepth > 0 && graphics != null) {
            graphics.disableScissor();
            clipDepth--;
        }
        graphics = null;
    }

    @Override
    public void fillRect(int x, int y, int width, int height, int argb) {
        if (graphics == null) return;
        graphics.fill(x, y, x + width, y + height, argb);
    }

    @Override
    public void strokeRect(int x, int y, int width, int height, int argb) {
        if (graphics == null) return;
        graphics.fill(x,             y,             x + width, y + 1,         argb);
        graphics.fill(x,             y + height - 1, x + width, y + height,   argb);
        graphics.fill(x,             y,             x + 1,     y + height,   argb);
        graphics.fill(x + width - 1, y,             x + width, y + height,   argb);
    }

    @Override
    public void fillRoundedRect(int x, int y, int width, int height, int radius, int argb) {
        fillRect(x, y, width, height, argb);
    }

    @Override
    public void drawText(int x, int y, String text, int argb) {
        if (graphics == null || text == null) return;
        graphics.drawString(font, Component.literal(text), x, y, argb, false);
    }

    @Override
    public void drawTextCentered(int x, int y, int width, String text, int argb) {
        if (graphics == null || text == null) return;
        int tw = font.width(text);
        int tx = x + (width - tw) / 2;
        graphics.drawString(font, Component.literal(text), tx, y, argb, false);
    }

    @Override
    public void drawTextRight(int x, int y, int width, String text, int argb) {
        if (graphics == null || text == null) return;
        int tw = font.width(text);
        int tx = x + width - tw;
        graphics.drawString(font, Component.literal(text), tx, y, argb, false);
    }

    @Override
    public void drawTextClipped(int x, int y, int maxWidth, String text, int argb) {
        if (graphics == null || text == null) return;
        String shown = text;
        if (font.width(shown) > maxWidth) {
            String ell = "...";
            int ellW = font.width(ell);
            int budget = Math.max(0, maxWidth - ellW);
            StringBuilder b = new StringBuilder();
            int acc = 0;
            for (int i = 0; i < text.length(); i++) {
                int cw = font.width(String.valueOf(text.charAt(i)));
                if (acc + cw > budget) break;
                b.append(text.charAt(i));
                acc += cw;
            }
            shown = b + ell;
        }
        graphics.drawString(font, Component.literal(shown), x, y, argb, false);
    }

    @Override
    public String translate(String key) {
        if (key == null) return "";
        return I18n.get(key);
    }

    @Override
    public int textWidth(String text) {
        return text == null ? 0 : font.width(text);
    }

    @Override
    public int textHeight() {
        return font.lineHeight;
    }

    @Override
    public void pushClip(int x, int y, int width, int height) {
        if (graphics == null) return;
        int x1 = Math.max(x, x + width);
        int y1 = Math.max(y, y + height);
        graphics.enableScissor(x, y, x1, y1);
        clipDepth++;
    }

    @Override
    public void popClip() {
        if (graphics == null || clipDepth == 0) return;
        graphics.disableScissor();
        clipDepth--;
    }

    @Override
    public void pushTranslate(int dx, int dy) {
        if (graphics == null) return;
        graphics.pose().pushPose();
        graphics.pose().translate((float) dx, (float) dy, 0f);
    }

    @Override
    public void popTranslate() {
        if (graphics == null) return;
        graphics.pose().popPose();
    }

    @Override
    public void drawHead(int x, int y, int size, String profileId) {
        if (graphics == null) return;
        ResourceLocation tex = null;
        try {
            if (profileId != null && !profileId.isEmpty()) {
                UUID uuid = UUID.fromString(profileId);
                Minecraft mc = Minecraft.getInstance();
                if (mc != null) {
                    PlayerSkin skin = mc.getSkinManager().getInsecureSkin(new GameProfile(uuid, ""));
                    if (skin != null) tex = skin.texture();
                }
            }
        } catch (Throwable ignored) {}

        if (tex == null) {
            int seed = profileId == null ? 0 : profileId.hashCode();
            int tone = UTheme.BG | (((seed >> 16) & 0x7F) + 0x40) << 16
                                 | (((seed >> 8)  & 0x7F) + 0x40) << 8
                                 | ( (seed        & 0x7F) + 0x40);
            fillRect(x, y, size, size, tone);
            strokeRect(x, y, size, size, UTheme.BORDER);
            return;
        }

        graphics.blit(tex, x, y, size, size, 8f,  8f, 8, 8, 64, 64);
        graphics.blit(tex, x, y, size, size, 40f, 8f, 8, 8, 64, 64);
    }

    @Override
    public int currentMouseX() { return mouseX; }

    @Override
    public int currentMouseY() { return mouseY; }

    @Override
    public float partialTick() { return partialTick; }
}
