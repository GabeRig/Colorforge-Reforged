package com.pineapplecompany.colorflow.gui;

import com.pineapplecompany.colorflow.Colorflow;
import com.pineapplecompany.colorflow.ColorflowClient;
import com.pineapplecompany.colorflow.color.BlockColorExtractor;
import com.pineapplecompany.colorflow.color.ColorUtils;
import com.pineapplecompany.colorflow.color.GradientGenerator;
import com.pineapplecompany.colorflow.gui.theme.Theme;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.EnumSet;

public class GradientScreen extends Screen {
    
    private static final int PANEL_WIDTH = 520;
    private static final int PANEL_HEIGHT = 380;
    
    private final Screen parent;
    
    private Block startBlock = Blocks.WHITE_CONCRETE;
    private Block endBlock = Blocks.BLACK_CONCRETE;
    private List<ItemStack> generatedGradient = null;
    private int gradientLength = 9;
    
    public enum BlockFilter {
        SOLID("Solid"),
        TRANSPARENT("Glass"),
        PARTIAL("Partial");
        
        public final String label;
        BlockFilter(String label) { this.label = label; }
    }
    private Set<BlockFilter> activeFilters = EnumSet.of(BlockFilter.SOLID);
    private List<BlockColorExtractor.ColoredBlock> allBlocks = new ArrayList<>();
    private List<BlockColorExtractor.ColoredBlock> filteredBlocks = new ArrayList<>();
    private int scrollOffset = 0;
    private int selectedSlot = -1;
    private String searchQuery = "";
    private TextFieldWidget searchField;
    private int browserX, browserY, browserW, browserH;
    private int startSlotX, startSlotY, endSlotX, endSlotY;
    private int previewY;
    private int hoveredBrowserSlot = -1;
    private int hoveredPreviewSlot = -1;
    private boolean hoveredStart = false;
    private boolean hoveredEnd = false;
    
