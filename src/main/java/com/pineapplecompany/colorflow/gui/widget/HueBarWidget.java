package com.pineapplecompany.colorflow.gui.widget;

import com.pineapplecompany.colorflow.ColorflowClient;
import com.pineapplecompany.colorflow.gui.theme.Theme;
import java.util.function.Consumer;
import net.minecraft.client.gui.DrawContext;

public class HueBarWidget {
    
    private final int x, y, width, height;
    private final Consumer<Float> onChange;
    private float hue = 0;
    private boolean dragging = false;
    
    public HueBarWidget(int x, int y, int width, int height, Consumer<Float> onChange) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.onChange = onChange;
    }
    
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Theme theme = ColorflowClient.getCurrentTheme();
        for (int py = 0; py < height; py++) {
            float h = (float) py / height * 360;
            int color = hueToRgb(h);
            context.fill(x, y + py, x + width, y + py + 1, 0xFF000000 | color);
        }
        
        drawBorder(context, x - 2, y - 2, width + 4, height + 4, theme.getBorder());
        int cursorY = y + (int) (hue / 360 * height);
        for (int i = 0; i < 6; i++) {
            context.fill(x - 8 + i, cursorY - (5 - i), x - 8 + i + 1, cursorY + (6 - i), 0xFFFFFFFF);
        }
        for (int i = 0; i < 6; i++) {
            context.fill(x + width + 7 - i, cursorY - (5 - i), x + width + 8 - i, cursorY + (6 - i), 0xFFFFFFFF);
        }
        
        context.fill(x, cursorY - 1, x + width, cursorY + 2, 0xFFFFFFFF);
        context.fill(x + 1, cursorY, x + width - 1, cursorY + 1, 0xFF000000);
    }
    
    private void drawBorder(DrawContext context, int bx, int by, int bw, int bh, int color) {
        context.fill(bx, by, bx + bw, by + 2, color);
        context.fill(bx, by + bh - 2, bx + bw, by + bh, color);
        context.fill(bx, by, bx + 2, by + bh, color);
        context.fill(bx + bw - 2, by, bx + bw, by + bh, color);
    }
    
    private int hueToRgb(float h) {
        float c = 1;
        float x = 1 - Math.abs((h / 60) % 2 - 1);
        float r, g, b;
        
        if (h < 60) { r = c; g = x; b = 0; }
        else if (h < 120) { r = x; g = c; b = 0; }
        else if (h < 180) { r = 0; g = c; b = x; }
        else if (h < 240) { r = 0; g = x; b = c; }
        else if (h < 300) { r = x; g = 0; b = c; }
        else { r = c; g = 0; b = x; }
        
        return ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }
    
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isInBounds(mouseX, mouseY)) {
            dragging = true;
            updateFromMouse(mouseY);
            return true;
        }
        return false;
    }
    
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            updateFromMouse(mouseY);
            return true;
        }
        return false;
    }
    
    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }
    
    private void updateFromMouse(double mouseY) {
        hue = (float) Math.max(0, Math.min(359.9, (mouseY - y) / height * 360));
        if (onChange != null) onChange.accept(hue);
    }
    
    private boolean isInBounds(double mx, double my) {
        return mx >= x - 10 && mx < x + width + 10 && my >= y && my < y + height;
    }
    
    public void setHue(float h) {
        this.hue = h;
    }
}