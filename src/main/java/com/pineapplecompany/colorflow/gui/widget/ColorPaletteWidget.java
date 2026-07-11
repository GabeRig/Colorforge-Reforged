package com.pineapplecompany.colorflow.gui.widget;

import com.pineapplecompany.colorflow.ColorflowClient;
import com.pineapplecompany.colorflow.gui.theme.Theme;
import net.minecraft.client.gui.DrawContext;

import java.util.function.BiConsumer;

public class ColorPaletteWidget {
    
    private final int x, y, width, height;
    private final BiConsumer<Float, Float> onChange;
    private float saturation = 1.0f;
    private float value = 1.0f;
    private boolean dragging = false;
    
    public ColorPaletteWidget(int x, int y, int width, int height, BiConsumer<Float, Float> onChange) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.onChange = onChange;
    }
    
    public void render(DrawContext context, int mouseX, int mouseY, float delta, float hue) {
        Theme theme = ColorflowClient.getCurrentTheme();
        for (int py = 0; py < height; py++) {
            for (int px = 0; px < width; px++) {
                float s = (float) px / width;
                float v = 1.0f - (float) py / height;
                int color = hsvToRgb(hue, s, v);
                context.fill(x + px, y + py, x + px + 1, y + py + 1, 0xFF000000 | color);
            }
        }
        
        drawBorder(context, x - 2, y - 2, width + 4, height + 4, theme.getBorder());
        int cursorX = x + (int) (saturation * (width - 1));
        int cursorY = y + (int) ((1.0f - value) * (height - 1));
        
        context.fill(cursorX - 7, cursorY - 7, cursorX + 8, cursorY + 8, 0xFFFFFFFF);
        context.fill(cursorX - 6, cursorY - 6, cursorX + 7, cursorY + 7, 0xFF000000);
        int cursorColor = hsvToRgb(hue, saturation, value);
        context.fill(cursorX - 5, cursorY - 5, cursorX + 6, cursorY + 6, 0xFF000000 | cursorColor);
        context.fill(cursorX - 10, cursorY, cursorX - 7, cursorY + 1, 0xFFFFFFFF);
        context.fill(cursorX + 8, cursorY, cursorX + 11, cursorY + 1, 0xFFFFFFFF);
        context.fill(cursorX, cursorY - 10, cursorX + 1, cursorY - 7, 0xFFFFFFFF);
        context.fill(cursorX, cursorY + 8, cursorX + 1, cursorY + 11, 0xFFFFFFFF);
    }
    
    private void drawBorder(DrawContext context, int bx, int by, int bw, int bh, int color) {
        context.fill(bx, by, bx + bw, by + 2, color);
        context.fill(bx, by + bh - 2, bx + bw, by + bh, color);
        context.fill(bx, by, bx + 2, by + bh, color);
        context.fill(bx + bw - 2, by, bx + bw, by + bh, color);
    }
    
    private int hsvToRgb(float h, float s, float v) {
        float c = v * s;
        float x = c * (1 - Math.abs((h / 60) % 2 - 1));
        float m = v - c;
        float r, g, b;
        
        if (h < 60) { r = c; g = x; b = 0; }
        else if (h < 120) { r = x; g = c; b = 0; }
        else if (h < 180) { r = 0; g = c; b = x; }
        else if (h < 240) { r = 0; g = x; b = c; }
        else if (h < 300) { r = x; g = 0; b = c; }
        else { r = c; g = 0; b = x; }
        
        int ri = (int) ((r + m) * 255);
        int gi = (int) ((g + m) * 255);
        int bi = (int) ((b + m) * 255);
        
        return (ri << 16) | (gi << 8) | bi;
    }
    
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isInBounds(mouseX, mouseY)) {
            dragging = true;
            updateFromMouse(mouseX, mouseY);
            return true;
        }
        return false;
    }
    
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            updateFromMouse(mouseX, mouseY);
            return true;
        }
        return false;
    }
    
    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }
    
    private void updateFromMouse(double mouseX, double mouseY) {
        saturation = (float) Math.max(0, Math.min(1, (mouseX - x) / width));
        value = 1.0f - (float) Math.max(0, Math.min(1, (mouseY - y) / height));
        if (onChange != null) onChange.accept(saturation, value);
    }
    
    private boolean isInBounds(double mx, double my) {
        return mx >= x && mx < x + width && my >= y && my < y + height;
    }
    
    public void setSaturationValue(float s, float v) {
        this.saturation = s;
        this.value = v;
    }
}