    public GradientScreen(Screen parent) {
        super(Text.literal("Gradient Generator"));
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;
        
        BlockColorExtractor ext = ColorflowClient.getBlockColorExtractor();
        if (ext != null && ext.isInitialized()) {
            allBlocks = new ArrayList<>(ext.getAllColoredBlocks());
            applyFilters();
        }
        
        browserX = panelX + 200;
        browserY = panelY + 80;
        browserW = 300;
        browserH = 160;
        startSlotX = panelX + 30;
        startSlotY = panelY + 100;
        endSlotX = panelX + 120;
        endSlotY = panelY + 100;
        previewY = panelY + 290;
        
        searchField = new TextFieldWidget(textRenderer, browserX, browserY - 22, browserW - 10, 16, Text.literal("Search"));
        searchField.setPlaceholder(Text.literal("Search blocks..."));
        searchField.setChangedListener(this::onSearchChanged);
        addDrawableChild(searchField);
        
        addDrawableChild(ButtonWidget.builder(Text.literal("-"), b -> {
            if (gradientLength > 3) { gradientLength--; generateGradient(); }
        }).dimensions(panelX + 30, panelY + 200, 20, 20).build());
        
        addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> {
            if (gradientLength < 15) { gradientLength++; generateGradient(); }
        }).dimensions(panelX + 130, panelY + 200, 20, 20).build());
    
        int filterY = panelY + 55;
        addDrawableChild(ButtonWidget.builder(Text.literal("Solid"), b -> {
            toggleFilter(BlockFilter.SOLID);
            b.setMessage(Text.literal(activeFilters.contains(BlockFilter.SOLID) ? "[Solid]" : "Solid"));
        }).dimensions(browserX, filterY, 60, 16).build());
        
        addDrawableChild(ButtonWidget.builder(Text.literal("Glass"), b -> {
            toggleFilter(BlockFilter.TRANSPARENT);
            b.setMessage(Text.literal(activeFilters.contains(BlockFilter.TRANSPARENT) ? "[Glass]" : "Glass"));
        }).dimensions(browserX + 65, filterY, 60, 16).build());
        
        addDrawableChild(ButtonWidget.builder(Text.literal("Partial"), b -> {
            toggleFilter(BlockFilter.PARTIAL);
            b.setMessage(Text.literal(activeFilters.contains(BlockFilter.PARTIAL) ? "[Partial]" : "Partial"));
        }).dimensions(browserX + 130, filterY, 60, 16).build());
        
        addDrawableChild(ButtonWidget.builder(Text.literal("Generate"), b -> generateGradient())
            .dimensions(panelX + 30, panelY + 230, 80, 20).build());
        
        addDrawableChild(ButtonWidget.builder(Text.literal("Apply Hotbar"), b -> applyToHotbar())
            .dimensions(panelX + 30, panelY + 255, 80, 20).build());
        
        addDrawableChild(ButtonWidget.builder(Text.literal("Swap"), b -> swapBlocks())
            .dimensions(panelX + 70, startSlotY + 50, 40, 16).build());
        
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> close())
            .dimensions(panelX + 15, panelY + PANEL_HEIGHT - 30, 60, 20).build());
        
        addDrawableChild(ButtonWidget.builder(Text.literal("X"), b -> close())
            .dimensions(panelX + PANEL_WIDTH - 25, panelY + 8, 18, 18).build());
        
        generateGradient();
    }
    
    private void toggleFilter(BlockFilter filter) {
        if (activeFilters.contains(filter)) {
            if (activeFilters.size() > 1) activeFilters.remove(filter);
        } else {
            activeFilters.add(filter);
        }
        applyFilters();
    }
    
    private void applyFilters() {
        filteredBlocks = new ArrayList<>();
        for (BlockColorExtractor.ColoredBlock block : allBlocks) {
            boolean match = false;
            
            if (activeFilters.contains(BlockFilter.SOLID) && block.category == BlockColorExtractor.BlockCategory.FULL_BLOCK) {
                match = true;
            }
            if (activeFilters.contains(BlockFilter.TRANSPARENT) && 
                (block.id.getPath().contains("glass") || block.id.getPath().contains("ice"))) {
                match = true;
            }
            if (activeFilters.contains(BlockFilter.PARTIAL) && 
                (block.category == BlockColorExtractor.BlockCategory.SLAB || 
                 block.category == BlockColorExtractor.BlockCategory.STAIRS ||
                 block.category == BlockColorExtractor.BlockCategory.WALL)) {
                match = true;
            }
            
            if (match && matchesSearch(block)) {
                filteredBlocks.add(block);
            }
        }
        scrollOffset = 0;
    }
    
    private boolean matchesSearch(BlockColorExtractor.ColoredBlock block) {
        if (searchQuery.isEmpty()) return true;
        return block.name.toLowerCase().contains(searchQuery) || 
               block.id.getPath().contains(searchQuery) ||
               block.modId.contains(searchQuery);
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
        
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 
            0xF0000000 | (theme.getPanelBackground() & 0x00FFFFFF));
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 5, theme.getPrimaryAccent());
        drawBorder(context, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, theme.getBorder());
        context.drawText(client.textRenderer, "GRADIENT GENERATOR", panelX + 15, panelY + 14, theme.getPrimaryAccent(), true);
        context.drawText(client.textRenderer, "1. Select START/END slot", panelX + 30, panelY + 70, theme.getTextSecondary(), false);
        context.drawText(client.textRenderer, "2. Pick block from browser", panelX + 30, panelY + 82, theme.getTextSecondary(), false);
        
        updateHoverStates(mouseX, mouseY);
        
        drawBlockSlot(context, startSlotX, startSlotY, "START", startBlock, selectedSlot == 0, hoveredStart, theme, client);
        drawBlockSlot(context, endSlotX, endSlotY, "END", endBlock, selectedSlot == 1, hoveredEnd, theme, client);
        context.drawText(client.textRenderer, "->", (startSlotX + endSlotX) / 2 + 12, startSlotY + 18, theme.getTextPrimary(), true);
        
        context.drawText(client.textRenderer, "Length:", panelX + 30, panelY + 188, theme.getTextSecondary(), false);
        String lengthText = String.valueOf(gradientLength);
        int lengthX = panelX + 75 - client.textRenderer.getWidth(lengthText) / 2;
        context.drawText(client.textRenderer, lengthText, lengthX, panelY + 205, theme.getTextPrimary(), true);
        
        drawBlockBrowser(context, mouseX, mouseY, theme, client);
        drawGradientPreview(context, panelX, previewY, mouseX, mouseY, theme, client);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void drawBlockSlot(DrawContext context, int x, int y, String label, Block block, 
                               boolean selected, boolean hovered, Theme theme, MinecraftClient client) {
        int size = 45;
        
        int bg = selected ? theme.getPrimaryAccent() : (hovered ? theme.getHighlight() : theme.getSecondaryBackground());
        context.fill(x, y, x + size, y + size, bg);
        
        int borderColor = selected ? 0xFFFFFFFF : theme.getBorder();
        drawBorder(context, x, y, size, size, borderColor);
        
        int labelX = x + (size - client.textRenderer.getWidth(label)) / 2;
        context.drawText(client.textRenderer, label, labelX, y - 12, theme.getTextPrimary(), true);
        
        if (block != null) {
            context.drawItem(new ItemStack(block), x + 14, y + 14);
            
            BlockColorExtractor.ColoredBlock cb = ColorflowClient.getBlockColorExtractor().getBlockColor(block);
            if (cb != null) {
                context.fill(x + 2, y + size - 6, x + size - 2, y + size - 2, 0xFF000000 | cb.rgb);
            }
        } else {
            context.drawText(client.textRenderer, "?", x + 18, y + 16, theme.getTextSecondary(), false);
        }
    }
    
    private void drawBlockBrowser(DrawContext context, int mouseX, int mouseY, Theme theme, MinecraftClient client) {
        context.fill(browserX, browserY, browserX + browserW, browserY + browserH, 
            0xF0000000 | (theme.getSecondaryBackground() & 0x00FFFFFF));
        drawBorder(context, browserX, browserY, browserW, browserH, theme.getBorder());
        
        String title = "Blocks (" + filteredBlocks.size() + ")";
        context.drawText(client.textRenderer, title, browserX + 5, browserY - 35, theme.getTextPrimary(), false);
        
        int slotSize = 22;
        int cols = (browserW - 12) / (slotSize + 2);
        int rows = (browserH - 4) / (slotSize + 2);
        int visibleSlots = cols * rows;
        
        hoveredBrowserSlot = -1;
        
        for (int i = 0; i < visibleSlots && (scrollOffset + i) < filteredBlocks.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            int slotX = browserX + 4 + col * (slotSize + 2);
            int slotY = browserY + 2 + row * (slotSize + 2);
            
            BlockColorExtractor.ColoredBlock block = filteredBlocks.get(scrollOffset + i);
            
            boolean hovered = mouseX >= slotX && mouseX < slotX + slotSize && 
                             mouseY >= slotY && mouseY < slotY + slotSize;
            if (hovered) hoveredBrowserSlot = scrollOffset + i;
            
            int slotBg = hovered ? theme.getHighlight() : theme.getPanelBackground();
            context.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, slotBg);
            
            context.fill(slotX, slotY, slotX + slotSize, slotY + 1, theme.getBorder());
            context.fill(slotX, slotY + slotSize - 1, slotX + slotSize, slotY + slotSize, theme.getBorder());
            context.fill(slotX, slotY, slotX + 1, slotY + slotSize, theme.getBorder());
            context.fill(slotX + slotSize - 1, slotY, slotX + slotSize, slotY + slotSize, theme.getBorder());
            
            context.drawItem(new ItemStack(block.item), slotX + 3, slotY + 3);
            context.fill(slotX + 2, slotY + slotSize - 3, slotX + slotSize - 2, slotY + slotSize - 1, 0xFF000000 | block.rgb);
        }
        
        if (filteredBlocks.size() > visibleSlots) {
            int scrollBarX = browserX + browserW - 8;
            int scrollBarH = browserH - 4;
            int thumbH = Math.max(15, scrollBarH * visibleSlots / filteredBlocks.size());
            int maxScroll = filteredBlocks.size() - visibleSlots;
            int thumbY = browserY + 2 + (scrollBarH - thumbH) * scrollOffset / Math.max(1, maxScroll);
            
            context.fill(scrollBarX, browserY + 2, scrollBarX + 6, browserY + browserH - 2, theme.getSecondaryBackground());
            context.fill(scrollBarX + 1, thumbY, scrollBarX + 5, thumbY + thumbH, theme.getPrimaryAccent());
        }
        
        // Tooltip
        if (hoveredBrowserSlot >= 0 && hoveredBrowserSlot < filteredBlocks.size()) {
            drawBlockTooltip(context, filteredBlocks.get(hoveredBrowserSlot), mouseX, mouseY, theme, client);
        }
    }
    
    private void drawBlockTooltip(DrawContext context, BlockColorExtractor.ColoredBlock block, 
                                  int mouseX, int mouseY, Theme theme, MinecraftClient client) {
        String name = block.name;
        String hex = ColorUtils.rgbToHex(block.getRed(), block.getGreen(), block.getBlue());
        String category = block.category.displayName;
        
        int tooltipW = Math.max(client.textRenderer.getWidth(name), 80) + 25;
        int tooltipH = 45;
        int tooltipX = mouseX + 15;
        int tooltipY = mouseY;
        
        if (tooltipX + tooltipW > width) tooltipX = mouseX - tooltipW - 5;
        if (tooltipY + tooltipH > height) tooltipY = height - tooltipH - 5;
        
        context.fill(tooltipX, tooltipY, tooltipX + tooltipW, tooltipY + tooltipH, 0xF0101010);
        drawBorder(context, tooltipX, tooltipY, tooltipW, tooltipH, 0xFFFFFFFF);
        
        context.fill(tooltipX + 5, tooltipY + 5, tooltipX + 15, tooltipY + 15, 0xFF000000 | block.rgb);
        context.drawText(client.textRenderer, name, tooltipX + 20, tooltipY + 5, 0xFFFFFFFF, true);
        context.drawText(client.textRenderer, hex, tooltipX + 20, tooltipY + 17, 0xFFFFAA00, false);
        context.drawText(client.textRenderer, category, tooltipX + 5, tooltipY + 30, 0xFF88FF88, false);
    }
    
    private void drawGradientPreview(DrawContext context, int panelX, int y, int mouseX, int mouseY, 
                                     Theme theme, MinecraftClient client) {
        context.drawText(client.textRenderer, "PREVIEW:", panelX + 15, y - 15, theme.getTextSecondary(), false);
        
        if (generatedGradient == null || generatedGradient.isEmpty()) {
            context.drawText(client.textRenderer, "Click 'Generate' to create gradient", panelX + 100, y + 12, theme.getTextSecondary(), false);
            return;
        }
        
        int slotSize = Math.min(32, (PANEL_WIDTH - 40) / generatedGradient.size() - 2);
        int totalW = generatedGradient.size() * (slotSize + 2);
        int startX = panelX + (PANEL_WIDTH - totalW) / 2;
        
        hoveredPreviewSlot = -1;
        
        for (int i = 0; i < generatedGradient.size(); i++) {
            int slotX = startX + i * (slotSize + 2);
            int slotY = y;
            
            boolean hovered = mouseX >= slotX && mouseX < slotX + slotSize && 
                             mouseY >= slotY && mouseY < slotY + slotSize;
            if (hovered) hoveredPreviewSlot = i;
            
            int bg;
            if (i == 0 || i == generatedGradient.size() - 1) {
                bg = theme.getPrimaryAccent();
            } else {
                bg = hovered ? theme.getHighlight() : theme.getSecondaryBackground();
            }
            context.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, bg);
            drawBorder(context, slotX, slotY, slotSize, slotSize, theme.getBorder());
            
            int itemOffset = (slotSize - 16) / 2;
            context.drawItem(generatedGradient.get(i), slotX + itemOffset, slotY + itemOffset);
            
            String num = String.valueOf(i + 1);
            int numX = slotX + (slotSize - client.textRenderer.getWidth(num)) / 2;
            context.drawText(client.textRenderer, num, numX, slotY + slotSize + 2, theme.getTextSecondary(), false);
        }
        
        int barY = y + slotSize + 15;
        int barH = 6;
        for (int i = 0; i < totalW; i++) {
            float t = (float) i / totalW;
            int idx = (int) (t * (generatedGradient.size() - 1));
            int idx2 = Math.min(idx + 1, generatedGradient.size() - 1);
            float localT = (t * (generatedGradient.size() - 1)) - idx;
            
            BlockColorExtractor ext = ColorflowClient.getBlockColorExtractor();
            BlockColorExtractor.ColoredBlock b1 = ext.getBlockColor(
                ((net.minecraft.item.BlockItem)generatedGradient.get(idx).getItem()).getBlock());
            BlockColorExtractor.ColoredBlock b2 = ext.getBlockColor(
                ((net.minecraft.item.BlockItem)generatedGradient.get(idx2).getItem()).getBlock());
            
            if (b1 != null && b2 != null) {
                int r = (int) (b1.getRed() + (b2.getRed() - b1.getRed()) * localT);
                int g = (int) (b1.getGreen() + (b2.getGreen() - b1.getGreen()) * localT);
                int b = (int) (b1.getBlue() + (b2.getBlue() - b1.getBlue()) * localT);
                context.fill(startX + i, barY, startX + i + 1, barY + barH, 0xFF000000 | (r << 16) | (g << 8) | b);
            }
        }
    }
    
    private void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 2, color);
        context.fill(x, y + h - 2, x + w, y + h, color);
        context.fill(x, y, x + 2, y + h, color);
        context.fill(x + w - 2, y, x + w, y + h, color);
    }
    
    private void updateHoverStates(int mouseX, int mouseY) {
        hoveredStart = mouseX >= startSlotX && mouseX < startSlotX + 45 && 
                      mouseY >= startSlotY && mouseY < startSlotY + 45;
        hoveredEnd = mouseX >= endSlotX && mouseX < endSlotX + 45 && 
                    mouseY >= endSlotY && mouseY < endSlotY + 45;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (hoveredStart) { selectedSlot = 0; return true; }
            if (hoveredEnd) { selectedSlot = 1; return true; }
            
            if (hoveredBrowserSlot >= 0 && hoveredBrowserSlot < filteredBlocks.size()) {
                Block block = filteredBlocks.get(hoveredBrowserSlot).block;
                if (selectedSlot == 0) {
                    startBlock = block;
                    Colorflow.LOGGER.info("Set start block: {}", filteredBlocks.get(hoveredBrowserSlot).name);
                } else if (selectedSlot == 1) {
                    endBlock = block;
                    Colorflow.LOGGER.info("Set end block: {}", filteredBlocks.get(hoveredBrowserSlot).name);
                }
                generateGradient();
                return true;
            }
            
            if (hoveredPreviewSlot >= 0 && generatedGradient != null && hoveredPreviewSlot < generatedGradient.size()) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null && client.player.isCreative()) {
                    ItemStack stack = generatedGradient.get(hoveredPreviewSlot).copy();
                    stack.setCount(1);
                    client.player.getInventory().setStack(client.player.getInventory().getSelectedSlot(), stack);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= browserX && mouseX < browserX + browserW && mouseY >= browserY && mouseY < browserY + browserH) {
            int cols = (browserW - 12) / 24;
            int rows = (browserH - 4) / 24;
            int visibleSlots = cols * rows;
            int maxScroll = Math.max(0, filteredBlocks.size() - visibleSlots);
            
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int)(verticalAmount * cols)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    
    private void onSearchChanged(String query) {
        searchQuery = query.toLowerCase();
        applyFilters();
    }
    
    private void generateGradient() {
        if (startBlock != null && endBlock != null) {
            generatedGradient = GradientGenerator.generateGradientWithLength(startBlock, endBlock, gradientLength);
            Colorflow.LOGGER.info("Generated {}-step gradient from {} to {}", 
                gradientLength, 
                startBlock.getName().getString(), 
                endBlock.getName().getString());
        }
    }
    
    private void applyToHotbar() {
        if (generatedGradient == null) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !client.player.isCreative()) return;
        
        for (int i = 0; i < Math.min(9, generatedGradient.size()); i++) {
            ItemStack stack = generatedGradient.get(i).copy();
            stack.setCount(1);
            client.player.getInventory().setStack(i, stack);
        }
        Colorflow.LOGGER.info("Applied {}-step gradient to hotbar!", Math.min(9, generatedGradient.size()));
    }
    
    private void swapBlocks() {
        Block temp = startBlock;
        startBlock = endBlock;
        endBlock = temp;
        generateGradient();
    }
    
    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }
    
    @Override
    public boolean shouldPause() { return false; }
}