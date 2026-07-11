package com.pineapplecompany.colorflow;

import com.pineapplecompany.colorflow.color.BlockColorExtractor;
import com.pineapplecompany.colorflow.gui.theme.Theme;
import com.pineapplecompany.colorflow.keybind.KeybindHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ColorflowClient implements ClientModInitializer {
    
    private static BlockColorExtractor blockColorExtractor;
    private static Theme currentTheme = Theme.CHERRY_BLOSSOM;
    
    @Override
    public void onInitializeClient() {
        Colorflow.LOGGER.info("═══════════════════════════════════════════════════════");
        Colorflow.LOGGER.info("  Colorflow v{} - Initializing...", Colorflow.VERSION);
        Colorflow.LOGGER.info("═══════════════════════════════════════════════════════");
        
        KeybindHandler.register();
        
        blockColorExtractor = new BlockColorExtractor();
        blockColorExtractor.initialize();
        
        Colorflow.LOGGER.info("  ✓ Keybindings registered (Press C to open)");
        Colorflow.LOGGER.info("  ✓ {} blocks registered!", blockColorExtractor.getBlockCount());
        Colorflow.LOGGER.info("═══════════════════════════════════════════════════════");
    }
    
    public static BlockColorExtractor getBlockColorExtractor() {
        return blockColorExtractor;
    }
    
    public static Theme getCurrentTheme() {
        return currentTheme;
    }
    
    public static void setCurrentTheme(Theme theme) {
        currentTheme = theme;
        Colorflow.LOGGER.info("Theme changed to: {}", theme.getName());
    }
    
    public static void cycleTheme() {
        Theme[] themes = Theme.values();
        int currentIndex = currentTheme.ordinal();
        int nextIndex = (currentIndex + 1) % themes.length;
        setCurrentTheme(themes[nextIndex]);
    }
    
    public static boolean isReady() {
        return blockColorExtractor != null && blockColorExtractor.getBlockCount() > 0;
    }
}