/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.mc.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import jp.zpw.openfriend.common.ui.UTheme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class OpenFriendIconButton extends Button {

    private static final ResourceLocation TEX =
            new ResourceLocation("openfriend", "textures/gui/openfriend_icon.png");

    public OpenFriendIconButton(int x, int y, int w, int h, OnPress onPress) {
        super(x, y, w, h, Component.literal("Friends"), onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partial) {
        if (this.isHoveredOrFocused()) {
            g.fill(getX() - 1, getY() - 1, getX() + width + 1, getY() + height + 1, UTheme.HOVER_GHOST);
        }
        RenderSystem.enableBlend();
        float a = this.active ? 1.0f : 0.5f;
        g.setColor(1.0f, 1.0f, 1.0f, a);
        g.blit(TEX, getX(), getY(), width, height, 0.0f, 0.0f, 512, 512, 512, 512);
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }
}
