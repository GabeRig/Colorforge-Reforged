package com.pineapplecompany.colorflow.color;

import com.pineapplecompany.colorflow.Colorflow;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.SignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.HangingSignBlock;
import net.minecraft.block.WallHangingSignBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.stream.Collectors;

public class BlockColorExtractor {
    public enum BlockCategory {
        FULL_BLOCK("Full Blocks"),
        SLAB("Slabs"),
        STAIRS("Stairs"),
        WALL("Walls"),
        FENCE("Fences & Gates"),
        PANE("Panes & Bars"),
        DOOR("Doors & Trapdoors"),
        BUTTON("Buttons & Plates"),
        SIGN("Signs"),
        OTHER("Other");
        
        public final String displayName;
        
        BlockCategory(String displayName) {
            this.displayName = displayName;
        }
    }
    
    public static class ColoredBlock {
        public final Block block;
        public final Item item;
        public final Identifier id;
        public final int rgb;
        public final double[] lab;
        public final String name;
        public final BlockCategory category;
        public final String modId;
        
        public ColoredBlock(Block block, int rgb, BlockCategory category) {
            this.block = block;
            this.item = block.asItem();
            this.id = Registries.BLOCK.getId(block);
            this.rgb = rgb;
            this.lab = ColorUtils.rgbToLab(
                (rgb >> 16) & 0xFF,
                (rgb >> 8) & 0xFF,
                rgb & 0xFF
            );
            this.name = formatBlockName(id.getPath());
            this.category = category;
            this.modId = id.getNamespace();
        }
        
