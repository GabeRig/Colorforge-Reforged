package com.pineapplecompany.colorflow.gui.widget;

import com.pineapplecompany.colorflow.ColorflowClient;
import com.pineapplecompany.colorflow.gui.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class ColorSliderWidget extends SliderWidget {
    
    public enum SliderType {
        RED, GREEN, BLUE,
        HUE, SATURATION, VALUE,
        ALPHA
    }
    
    private final SliderType type;
    private final Consumer<Double> onValueChanged;
    private final double minValue;
    private final double maxValue;
    
    private int baseHue = 0;
    private float baseSaturation = 1.0f;
    private float baseValue = 1.0f;
    
    public ColorSliderWidget(int x, int y, int width, int height, 
                             SliderType type, double initialValue,
                             Consumer<Double> onValueChanged) {
        super(x, y, width, height, Text.literal(""), normalizeValue(initialValue, type));
        this.type = type;
        this.onValueChanged = onValueChanged;
        this.minValue = getMinValue(type);
        this.maxValue = getMaxValue(type);
        updateMessage();
    }
    
    private static double normalizeValue(double value, SliderType type) {
        double min = getMinValue(type);
        double max = getMaxValue(type);
        return (value - min) / (max - min);
    }
    
    private static double getMinValue(SliderType type) {
        return 0;
    }
    
    private static double getMaxValue(SliderType type) {
        return switch (type) {
            case HUE -> 360;
            case SATURATION, VALUE, ALPHA -> 1;
            default -> 255;
        };
    }
    
    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Theme theme = ColorflowClient.getCurrentTheme();
        
        context.fill(getX(), getY(), getX() + width, getY() + height, 
            theme.getSecondaryBackground());
        
        drawGradientTrack(context);
        
        int handleX = getX() + (int) ((width - 8) * value);
        int handleColor = theme.getPrimaryAccent();
        
        context.fill(handleX + 1, getY() + 1, handleX + 9, getY() + height + 1, 
            0x40000000);
        
        context.fill(handleX, getY(), handleX + 8, getY() + height, handleColor);
        
        context.fill(handleX + 1, getY() + 1, handleX + 7, getY() + 3, 
            0x40FFFFFF);
        
        drawBorder(context, getX(), getY(), width, height, theme.getBorder());
        
        String valueText = getFormattedValue();
        MinecraftClient client = MinecraftClient.getInstance();
        context.drawText(client.textRenderer, valueText,
            getX() + width + 5, getY() + (height - 8) / 2,
            theme.getTextPrimary(), true);
    }
    
    private void drawGradientTrack(DrawContext context) {
        int trackX = getX() + 2;
        int trackY = getY() + 2;
        int trackWidth = width - 4;
        int trackHeight = height - 4;
        
        switch (type) {
            case RED -> drawHorizontalGradient(context, trackX, trackY, trackWidth, trackHeight,
                0xFF000000 | (0 << 16) | (baseHue << 8) | baseHue,
                0xFF000000 | (255 << 16) | (baseHue << 8) | baseHue);
            case GREEN -> drawHorizontalGradient(context, trackX, trackY, trackWidth, trackHeight,
                0xFF000000 | (baseHue << 16) | (0 << 8) | baseHue,
                0xFF000000 | (baseHue << 16) | (255 << 8) | baseHue);
            case BLUE -> drawHorizontalGradient(context, trackX, trackY, trackWidth, trackHeight,
                0xFF000000 | (baseHue << 16) | (baseHue << 8) | 0,
                0xFF000000 | (baseHue << 16) | (baseHue << 8) | 255);
            case HUE -> drawHueGradient(context, trackX, trackY, trackWidth, trackHeight);
            case SATURATION -> drawSaturationGradient(context, trackX, trackY, trackWidth, trackHeight);
            case VALUE -> drawValueGradient(context, trackX, trackY, trackWidth, trackHeight);
            case ALPHA -> drawCheckerboard(context, trackX, trackY, trackWidth, trackHeight);
        }
    }
    
    private void drawHorizontalGradient(DrawContext context, int x, int y, int width, int height,
                                        int colorStart, int colorEnd) {
        for (int i = 0; i < width; i++) {
            float t = (float) i / width;
            int color = lerpColor(colorStart, colorEnd, t);
            context.fill(x + i, y, x + i + 1, y + height, color);
        }
    }
    
    private void drawHueGradient(DrawContext context, int x, int y, int width, int height) {
        for (int i = 0; i < width; i++) {
            float hue = (float) i / width * 360;
            int color = java.awt.Color.HSBtoRGB(hue / 360f, 1f, 1f) | 0xFF000000;
            context.fill(x + i, y, x + i + 1, y + height, color);
        }
    }
    
    private void drawSaturationGradient(DrawContext context, int x, int y, int width, int height) {
        for (int i = 0; i < width; i++) {
            float sat = (float) i / width;
            int color = java.awt.Color.HSBtoRGB(baseHue / 360f, sat, baseValue) | 0xFF000000;
            context.fill(x + i, y, x + i + 1, y + height, color);
        }
    }
    
    private void drawValueGradient(DrawContext context, int x, int y, int width, int height) {
        for (int i = 0; i < width; i++) {
            float val = (float) i / width;
            int color = java.awt.Color.HSBtoRGB(baseHue / 360f, baseSaturation, val) | 0xFF000000;
            context.fill(x + i, y, x + i + 1, y + height, color);
        }
    }
    
    private void drawCheckerboard(DrawContext context, int x, int y, int width, int height) {
        int checkSize = 4;
        for (int i = 0; i < width; i += checkSize) {
            for (int j = 0; j < height; j += checkSize) {
                boolean isWhite = ((i / checkSize) + (j / checkSize)) % 2 == 0;
                int color = isWhite ? 0xFFFFFFFF : 0xFFCCCCCC;
                context.fill(x + i, y + j, 
                    Math.min(x + i + checkSize, x + width),
                    Math.min(y + j + checkSize, y + height), color);
            }
        }
        drawHorizontalGradient(context, x, y, width, height, 0x00000000, 0xFF000000);
    }
    
    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }
    
    private int lerpColor(int color1, int color2, float t) {
        int a1 = (color1 >> 24) & 0xFF, a2 = (color2 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF, r2 = (color2 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF, g2 = (color2 >> 8) & 0xFF;
        int b1 = color1 & 0xFF, b2 = color2 & 0xFF;
        
        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);
        
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    
    @Override
    protected void updateMessage() {
        setMessage(Text.literal(getLabel() + ": " + getFormattedValue()));
    }
    
    @Override
    protected void applyValue() {
        if (onValueChanged != null) {
            onValueChanged.accept(getActualValue());
        }
    }
    
    public double getActualValue() {
        return minValue + (maxValue - minValue) * value;
    }
    
    public void setActualValue(double actualValue) {
        this.value = (actualValue - minValue) / (maxValue - minValue);
        updateMessage();
    }
    
    private String getLabel() {
        return switch (type) {
            case RED -> "R";
            case GREEN -> "G";
            case BLUE -> "B";
            case HUE -> "H";
            case SATURATION -> "S";
            case VALUE -> "V";
            case ALPHA -> "A";
        };
    }
    
    private String getFormattedValue() {
        double actual = getActualValue();
        return switch (type) {
            case HUE -> String.format("%.0f°", actual);
            case SATURATION, VALUE, ALPHA -> String.format("%.0f%%", actual * 100);
            default -> String.format("%.0f", actual);
        };
    }
    
    public void setBaseColor(float hue, float saturation, float val) {
        this.baseHue = (int) hue;
        this.baseSaturation = saturation;
        this.baseValue = val;
    }
}