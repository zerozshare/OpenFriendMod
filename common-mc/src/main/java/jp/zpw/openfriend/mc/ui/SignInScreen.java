/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 * Variant for group-b (Minecraft 1.19 - 1.19.4): PoseStack-based render.
 */
package jp.zpw.openfriend.mc.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public final class SignInScreen extends Screen {

    private static final int PANEL_WIDTH = 300;
    private static final int PANEL_HEIGHT = 180;
    private static final int BORDER = 0xFF4F8FFF;
    private static final int BG = 0xF00A0A0A;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int CODE_COLOR = 0xFF7BC8FF;
    private static final int DIM = 0xFF777777;
    private static final int SUCCESS = 0xFF6CD27A;

    private static volatile SignInScreen current;

    public static SignInScreen current() { return current; }
    public static void setCurrent(SignInScreen s) { current = s; }

    private final String verificationUri;
    private final String userCode;

    private volatile boolean signedIn;
    private volatile String signedInName = "";

    private Button copyBtn;
    private Button browserBtn;
    private Button cancelBtn;

    public SignInScreen(String verificationUri, String userCode) {
        super(new TextComponent("Sign in to OpenFriend"));
        this.verificationUri = verificationUri == null ? "" : verificationUri;
        this.userCode = userCode == null ? "" : userCode;
    }

    public void markSignedIn(String name) {
        this.signedIn = true;
        this.signedInName = name == null ? "" : name;
    }

    @Override
    protected void init() {
        super.init();
        int mx = (this.width  - PANEL_WIDTH)  / 2;
        int my = (this.height - PANEL_HEIGHT) / 2;

        int btnH = 20; int gap = 6; int btnW = (PANEL_WIDTH - 16 - gap * 2) / 3;
        
        
        int row1Y = my + PANEL_HEIGHT - btnH - 12;

        copyBtn = new Button(mx + 8, row1Y, btnW, btnH, new TextComponent("Copy code"), b -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(userCode);
        });

        browserBtn = new Button(mx + 8 + btnW + gap, row1Y, btnW, btnH, new TextComponent("Open browser"), b -> {
            if (!verificationUri.isEmpty()) {
                try { Util.getPlatform().openUri(verificationUri); } catch (Throwable ignored) {}
            }
        });

        cancelBtn = new Button(mx + PANEL_WIDTH - btnW - 8, row1Y, btnW, btnH, new TextComponent("Close"), b -> onClose());

        addButton(copyBtn);
        addButton(browserBtn);
        addButton(cancelBtn);
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        super.render(pose, mouseX, mouseY, partialTick);

        int mx = (this.width  - PANEL_WIDTH)  / 2;
        int my = (this.height - PANEL_HEIGHT) / 2;

        GuiComponent.fill(pose, 0, 0, this.width, this.height, 0xC0000000);
        GuiComponent.fill(pose, mx, my, mx + PANEL_WIDTH, my + PANEL_HEIGHT, BG);
        GuiComponent.fill(pose, mx,                  my,                       mx + PANEL_WIDTH, my + 1,                   BORDER);
        GuiComponent.fill(pose, mx,                  my + PANEL_HEIGHT - 1,    mx + PANEL_WIDTH, my + PANEL_HEIGHT,        BORDER);
        GuiComponent.fill(pose, mx,                  my,                       mx + 1,           my + PANEL_HEIGHT,        BORDER);
        GuiComponent.fill(pose, mx + PANEL_WIDTH - 1, my,                      mx + PANEL_WIDTH, my + PANEL_HEIGHT,        BORDER);

        int titleY = my + 12;
        GuiComponent.drawCenteredString(pose, this.font, "Sign in to OpenFriend", this.width / 2, titleY, TEXT);

        int instrY = titleY + this.font.lineHeight + 6;
        GuiComponent.drawCenteredString(pose, this.font, "1. Open the URL in your browser:", this.width / 2, instrY, DIM);
        GuiComponent.drawCenteredString(pose, this.font, verificationUri, this.width / 2, instrY + this.font.lineHeight + 2, CODE_COLOR);

        int codeLabelY = instrY + this.font.lineHeight * 2 + 14;
        GuiComponent.drawCenteredString(pose, this.font, "2. Enter this code:", this.width / 2, codeLabelY, DIM);

        int codeY = codeLabelY + this.font.lineHeight + 12;
        int codeW = this.font.width(userCode) * 2;
        int codeBoxX1 = this.width / 2 - codeW / 2 - 10;
        int codeBoxX2 = this.width / 2 + codeW / 2 + 10;
        GuiComponent.fill(pose, codeBoxX1, codeY - 4, codeBoxX2, codeY + this.font.lineHeight * 2 + 4, 0xFF000000);
        pose.pushPose();
        pose.translate(this.width / 2f, codeY + this.font.lineHeight, 0);
        pose.scale(2.0f, 2.0f, 1.0f);
        pose.translate(-this.width / 2f, -(codeY + this.font.lineHeight), 0);
        GuiComponent.drawCenteredString(pose, this.font, userCode, this.width / 2, codeY, CODE_COLOR);
        pose.popPose();

        int statusY = my + PANEL_HEIGHT - 40;
        if (signedIn) {
            GuiComponent.drawCenteredString(pose, this.font, "Signed in as " + signedInName, this.width / 2, statusY, SUCCESS);
        } else {
            GuiComponent.drawCenteredString(pose, this.font, "Waiting for sign-in…", this.width / 2, statusY, DIM);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        if (current == this) current = null;
        super.onClose();
    }
}
