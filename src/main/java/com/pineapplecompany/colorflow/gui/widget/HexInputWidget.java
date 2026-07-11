package com.pineapplecompany.colorflow.gui.widget;

import com.pineapplecompany.colorflow.ColorflowClient;
import com.pineapplecompany.colorflow.color.ColorUtils;
import com.pineapplecompany.colorflow.gui.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class HexInputWidget extends TextFieldWidget {
    
    private Consumer<int[]> onColorChanged;
    private int lastValidColor = 0xFFFFFF;
    private boolean isValid = true;
    
    public HexInputWidget(TextRenderer textRenderer, int x, int y, int width, int height) {
        super(textRenderer, x, y, width, height, Text.literal("Hex Color"));
        
        setMaxLength(7);
        setText("#FFFFFF");
        setEditableColor(0xFFFFFF);
        
        setChangedListener(this::onTextChanged);
    }
    
    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Theme theme = ColorflowClient.getCurrentTheme();
        
        context.fill(getX() - 1, getY() - 1, getX() + width + 1, getY() + height + 1,
            theme.getBorder());
        context.fill(getX(), getY(), getX() + width, getY() + height,
            theme.getSecondaryBackground());
        
        int swatchSize = height - 4;
        int swatchX = getX() + width - swatchSize - 2;
        int swatchY = getY() + 2;
        
        drawCheckerboard(context, swatchX, swatchY, swatchSize, swatchSize);
        
        int previewColor = isValid ? (0xFF000000 | lastValidColor) : 0xFFFF0000;
        context.fill(swatchX, swatchY, swatchX + swatchSize, swatchY + swatchSize, previewColor);
        
        drawBorder(context, swatchX, swatchY, swatchSize, swatchSize, theme.getBorder());
        
        if (!isValid) {
            context.fill(getX(), getY() + height - 2, getX() + width - swatchSize - 4, 
                getY() + height, 0xFFFF4444);
        }
        
        super.renderWidget(context, mouseX, mouseY, delta);
        
        MinecraftClient client = MinecraftClient.getInstance();
        context.drawText(client.textRenderer, "HEX",
            getX(), getY() - 12, theme.getTextSecondary(), false);
    }
    
    private void drawCheckerboard(DrawContext context, int x, int y, int width, int height) {
        int checkSize = 3;
        for (int i = 0; i < width; i += checkSize) {
            for (int j = 0; j < height; j += checkSize) {
                boolean isWhite = ((i / checkSize) + (j / checkSize)) % 2 == 0;
                int color = isWhite ? 0xFFFFFFFF : 0xFFCCCCCC;
                context.fill(x + i, y + j,
                    Math.min(x + i + checkSize, x + width),
                    Math.min(y + j + checkSize, y + height), color);
            }
        }
    }
    
    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }
    
    private void onTextChanged(String text) {
        int[] rgb = ColorUtils.hexToRgb(text);
        
        if (rgb != null) {
            isValid = true;
            lastValidColor = ColorUtils.rgbToInt(rgb[0], rgb[1], rgb[2]);
            setEditableColor(0xFFFFFF);
            
            if (onColorChanged != null) {
                onColorChanged.accept(rgb);
            }
        } else {
            isValid = false;
            setEditableColor(0xFF6666);
        }
    }
    
    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (chr == '#' || (chr >= '0' && chr <= '9') || 
            (chr >= 'a' && chr <= 'f') || (chr >= 'A' && chr <= 'F')) {
            return super.charTyped(chr, modifiers);
        }
        return false;
    }
    
    public void setOnColorChanged(Consumer<int[]> callback) {
        this.onColorChanged = callback;
    }
    
    public void setFromRgb(int r, int g, int b) {
        String hex = ColorUtils.rgbToHex(r, g, b);
        Consumer<int[]> temp = onColorChanged;
        onColorChanged = null;
        setText(hex);
        lastValidColor = ColorUtils.rgbToInt(r, g, b);
        isValid = true;
        onColorChanged = temp;
    }
    
    public int getColor() {
        return lastValidColor;
    }
    
    public boolean isValidColor() {
        return isValid;
    }
}