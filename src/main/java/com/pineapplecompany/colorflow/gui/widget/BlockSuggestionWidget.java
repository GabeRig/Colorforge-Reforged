package com.pineapplecompany.colorflow.gui.widget;

import com.pineapplecompany.colorflow.Colorflow;
import com.pineapplecompany.colorflow.ColorflowClient;
import com.pineapplecompany.colorflow.color.BlockColorExtractor;
import com.pineapplecompany.colorflow.color.ColorUtils;
import com.pineapplecompany.colorflow.gui.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BlockSuggestionWidget implements Drawable, Element {
    
    private static final int SLOT_SIZE = 26;
    private static final int SLOT_SPACING = 4;
    private static final int COLUMNS = 3;
    private static final int ROWS = 3;
    private static final int TOTAL_SLOTS = COLUMNS * ROWS;
    
    private final int x, y, width, height;
    private List<BlockColorExtractor.ColoredBlock> suggestions = new ArrayList<>();
    private int hoveredSlot = -1;
    private boolean focused = false;
    private Consumer<BlockColorExtractor.ColoredBlock> onBlockSelected;
    private ItemStack draggedStack = ItemStack.EMPTY;
    private boolean isDragging = false;
    
    public BlockSuggestionWidget(int x, int y) {
        this.x = x;
        this.y = y;
        this.width = COLUMNS * (SLOT_SIZE + SLOT_SPACING) + 12;
        this.height = ROWS * (SLOT_SIZE + SLOT_SPACING) + 38;
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Theme theme = ColorflowClient.getCurrentTheme();
        MinecraftClient client = MinecraftClient.getInstance();
        context.fill(x, y, x + width, y + height, 0xF0000000 | (theme.getSecondaryBackground() & 0x00FFFFFF));
        
        int border = theme.getBorder();
        context.fill(x, y, x + width, y + 2, border);
        context.fill(x, y + height - 2, x + width, y + height, border);
        context.fill(x, y, x + 2, y + height, border);
        context.fill(x + width - 2, y, x + width, y + height, border);
        
        String title = "MATCHES";
        context.drawText(client.textRenderer, title, x + 6, y + 6, theme.getPrimaryAccent(), true);
        context.drawText(client.textRenderer, "(" + suggestions.size() + ")", x + width - 25, y + 6, theme.getTextSecondary(), false);
        
        int slotStartX = x + 6;
        int slotStartY = y + 22;
        
        hoveredSlot = -1;
        
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            int col = i % COLUMNS;
            int row = i / COLUMNS;
            int slotX = slotStartX + col * (SLOT_SIZE + SLOT_SPACING);
            int slotY = slotStartY + row * (SLOT_SIZE + SLOT_SPACING);
            
            boolean hovered = mouseX >= slotX && mouseX < slotX + SLOT_SIZE &&
                             mouseY >= slotY && mouseY < slotY + SLOT_SIZE;
            if (hovered) hoveredSlot = i;
            
            int slotBg = hovered ? theme.getHighlight() : theme.getPanelBackground();
            context.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, slotBg);
            
            int slotBorder = hovered ? theme.getPrimaryAccent() : theme.getBorder();
            context.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + 2, slotBorder);
            context.fill(slotX, slotY + SLOT_SIZE - 2, slotX + SLOT_SIZE, slotY + SLOT_SIZE, slotBorder);
            context.fill(slotX, slotY, slotX + 2, slotY + SLOT_SIZE, slotBorder);
            context.fill(slotX + SLOT_SIZE - 2, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, slotBorder);
            
            if (i < suggestions.size()) {
                BlockColorExtractor.ColoredBlock block = suggestions.get(i);
                context.drawItem(new ItemStack(block.item), slotX + 5, slotY + 5);
                context.fill(slotX + 3, slotY + SLOT_SIZE - 6, slotX + SLOT_SIZE - 3, slotY + SLOT_SIZE - 2, 
                    0xFF000000 | block.rgb);
            }
        }
        
        context.drawText(client.textRenderer, "Click to get block", x + 6, y + height - 12, theme.getTextSecondary(), false);
        
        if (hoveredSlot >= 0 && hoveredSlot < suggestions.size()) {
            renderTooltip(context, suggestions.get(hoveredSlot), mouseX, mouseY, client, theme);
        }
        
        if (isDragging && !draggedStack.isEmpty()) {
            context.drawItem(draggedStack, (int)mouseX - 8, (int)mouseY - 8);
        }
    }
    
    private void renderTooltip(DrawContext context, BlockColorExtractor.ColoredBlock block, 
                               int mouseX, int mouseY, MinecraftClient client, Theme theme) {
        String name = block.name;
        String hex = ColorUtils.rgbToHex(block.getRed(), block.getGreen(), block.getBlue());
        String category = block.category.displayName;
        String mod = "[" + block.modId + "]";
        
        int maxW = Math.max(client.textRenderer.getWidth(name),
                  Math.max(client.textRenderer.getWidth(hex),
                  Math.max(client.textRenderer.getWidth(category),
                           client.textRenderer.getWidth(mod))));
        
        int tooltipW = maxW + 35;
        int tooltipH = 70;
        int tooltipX = mouseX + 15;
        int tooltipY = mouseY;
        
        if (tooltipX + tooltipW > client.getWindow().getScaledWidth()) {
            tooltipX = mouseX - tooltipW - 10;
        }
        if (tooltipY + tooltipH > client.getWindow().getScaledHeight()) {
            tooltipY = client.getWindow().getScaledHeight() - tooltipH - 5;
        }
        
        context.fill(tooltipX, tooltipY, tooltipX + tooltipW, tooltipY + tooltipH, 0xF0101010);
        context.fill(tooltipX, tooltipY, tooltipX + tooltipW, tooltipY + 1, 0xFFFFFFFF);
        context.fill(tooltipX, tooltipY + tooltipH - 1, tooltipX + tooltipW, tooltipY + tooltipH, 0xFFFFFFFF);
        context.fill(tooltipX, tooltipY, tooltipX + 1, tooltipY + tooltipH, 0xFFFFFFFF);
        context.fill(tooltipX + tooltipW - 1, tooltipY, tooltipX + tooltipW, tooltipY + tooltipH, 0xFFFFFFFF);
        
        context.fill(tooltipX + 5, tooltipY + 5, tooltipX + 17, tooltipY + 17, 0xFF000000 | block.rgb);
        context.drawText(client.textRenderer, name, tooltipX + 22, tooltipY + 6, 0xFFFFFFFF, true);
        context.drawText(client.textRenderer, hex, tooltipX + 22, tooltipY + 20, 0xFFFFAA00, true);
        context.drawText(client.textRenderer, category, tooltipX + 6, tooltipY + 38, 0xFF88FF88, false);
        context.drawText(client.textRenderer, mod, tooltipX + 6, tooltipY + 52, 0xFF8888FF, false);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hoveredSlot >= 0 && hoveredSlot < suggestions.size()) {
            BlockColorExtractor.ColoredBlock block = suggestions.get(hoveredSlot);
            MinecraftClient client = MinecraftClient.getInstance();
            
            if (client.player != null && client.player.isCreative()) {
                ItemStack stack = new ItemStack(block.item, 1);
                
                if (Screen.hasShiftDown()) {
                    client.player.getInventory().insertStack(stack);
                    Colorflow.LOGGER.info("Added 1x {} to inventory", block.name);
                } else {
                    int slot = client.player.getInventory().selectedSlot;
                    client.player.getInventory().setStack(slot, stack);
                    Colorflow.LOGGER.info("Added 1x {} to hotbar slot {}", block.name, slot + 1);
                }
                
                if (onBlockSelected != null) onBlockSelected.accept(block);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && hoveredSlot >= 0 && hoveredSlot < suggestions.size() && !isDragging) {
            draggedStack = new ItemStack(suggestions.get(hoveredSlot).item, 1);
            isDragging = true;
        }
        return isDragging;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (isDragging && !draggedStack.isEmpty()) {
          MinecraftClient client = MinecraftClient.getInstance();
          if (client.player != null && client.player.isCreative() 
              && mouseY > client.getWindow().getScaledHeight() - 80) {
              client.player.getInventory().insertStack(draggedStack.copy());
          }
          draggedStack = ItemStack.EMPTY;
          isDragging = false;
          return true;
      }
      return false;
    }
    
    public void updateSuggestions(int r, int g, int b) {
        BlockColorExtractor ext = ColorflowClient.getBlockColorExtractor();
        suggestions = (ext != null && ext.isInitialized()) ? ext.findClosestBlocks(r, g, b, TOTAL_SLOTS) : new ArrayList<>();
    }
    
    public void setOnBlockSelected(Consumer<BlockColorExtractor.ColoredBlock> cb) { this.onBlockSelected = cb; }
    public List<BlockColorExtractor.ColoredBlock> getSuggestions() { return suggestions; }
    @Override public void setFocused(boolean f) { this.focused = f; }
    @Override public boolean isFocused() { return focused; }
}