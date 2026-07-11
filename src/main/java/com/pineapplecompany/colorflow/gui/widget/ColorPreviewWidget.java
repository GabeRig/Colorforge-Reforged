package com.pineapplecompany.colorflow.gui.widget;

import com.pineapplecompany.colorflow.ColorflowClient;
import com.pineapplecompany.colorflow.color.ColorUtils;
import com.pineapplecompany.colorflow.gui.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;

public class ColorPreviewWidget implements Drawable, Element {
    
    private final int x, y, size;
    private int red = 255, green = 0, blue = 0;
    
    public ColorPreviewWidget(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Theme theme = ColorflowClient.getCurrentTheme();
        MinecraftClient client = MinecraftClient.getInstance();
        
        int panelW = size + 10;
        int panelH = size + 80;
        context.fill(x, y, x + panelW, y + panelH, theme.getSecondaryBackground());
        drawBorder(context, x, y, panelW, panelH, theme.getBorder());
        
        context.drawText(client.textRenderer, "COLOR", x + 5, y + 5, theme.getTextSecondary(), false);
        int swatchX = x + 5;
        int swatchY = y + 18;
        int swatchSize = size;
        
        context.fill(swatchX - 2, swatchY - 2, swatchX + swatchSize + 2, swatchY + swatchSize + 2, 0xFF000000);
        
        int mainColor = 0xFF000000 | (red << 16) | (green << 8) | blue;
        context.fill(swatchX, swatchY, swatchX + swatchSize, swatchY + swatchSize, mainColor);
        for (int i = 0; i < swatchSize / 4; i++) {
            int alpha = 60 - i * 4;
            context.fill(swatchX, swatchY + i, swatchX + swatchSize, swatchY + i + 1, (alpha << 24) | 0xFFFFFF);
        }
        
        int barY = swatchY + swatchSize + 8;
        int barH = 14;
        int halfW = swatchSize / 2 - 2;
        int lr = Math.min(255, red + 50);
        int lg = Math.min(255, green + 50);
        int lb = Math.min(255, blue + 50);
        context.fill(swatchX, barY, swatchX + halfW, barY + barH, 0xFF000000 | (lr << 16) | (lg << 8) | lb);
        context.drawText(client.textRenderer, "+", swatchX + halfW / 2 - 2, barY + 3, 0xFF000000, false);
        int dr = Math.max(0, red - 50);
        int dg = Math.max(0, green - 50);
        int db = Math.max(0, blue - 50);
        context.fill(swatchX + halfW + 4, barY, swatchX + swatchSize, barY + barH, 0xFF000000 | (dr << 16) | (dg << 8) | db);
        context.drawText(client.textRenderer, "-", swatchX + halfW + halfW / 2 + 2, barY + 3, 0xFFFFFFFF, false);
        
        String hex = ColorUtils.rgbToHex(red, green, blue);
        context.drawText(client.textRenderer, hex, x + 5, barY + barH + 8, 0xFFFFFFFF, true);
        
        context.drawText(client.textRenderer, "R:" + red, x + 5, barY + barH + 22, 0xFFFF6666, false);
        context.drawText(client.textRenderer, "G:" + green, x + 5, barY + barH + 34, 0xFF66FF66, false);
        context.drawText(client.textRenderer, "B:" + blue, x + 5, barY + barH + 46, 0xFF6666FF, false);
    }
    
    private void drawBorder(DrawContext ctx, int bx, int by, int bw, int bh, int color) {
        ctx.fill(bx, by, bx + bw, by + 2, color);
        ctx.fill(bx, by + bh - 2, bx + bw, by + bh, color);
        ctx.fill(bx, by, bx + 2, by + bh, color);
        ctx.fill(bx + bw - 2, by, bx + bw, by + bh, color);
    }
    
    public void setColor(int r, int g, int b) {
        red = Math.max(0, Math.min(255, r));
        green = Math.max(0, Math.min(255, g));
        blue = Math.max(0, Math.min(255, b));
    }
    
    public void setColor(int rgb) {
        setColor((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }
    
    public int getRed() { return red; }
    public int getGreen() { return green; }
    public int getBlue() { return blue; }
    public int getPackedColor() { return ColorUtils.rgbToInt(red, green, blue); }
    
    @Override public void setFocused(boolean f) {}
    @Override public boolean isFocused() { return false; }
}