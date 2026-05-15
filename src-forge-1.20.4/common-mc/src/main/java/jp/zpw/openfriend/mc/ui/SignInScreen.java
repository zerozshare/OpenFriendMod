/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.mc.ui;

import jp.zpw.openfriend.common.ui.UTheme;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SignInScreen extends Screen {

    private static final int PANEL_WIDTH = 300;
    private static final int PANEL_HEIGHT = 180;

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
        super(Component.literal("Sign in to OpenFriend"));
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

        int btnH = 20;
        int gap = 6;
        int btnW = (PANEL_WIDTH - 16 - gap * 2) / 3;
        int row1Y = my + PANEL_HEIGHT - btnH - 12;

        copyBtn = Button.builder(Component.literal("Copy code"), b -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(userCode);
        }).bounds(mx + 8, row1Y, btnW, btnH).build();

        browserBtn = Button.builder(Component.literal("Open browser"), b -> {
            if (!verificationUri.isEmpty()) {
                try { Util.getPlatform().openUri(verificationUri); } catch (Throwable ignored) {}
            }
        }).bounds(mx + 8 + btnW + gap, row1Y, btnW, btnH).build();

        cancelBtn = Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(mx + 8 + (btnW + gap) * 2, row1Y, btnW, btnH).build();

        addRenderableWidget(copyBtn);
        addRenderableWidget(browserBtn);
        addRenderableWidget(cancelBtn);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        int mx = (this.width  - PANEL_WIDTH)  / 2;
        int my = (this.height - PANEL_HEIGHT) / 2;

        g.fill(0, 0, this.width, this.height, UTheme.DIM_OVERLAY);
        g.fill(mx, my, mx + PANEL_WIDTH, my + PANEL_HEIGHT, UTheme.TOAST_BG);
        g.fill(mx,                  my,                       mx + PANEL_WIDTH, my + 1,                   UTheme.ACCENT_BLUE);
        g.fill(mx,                  my + PANEL_HEIGHT - 1,    mx + PANEL_WIDTH, my + PANEL_HEIGHT,        UTheme.ACCENT_BLUE);
        g.fill(mx,                  my,                       mx + 1,           my + PANEL_HEIGHT,        UTheme.ACCENT_BLUE);
        g.fill(mx + PANEL_WIDTH - 1, my,                      mx + PANEL_WIDTH, my + PANEL_HEIGHT,        UTheme.ACCENT_BLUE);

        int titleY = my + 12;
        g.drawCenteredString(this.font, "Sign in to OpenFriend", this.width / 2, titleY, UTheme.TEXT);

        int instrY = titleY + this.font.lineHeight + 6;
        g.drawCenteredString(this.font, "1. Open the URL in your browser:", this.width / 2, instrY, UTheme.TEXT_DIM);
        g.drawCenteredString(this.font, verificationUri, this.width / 2, instrY + this.font.lineHeight + 2, UTheme.ACCENT_CYAN);

        int codeLabelY = instrY + this.font.lineHeight * 2 + 14;
        g.drawCenteredString(this.font, "2. Enter this code:", this.width / 2, codeLabelY, UTheme.TEXT_DIM);

        int codeY = codeLabelY + this.font.lineHeight + 12;
        int codeW = this.font.width(userCode) * 2;
        int codeBoxX1 = this.width / 2 - codeW / 2 - 10;
        int codeBoxX2 = this.width / 2 + codeW / 2 + 10;
        g.fill(codeBoxX1, codeY - 4, codeBoxX2, codeY + this.font.lineHeight * 2 + 4, UTheme.BG);
        g.pose().pushPose();
        g.pose().translate(this.width / 2f, codeY + this.font.lineHeight, 0);
        g.pose().scale(2.0f, 2.0f, 1.0f);
        g.pose().translate(-this.width / 2f, -(codeY + this.font.lineHeight), 0);
        g.drawCenteredString(this.font, userCode, this.width / 2, codeY, UTheme.ACCENT_CYAN);
        g.pose().popPose();

        int statusY = my + PANEL_HEIGHT - 40;
        if (signedIn) {
            g.drawCenteredString(this.font, "Signed in as " + signedInName, this.width / 2, statusY, UTheme.ONLINE);
        } else {
            g.drawCenteredString(this.font, "Waiting for sign-in…", this.width / 2, statusY, UTheme.TEXT_DIM);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
