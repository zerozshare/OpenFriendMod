/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 * Variant for group-a (Minecraft 1.16.5): no shader pipeline yet — use
 * TextureManager.bind + RenderSystem.color4f; AbstractWidget.isHovered is a
 * field; Button constructor is 6-arg.
 */
package jp.zpw.openfriend.mc.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public final class OpenFriendIconButton extends Button {

    private static final ResourceLocation TEX =
            new ResourceLocation("openfriend", "textures/gui/openfriend_icon.png");

    public OpenFriendIconButton(int x, int y, int w, int h, OnPress onPress) {
        super(x, y, w, h, new TextComponent("Friends"), onPress);
    }

    @Override
    public void renderButton(PoseStack pose, int mouseX, int mouseY, float partial) {
        if (this.isHovered || this.isFocused()) {
            GuiComponent.fill(pose, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, 0x40FFFFFF);
        }
        Minecraft.getInstance().getTextureManager().bind(TEX);
        float a = this.active ? 1.0f : 0.5f;
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, a);
        RenderSystem.enableBlend();
        GuiComponent.blit(pose, this.x, this.y, 0.0f, 0.0f, this.width, this.height, this.width, this.height);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }
}
