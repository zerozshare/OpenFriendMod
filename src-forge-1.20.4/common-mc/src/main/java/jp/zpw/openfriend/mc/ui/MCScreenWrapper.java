/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.mc.ui;

import jp.zpw.openfriend.common.screen.FriendsOverlayScreen;
import jp.zpw.openfriend.mc.OpenFriendMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MCScreenWrapper extends Screen {

    private final FriendsOverlayScreen overlay;
    private MCRenderer renderer;
    private boolean renderFault;

    public MCScreenWrapper(FriendsOverlayScreen overlay) {
        super(Component.literal("OpenFriend"));
        this.overlay = overlay;
    }

    @Override
    protected void init() {
        try {
            super.init();
            this.renderer = new MCRenderer(this.font);
            overlay.layout(this.width, this.height);
        } catch (Throwable t) {
            OpenFriendMod.LOG.error("OpenFriend overlay init failed", t);
            this.renderFault = true;
        }
    }

    @Override
    public void resize(Minecraft mc, int w, int h) {
        try {
            super.resize(mc, w, h);
            overlay.layout(w, h);
        } catch (Throwable t) {
            OpenFriendMod.LOG.error("OpenFriend overlay resize failed", t);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        if (renderFault) {
            renderFallback(g);
            return;
        }
        if (renderer == null) renderer = new MCRenderer(this.font);
        renderer.beginFrame(g, mouseX, mouseY, partialTick);
        try {
            overlay.render(renderer);
        } catch (Throwable t) {
            OpenFriendMod.LOG.error("OpenFriend overlay render failed", t);
            renderFault = true;
        } finally {
            renderer.endFrame();
        }
        if (renderFault) renderFallback(g);
    }

    private void renderFallback(GuiGraphics g) {
        g.fill(0, 0, this.width, this.height, jp.zpw.openfriend.common.ui.UTheme.DIM_OVERLAY);
        g.drawCenteredString(this.font, Component.literal("OpenFriend overlay error — see logs"),
                this.width / 2, this.height / 2 - this.font.lineHeight, jp.zpw.openfriend.common.ui.UTheme.DANGER);
        g.drawCenteredString(this.font, Component.literal("Press Esc to close"),
                this.width / 2, this.height / 2 + 4, jp.zpw.openfriend.common.ui.UTheme.TOAST_CLOSE);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (renderFault) return super.mouseClicked(mouseX, mouseY, button);
        try {
            int sw = this.width;
            if (OpenFriendToastOverlay.handleClick((int) mouseX, (int) mouseY, sw)) return true;
            if (overlay.mouseClick(mouseX, mouseY, button)) return true;
        } catch (Throwable t) {
            OpenFriendMod.LOG.error("OpenFriend overlay mouseClick failed", t);
            renderFault = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (renderFault) return super.mouseReleased(mouseX, mouseY, button);
        try {
            if (overlay.mouseRelease(mouseX, mouseY, button)) return true;
        } catch (Throwable t) {
            OpenFriendMod.LOG.error("OpenFriend overlay mouseRelease failed", t);
            renderFault = true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (renderFault) return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        try {
            if (overlay.mouseScroll(mouseX, mouseY, scrollY)) return true;
        } catch (Throwable t) {
            OpenFriendMod.LOG.error("OpenFriend overlay mouseScroll failed", t);
            renderFault = true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (renderFault && keyCode == 256) {
            this.onClose();
            return true;
        }
        if (renderFault) return super.keyPressed(keyCode, scanCode, modifiers);
        try {
            if (OpenFriendToastOverlay.handleKey(keyCode)) return true;
            if (overlay.keyPress(keyCode, scanCode, modifiers)) return true;
        } catch (Throwable t) {
            OpenFriendMod.LOG.error("OpenFriend overlay keyPress failed", t);
            renderFault = true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char ch, int modifiers) {
        if (renderFault) return super.charTyped(ch, modifiers);
        try {
            if (overlay.charTyped(ch, modifiers)) return true;
        } catch (Throwable t) {
            OpenFriendMod.LOG.error("OpenFriend overlay charTyped failed", t);
            renderFault = true;
        }
        return super.charTyped(ch, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
