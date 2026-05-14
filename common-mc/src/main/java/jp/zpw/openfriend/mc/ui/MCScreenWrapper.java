/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 * Variant for group-b (Minecraft 1.19 - 1.19.4): Screen.render takes PoseStack
 * (GuiGraphics did not exist yet); mouseScrolled takes 3 args.
 */
package jp.zpw.openfriend.mc.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import jp.zpw.openfriend.common.screen.FriendsOverlayScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public final class MCScreenWrapper extends Screen {

    private final FriendsOverlayScreen overlay;
    private MCRenderer renderer;

    public MCScreenWrapper(FriendsOverlayScreen overlay) {
        super(new TextComponent("OpenFriend"));
        this.overlay = overlay;
    }

    @Override
    protected void init() {
        super.init();
        this.renderer = new MCRenderer(this.font);
        overlay.layout(this.width, this.height);
    }

    @Override
    public void resize(Minecraft mc, int w, int h) {
        super.resize(mc, w, h);
        overlay.layout(w, h);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (renderer == null) renderer = new MCRenderer(this.font);
        renderer.beginFrame(poseStack, mouseX, mouseY, partialTick);
        try {
            overlay.render(renderer);
        } finally {
            renderer.endFrame();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (overlay.mouseClick(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (overlay.mouseRelease(mouseX, mouseY, button)) return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (overlay.mouseScroll(mouseX, mouseY, scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (overlay.keyPress(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char ch, int modifiers) {
        if (overlay.charTyped(ch, modifiers)) return true;
        return super.charTyped(ch, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
