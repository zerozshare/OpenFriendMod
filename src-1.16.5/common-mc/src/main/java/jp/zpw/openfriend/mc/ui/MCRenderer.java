/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 * Variant for group-b (Minecraft 1.19 - 1.19.4): uses PoseStack + GuiComponent.fill
 * + Font.draw instead of GuiGraphics. Scissor clipping is a no-op here because the
 * 1.19 RenderSystem.enableScissor needs framebuffer coordinates, which would
 * require window-scale conversion; the Friends overlay tolerates this — content
 * outside the modal is hidden by the OVERLAY dim layer.
 */
package jp.zpw.openfriend.mc.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import jp.zpw.openfriend.common.ui.URenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public final class MCRenderer implements URenderer {

    private final Font font;
    private PoseStack poseStack;
    private int mouseX;
    private int mouseY;
    private float partialTick;

    public MCRenderer(Font font) {
        this.font = font;
    }

    public void beginFrame(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.poseStack = poseStack;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.partialTick = partialTick;
    }

    public void endFrame() {
        this.poseStack = null;
    }

    @Override
    public void fillRect(int x, int y, int width, int height, int argb) {
        if (poseStack == null) return;
        GuiComponent.fill(poseStack, x, y, x + width, y + height, argb);
    }

    @Override
    public void strokeRect(int x, int y, int width, int height, int argb) {
        if (poseStack == null) return;
        GuiComponent.fill(poseStack, x,             y,              x + width,     y + 1,         argb);
        GuiComponent.fill(poseStack, x,             y + height - 1, x + width,     y + height,    argb);
        GuiComponent.fill(poseStack, x,             y,              x + 1,         y + height,    argb);
        GuiComponent.fill(poseStack, x + width - 1, y,              x + width,     y + height,    argb);
    }

    @Override
    public void fillRoundedRect(int x, int y, int width, int height, int radius, int argb) {
        fillRect(x, y, width, height, argb);
    }

    @Override
    public void drawText(int x, int y, String text, int argb) {
        if (poseStack == null || text == null) return;
        font.draw(poseStack, new TextComponent(text), (float) x, (float) y, argb);
    }

    @Override
    public void drawTextCentered(int x, int y, int width, String text, int argb) {
        if (poseStack == null || text == null) return;
        int tx = x + (width - font.width(text)) / 2;
        font.draw(poseStack, new TextComponent(text), (float) tx, (float) y, argb);
    }

    @Override
    public void drawTextRight(int x, int y, int width, String text, int argb) {
        if (poseStack == null || text == null) return;
        int tx = x + width - font.width(text);
        font.draw(poseStack, new TextComponent(text), (float) tx, (float) y, argb);
    }

    @Override
    public void drawTextClipped(int x, int y, int maxWidth, String text, int argb) {
        if (poseStack == null || text == null) return;
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
        font.draw(poseStack, new TextComponent(shown), (float) x, (float) y, argb);
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
    }

    @Override
    public void popClip() {
    }

    @Override
    public void pushTranslate(int dx, int dy) {
        if (poseStack == null) return;
        poseStack.pushPose();
        poseStack.translate((double) dx, (double) dy, 0d);
    }

    @Override
    public void popTranslate() {
        if (poseStack == null) return;
        poseStack.popPose();
    }

    @Override
    public void drawHead(int x, int y, int size, String profileId) {
        int seed = profileId == null ? 0 : profileId.hashCode();
        int tone = 0xFF000000 | (((seed >> 16) & 0x7F) + 0x40) << 16
                              | (((seed >> 8)  & 0x7F) + 0x40) << 8
                              | ( (seed        & 0x7F) + 0x40);
        fillRect(x, y, size, size, tone);
        strokeRect(x, y, size, size, 0xFF1A1A1A);
    }

    @Override
    public int currentMouseX() { return mouseX; }

    @Override
    public int currentMouseY() { return mouseY; }

    @Override
    public float partialTick() { return partialTick; }
}
