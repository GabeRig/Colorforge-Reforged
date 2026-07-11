package com.pineapplecompany.colorflow.gui;

import com.pineapplecompany.colorflow.Colorflow;
import com.pineapplecompany.colorflow.ColorflowClient;
import com.pineapplecompany.colorflow.color.BlockColorExtractor;
import com.pineapplecompany.colorflow.color.ColorUtils;
import com.pineapplecompany.colorflow.gui.theme.Theme;
import com.pineapplecompany.colorflow.gui.widget.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ColorPickerScreen extends Screen {
    
    private static final int PANEL_WIDTH = 480;
    private static final int PANEL_HEIGHT = 320;
    
    private ColorPaletteWidget colorPalette;
    private HueBarWidget hueBar;
    private ColorPreviewWidget colorPreview;
    private BlockSuggestionWidget blockSuggestions;
    private HexInputWidget hexInput;
    
    private ColorSliderWidget redSlider;
    private ColorSliderWidget greenSlider;
    private ColorSliderWidget blueSlider;
    
    private float currentHue = 0;
    private float currentSaturation = 1;
    private float currentValue = 1;
    private int currentRed = 255;
    private int currentGreen = 0;
    private int currentBlue = 0;
    private boolean updating = false;
    private boolean fullBlocksOnly = false;
    
    public ColorPickerScreen() {
        super(Text.literal("Colorflow"));
    }
    
    @Override
    protected void init() {
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;
        
        colorPalette = new ColorPaletteWidget(panelX + 15, panelY + 40, 120, 120, this::onPaletteChanged);
        hueBar = new HueBarWidget(panelX + 145, panelY + 40, 20, 120, this::onHueChanged);
        
        int sliderX = panelX + 15;
        int sliderY = panelY + 175;
        int sliderW = 150;
        
        redSlider = new ColorSliderWidget(sliderX, sliderY, sliderW, 14,
            ColorSliderWidget.SliderType.RED, currentRed, this::onRedChanged);
        addDrawableChild(redSlider);
        
        greenSlider = new ColorSliderWidget(sliderX, sliderY + 20, sliderW, 14,
            ColorSliderWidget.SliderType.GREEN, currentGreen, this::onGreenChanged);
        addDrawableChild(greenSlider);
        
        blueSlider = new ColorSliderWidget(sliderX, sliderY + 40, sliderW, 14,
            ColorSliderWidget.SliderType.BLUE, currentBlue, this::onBlueChanged);
        addDrawableChild(blueSlider);
        
        hexInput = new HexInputWidget(textRenderer, sliderX, sliderY + 65, 80, 16);
        hexInput.setFromRgb(currentRed, currentGreen, currentBlue);
        hexInput.setOnColorChanged(this::onHexChanged);
        addDrawableChild(hexInput);
        
        colorPreview = new ColorPreviewWidget(panelX + 180, panelY + 40, 80);
        colorPreview.setColor(currentRed, currentGreen, currentBlue);
        
        blockSuggestions = new BlockSuggestionWidget(panelX + 280, panelY + 35);
        blockSuggestions.updateSuggestions(currentRed, currentGreen, currentBlue);
        
        int btnY = panelY + PANEL_HEIGHT - 40;
        
        addDrawableChild(ButtonWidget.builder(Text.literal("Gradient Tool"), b -> openGradientScreen())
            .dimensions(panelX + 15, btnY, 100, 20).build());
        
        addDrawableChild(ButtonWidget.builder(Text.literal(ColorflowClient.getCurrentTheme().getName()), b -> {
            ColorflowClient.cycleTheme();
            b.setMessage(Text.literal(ColorflowClient.getCurrentTheme().getName()));
        }).dimensions(panelX + 120, btnY, 90, 20).build());
        
        addDrawableChild(ButtonWidget.builder(Text.literal(fullBlocksOnly ? "Full Blocks" : "All Blocks"), b -> {
            fullBlocksOnly = !fullBlocksOnly;
            ColorflowClient.getBlockColorExtractor().setFullBlocksOnly(fullBlocksOnly);
            b.setMessage(Text.literal(fullBlocksOnly ? "Full Blocks" : "All Blocks"));
            blockSuggestions.updateSuggestions(currentRed, currentGreen, currentBlue);
        }).dimensions(panelX + 215, btnY, 80, 20).build());
        
        addDrawableChild(ButtonWidget.builder(Text.literal("X"), b -> close())
            .dimensions(panelX + PANEL_WIDTH - 25, panelY + 8, 18, 18).build());
        
        syncFromRgb();
    }
    
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xC0101010);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        
        Theme theme = ColorflowClient.getCurrentTheme();
        MinecraftClient client = MinecraftClient.getInstance();
        
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;
        
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xF0000000 | (theme.getPanelBackground() & 0x00FFFFFF));
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 5, theme.getPrimaryAccent());
        drawBorder(context, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, theme.getBorder(), 2);
        
        drawTitle(context, panelX, panelY, theme, client);
        
        context.drawText(client.textRenderer, "Color Palette", panelX + 15, panelY + 28, theme.getTextSecondary(), false);
        context.drawText(client.textRenderer, "RGB Values", panelX + 15, panelY + 163, theme.getTextSecondary(), false);
        
        colorPalette.render(context, mouseX, mouseY, delta, currentHue);
        hueBar.render(context, mouseX, mouseY, delta);
        colorPreview.render(context, mouseX, mouseY, delta);
        blockSuggestions.render(context, mouseX, mouseY, delta);
        
        BlockColorExtractor ext = ColorflowClient.getBlockColorExtractor();
        if (ext != null) {
            String stats = ext.getBlockCount() + " blocks loaded";
            context.drawText(client.textRenderer, stats, panelX + PANEL_WIDTH - client.textRenderer.getWidth(stats) - 10, 
                panelY + PANEL_HEIGHT - 12, theme.getTextSecondary(), false);
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void drawTitle(DrawContext context, int panelX, int panelY, Theme theme, MinecraftClient client) {
        int iconX = panelX + 15;
        int iconY = panelY + 10;
        context.fill(iconX, iconY, iconX + 5, iconY + 5, 0xFFFF4444);
        context.fill(iconX + 6, iconY, iconX + 11, iconY + 5, 0xFF44FF44);
        context.fill(iconX + 12, iconY, iconX + 17, iconY + 5, 0xFF4444FF);
        
        context.drawText(client.textRenderer, "COLORFLOW", iconX + 22, iconY - 1, theme.getTextPrimary(), true);
        context.drawText(client.textRenderer, "v" + Colorflow.VERSION, iconX + 85, iconY - 1, theme.getTextSecondary(), false);
    }
    
    private void drawBorder(DrawContext context, int x, int y, int w, int h, int color, int thickness) {
        context.fill(x, y, x + w, y + thickness, color);
        context.fill(x, y + h - thickness, x + w, y + h, color);
        context.fill(x, y, x + thickness, y + h, color);
        context.fill(x + w - thickness, y, x + w, y + h, color);
    }
    
    private void onPaletteChanged(float saturation, float value) {
        if (updating) return;
        currentSaturation = saturation;
        currentValue = value;
        syncFromHsv();
    }
    
    private void onHueChanged(float hue) {
        if (updating) return;
        currentHue = hue;
        syncFromHsv();
    }
    
    private void onRedChanged(Double value) {
        if (updating) return;
        currentRed = value.intValue();
        syncFromRgb();
    }
    
    private void onGreenChanged(Double value) {
        if (updating) return;
        currentGreen = value.intValue();
        syncFromRgb();
    }
    
    private void onBlueChanged(Double value) {
        if (updating) return;
        currentBlue = value.intValue();
        syncFromRgb();
    }
    
    private void onHexChanged(int[] rgb) {
        if (updating) return;
        currentRed = rgb[0];
        currentGreen = rgb[1];
        currentBlue = rgb[2];
        syncFromRgb();
    }
    
    private void syncFromHsv() {
        updating = true;
        int[] rgb = ColorUtils.hsvToRgb(currentHue, currentSaturation, currentValue);
        currentRed = rgb[0];
        currentGreen = rgb[1];
        currentBlue = rgb[2];
        
        redSlider.setActualValue(currentRed);
        greenSlider.setActualValue(currentGreen);
        blueSlider.setActualValue(currentBlue);
        hexInput.setFromRgb(currentRed, currentGreen, currentBlue);
        colorPreview.setColor(currentRed, currentGreen, currentBlue);
        blockSuggestions.updateSuggestions(currentRed, currentGreen, currentBlue);
        updating = false;
    }
    
    private void syncFromRgb() {
        updating = true;
        float[] hsv = ColorUtils.rgbToHsv(currentRed, currentGreen, currentBlue);
        currentHue = hsv[0];
        currentSaturation = hsv[1];
        currentValue = hsv[2];
        
        hueBar.setHue(currentHue);
        colorPalette.setSaturationValue(currentSaturation, currentValue);
        hexInput.setFromRgb(currentRed, currentGreen, currentBlue);
        colorPreview.setColor(currentRed, currentGreen, currentBlue);
        blockSuggestions.updateSuggestions(currentRed, currentGreen, currentBlue);
        updating = false;
    }
    
    private void openGradientScreen() {
        MinecraftClient.getInstance().setScreen(new GradientScreen(this));
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (colorPalette.mouseClicked(mouseX, mouseY, button)) return true;
        if (hueBar.mouseClicked(mouseX, mouseY, button)) return true;
        if (blockSuggestions.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (colorPalette.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
        if (hueBar.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
        if (blockSuggestions.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        colorPalette.mouseReleased(mouseX, mouseY, button);
        hueBar.mouseReleased(mouseX, mouseY, button);
        if (blockSuggestions.mouseReleased(mouseX, mouseY, button)) return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean shouldPause() { return false; }
}