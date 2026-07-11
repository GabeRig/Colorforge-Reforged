package com.pineapplecompany.colorflow.color;

import com.pineapplecompany.colorflow.ColorflowClient;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GradientGenerator {
    
    public static List<ItemStack> generateGradient(Block startBlock, Block endBlock) {
        return generateGradientWithLength(startBlock, endBlock, 9);
    }
    
    public static List<ItemStack> generateGradientWithLength(Block startBlock, Block endBlock, int steps) {
        List<ItemStack> gradient = new ArrayList<>();
        BlockColorExtractor extractor = ColorflowClient.getBlockColorExtractor();
        
        if (extractor == null || !extractor.isInitialized()) {
            return gradient;
        }
        BlockColorExtractor.ColoredBlock startColored = extractor.getBlockColor(startBlock);
        BlockColorExtractor.ColoredBlock endColored = extractor.getBlockColor(endBlock);
        
        if (startColored == null || endColored == null) {
            gradient.add(new ItemStack(startBlock));
            for (int i = 1; i < steps - 1; i++) {
                gradient.add(new ItemStack(startBlock));
            }
            gradient.add(new ItemStack(endBlock));
            return gradient;
        }
        
        gradient.add(new ItemStack(startBlock));
        for (int i = 1; i < steps - 1; i++) {
            double t = (double) i / (steps - 1);
            double[] interpolatedLab = ColorUtils.lerpLab(startColored.lab, endColored.lab, t);
            
            BlockColorExtractor.ColoredBlock closest = extractor.findClosestBlock(interpolatedLab);
            
            if (closest != null) {
                gradient.add(new ItemStack(closest.item));
            } else {
                gradient.add(new ItemStack(t < 0.5 ? startBlock : endBlock));
            }
        }
        
        gradient.add(new ItemStack(endBlock));
        
        return gradient;
    }
}