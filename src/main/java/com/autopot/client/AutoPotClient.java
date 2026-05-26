package com.autopot.client;

import com.autopot.AutoPot;
import com.autopot.AutoPotConfig;
import com.autopot.client.ui.AutoPotConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import org.lwjgl.glfw.GLFW;

public class AutoPotClient implements ClientModInitializer {

    private KeyBinding activationKey;
    private KeyBinding configKey;

    private AutoPotConfig config;
    private AutoPotHandler handler;

    @Override
    public void onInitializeClient() {
        config = AutoPot.getConfig();

        activationKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autopot.activate",
                InputUtil.Type.KEYSYM,
                config.getActivationKey().getCode(),
                "key.categories.autopot"
        ));

        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autopot.config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "key.categories.autopot"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (handler == null) {
                handler = new AutoPotHandler(client);
            }

            // Sync config reference in case it was updated
            config = AutoPot.getConfig();

            // Handle activation key
            while (activationKey.wasPressed()) {
                handler.triggerThrows(config);
            }

            // Handle config key
            while (configKey.wasPressed()) {
                client.setScreen(new AutoPotConfigScreen(
                        client.currentScreen,
                        config,
                        updated -> AutoPot.updateConfig(updated)
                ));
            }

            // Tick handler (processes potion queue)
            handler.tick(config);
        });
    }
}
