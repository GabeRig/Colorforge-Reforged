package com.pineapplecompany.colorflow.keybind;

import com.pineapplecompany.colorflow.Colorflow;
import com.pineapplecompany.colorflow.ColorflowClient;
import com.pineapplecompany.colorflow.gui.ColorPickerScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class KeybindHandler {
    
    private static KeyBinding openColorPickerKey;
    private static KeyBinding cycleThemeKey;
    
    public static void register() {
        openColorPickerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.colorflow.open_picker",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "category.colorflow.general"
        ));
        
        cycleThemeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.colorflow.cycle_theme",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "category.colorflow.general"
        ));
        
        ClientTickEvents.END_CLIENT_TICK.register(KeybindHandler::onClientTick);
        
        Colorflow.LOGGER.info("Keybindings registered: C = Open Picker, V = Cycle Theme");
    }
    
    private static void onClientTick(MinecraftClient client) {
        if (client.player == null) return;
        
        while (openColorPickerKey.wasPressed()) {
            openColorPicker(client);
        }
        
        while (cycleThemeKey.wasPressed()) {
            cycleTheme(client);
        }
    }
    
    private static void openColorPicker(MinecraftClient client) {
        if (client.currentScreen == null) {
            if (client.player.isCreative()) {
                client.setScreen(new ColorPickerScreen());
            } else {
                client.player.sendMessage(
                    Text.literal("§c[Colorflow] Creative mode required!"),
                    true
                );
            }
        }
    }
    
    private static void cycleTheme(MinecraftClient client) {
        ColorflowClient.cycleTheme();
        
        if (client.player != null) {
            String themeName = ColorflowClient.getCurrentTheme().getName();
            client.player.sendMessage(
                Text.literal("§d[Colorflow] Theme: " + themeName),
                true
            );
        }
    }
    
    public static KeyBinding getOpenColorPickerKey() {
        return openColorPickerKey;
    }
    
    public static KeyBinding getCycleThemeKey() {
        return cycleThemeKey;
    }
}