        private String formatBlockName(String path) {
            String[] words = path.split("_");
            StringBuilder result = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    result.append(Character.toUpperCase(word.charAt(0)))
                          .append(word.substring(1))
                          .append(" ");
                }
            }
            return result.toString().trim();
        }
        
        public int getRed() { return (rgb >> 16) & 0xFF; }
        public int getGreen() { return (rgb >> 8) & 0xFF; }
        public int getBlue() { return rgb & 0xFF; }
        
        public boolean isFullBlock() {
            return category == BlockCategory.FULL_BLOCK;
        }
    }
    
    private final List<ColoredBlock> allBlocks = new ArrayList<>();
    private final Map<BlockCategory, List<ColoredBlock>> blocksByCategory = new HashMap<>();
    private final Map<String, List<ColoredBlock>> blocksByMod = new HashMap<>();
    
    private boolean initialized = false;
    private Set<BlockCategory> enabledCategories = EnumSet.allOf(BlockCategory.class);
    
    private static final Set<String> EXCLUDED_BLOCKS = Set.of(
        "air", "cave_air", "void_air",
        "barrier", "light",
        "nether_portal", "end_portal", "end_gateway",
        "fire", "soul_fire",
        "moving_piston", "bubble_column",
        "debug_stick"
    );
    
    public void initialize() {
        if (initialized) return;
        
        Colorflow.LOGGER.info("╔════════════════════════════════════════════════════════╗");
        Colorflow.LOGGER.info("║         COLORFLOW - Block Color Extraction             ║");
        Colorflow.LOGGER.info("╚════════════════════════════════════════════════════════╝");
        
        long startTime = System.currentTimeMillis();
        
        MinecraftClient client = MinecraftClient.getInstance();
        for (BlockCategory cat : BlockCategory.values()) {
            blocksByCategory.put(cat, new ArrayList<>());
        }
        
        int totalBlocks = 0;
        int processedBlocks = 0;
        int excludedBlocks = 0;
        int noItemBlocks = 0;
        int noColorBlocks = 0;
        
        Map<String, Integer> modCounts = new HashMap<>();
        Map<BlockCategory, Integer> categoryCounts = new HashMap<>();
        
        for (Block block : Registries.BLOCK) {
            totalBlocks++;
            Identifier id = Registries.BLOCK.getId(block);
            String path = id.getPath();
            String modId = id.getNamespace();
            if (isExcluded(path)) {
                excludedBlocks++;
                continue;
            }
            
            Item item = block.asItem();
            if (!(item instanceof BlockItem)) {
                noItemBlocks++;
                continue;
            }
            
            int color = extractColor(block, client);
            if (color == -1) {
                noColorBlocks++;
                continue;
            }
            
            BlockCategory category = categorizeBlock(block);
            ColoredBlock coloredBlock = new ColoredBlock(block, color, category);
            allBlocks.add(coloredBlock);
            blocksByCategory.get(category).add(coloredBlock);
            
            // Track by mod
            blocksByMod.computeIfAbsent(modId, k -> new ArrayList<>()).add(coloredBlock);
            
            // Update counts
            modCounts.merge(modId, 1, Integer::sum);
            categoryCounts.merge(category, 1, Integer::sum);
            
            processedBlocks++;
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        
        // Beautiful logging
        Colorflow.LOGGER.info("┌──────────────────────────────────────────────────────┐");
        Colorflow.LOGGER.info("│ Extraction Complete!                                 │");
        Colorflow.LOGGER.info("├──────────────────────────────────────────────────────┤");
        Colorflow.LOGGER.info("│ Total blocks scanned:    {:>6}                      │", totalBlocks);
        Colorflow.LOGGER.info("│ Blocks with colors:      {:>6}                      │", processedBlocks);
        Colorflow.LOGGER.info("│ Excluded (invisible):    {:>6}                      │", excludedBlocks);
        Colorflow.LOGGER.info("│ No item (unobtainable):  {:>6}                      │", noItemBlocks);
        Colorflow.LOGGER.info("│ No color data:           {:>6}                      │", noColorBlocks);
        Colorflow.LOGGER.info("│ Time elapsed:            {:>6}ms                    │", elapsed);
        Colorflow.LOGGER.info("├──────────────────────────────────────────────────────┤");
        Colorflow.LOGGER.info("│ By Category:                                         │");
        
        for (BlockCategory cat : BlockCategory.values()) {
            int count = categoryCounts.getOrDefault(cat, 0);
            if (count > 0) {
                Colorflow.LOGGER.info("│   {:.<25} {:>5} blocks           │", cat.displayName, count);
            }
        }
        
        Colorflow.LOGGER.info("├──────────────────────────────────────────────────────┤");
        Colorflow.LOGGER.info("│ By Mod:                                              │");
        
        modCounts.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(10)
            .forEach(entry -> {
                Colorflow.LOGGER.info("│   {:.<25} {:>5} blocks           │", 
                    entry.getKey(), entry.getValue());
            });
        
        if (modCounts.size() > 10) {
            Colorflow.LOGGER.info("│   ... and {} more mods                              │", 
                modCounts.size() - 10);
        }
        
        Colorflow.LOGGER.info("└──────────────────────────────────────────────────────┘");
        
        initialized = true;
    }
    
    private boolean isExcluded(String path) {
        for (String excluded : EXCLUDED_BLOCKS) {
            if (path.equals(excluded) || path.contains(excluded + "_")) {
                return true;
            }
        }
        return false;
    }
    
    private BlockCategory categorizeBlock(Block block) {
        if (block instanceof SlabBlock) return BlockCategory.SLAB;
        if (block instanceof StairsBlock) return BlockCategory.STAIRS;
        if (block instanceof WallBlock) return BlockCategory.WALL;
        if (block instanceof FenceBlock || block instanceof FenceGateBlock) return BlockCategory.FENCE;
        if (block instanceof PaneBlock) return BlockCategory.PANE;
        if (block instanceof DoorBlock || block instanceof TrapdoorBlock) return BlockCategory.DOOR;
        if (block instanceof ButtonBlock || block instanceof PressurePlateBlock) return BlockCategory.BUTTON;
        if (block instanceof SignBlock || block instanceof WallSignBlock || 
            block instanceof HangingSignBlock || block instanceof WallHangingSignBlock) return BlockCategory.SIGN;
        
        String path = Registries.BLOCK.getId(block).getPath();
        if (path.contains("_slab")) return BlockCategory.SLAB;
        if (path.contains("_stairs")) return BlockCategory.STAIRS;
        if (path.contains("_wall") && !path.contains("wall_")) return BlockCategory.WALL;
        if (path.contains("_fence")) return BlockCategory.FENCE;
        if (path.contains("_pane") || path.contains("_bars")) return BlockCategory.PANE;
        if (path.contains("_door") || path.contains("trapdoor")) return BlockCategory.DOOR;
        if (path.contains("_button") || path.contains("pressure_plate")) return BlockCategory.BUTTON;
        if (path.contains("_sign")) return BlockCategory.SIGN;
        try {
            var shape = block.getDefaultState().getOutlineShape(null, null);
            if (!shape.isEmpty()) {
                var box = shape.getBoundingBox();
                boolean isFull = box.minX <= 0.01 && box.minY <= 0.01 && box.minZ <= 0.01 &&
                                box.maxX >= 0.99 && box.maxY >= 0.99 && box.maxZ >= 0.99;
                if (isFull) return BlockCategory.FULL_BLOCK;
            }
        } catch (Exception e) {
            return BlockCategory.FULL_BLOCK;
        }
        
        return BlockCategory.OTHER;
    }
    
    private int extractColor(Block block, MinecraftClient client) {
        try {
            if (client != null && client.world != null) {
                try {
                    MapColor mapColor = block.getDefaultState().getMapColor(
                        client.world,
                        BlockPos.ORIGIN
                    );
                    if (mapColor != null && mapColor != MapColor.CLEAR && mapColor.color != 0) {
                        return mapColor.color;
                    }
                } catch (Exception e) {
                }
            }
            
            try {
                MapColor mapColor = block.getDefaultMapColor();
                if (mapColor != null && mapColor != MapColor.CLEAR && mapColor.color != 0) {
                    return mapColor.color;
                }
            } catch (Exception e) {
            }
            
        } catch (Exception e) {
        }
        
        return -1;
    }
    
    public void setEnabledCategories(Set<BlockCategory> categories) {
        this.enabledCategories = EnumSet.copyOf(categories);
    }
    
    public void enableCategory(BlockCategory category) {
        enabledCategories.add(category);
    }
    
    public void disableCategory(BlockCategory category) {
        enabledCategories.remove(category);
    }
    
    public void setFullBlocksOnly(boolean fullOnly) {
        if (fullOnly) {
            enabledCategories = EnumSet.of(BlockCategory.FULL_BLOCK);
        } else {
            enabledCategories = EnumSet.allOf(BlockCategory.class);
        }
    }
    
    public Set<BlockCategory> getEnabledCategories() {
        return EnumSet.copyOf(enabledCategories);
    }
    
    public List<ColoredBlock> findClosestBlocks(int r, int g, int b, int count) {
        if (!initialized) initialize();
        
        if (allBlocks.isEmpty()) {
            return new ArrayList<>();
        }
        
        double[] targetLab = ColorUtils.rgbToLab(r, g, b);
        
        return allBlocks.stream()
            .filter(block -> enabledCategories.contains(block.category))
            .map(block -> new AbstractMap.SimpleEntry<>(
                block, 
                ColorUtils.deltaE2000(targetLab, block.lab)
            ))
            .sorted(Comparator.comparingDouble(Map.Entry::getValue))
            .limit(count)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    public ColoredBlock findClosestBlock(int r, int g, int b) {
        List<ColoredBlock> closest = findClosestBlocks(r, g, b, 1);
        return closest.isEmpty() ? null : closest.get(0);
    }
    
    public ColoredBlock findClosestBlock(double[] lab) {
        if (!initialized) initialize();
        
        ColoredBlock closest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (ColoredBlock block : allBlocks) {
            if (!enabledCategories.contains(block.category)) continue;
            
            double distance = ColorUtils.deltaE2000(lab, block.lab);
            if (distance < minDistance) {
                minDistance = distance;
                closest = block;
            }
        }
        
        return closest;
    }
    
    public List<ColoredBlock> getAllColoredBlocks() {
        if (!initialized) initialize();
        return Collections.unmodifiableList(allBlocks);
    }
    
    public List<ColoredBlock> getBlocksByCategory(BlockCategory category) {
        if (!initialized) initialize();
        return Collections.unmodifiableList(blocksByCategory.getOrDefault(category, new ArrayList<>()));
    }
    
    public List<ColoredBlock> getBlocksByMod(String modId) {
        if (!initialized) initialize();
        return Collections.unmodifiableList(blocksByMod.getOrDefault(modId, new ArrayList<>()));
    }
    
    public Set<String> getLoadedMods() {
        if (!initialized) initialize();
        return Collections.unmodifiableSet(blocksByMod.keySet());
    }
    
    public ColoredBlock getBlockColor(Block block) {
        if (!initialized) initialize();
        
        for (ColoredBlock colored : allBlocks) {
            if (colored.block == block) {
                return colored;
            }
        }
        return null;
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    public int getBlockCount() {
        return allBlocks.size();
    }
    
    public int getBlockCount(BlockCategory category) {
        return blocksByCategory.getOrDefault(category, new ArrayList<>()).size();
    }
    
    public void reload() {
        allBlocks.clear();
        blocksByCategory.clear();
        blocksByMod.clear();
        initialized = false;
        initialize();
    }
}