package com.autopot;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoPot implements ModInitializer {

    public static final String MOD_ID = "autopot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static AutoPotConfig config;

    @Override
    public void onInitialize() {
        config = AutoPotConfig.load();
        LOGGER.info("AutoPot loaded config with activation key {}", config.getActivationKey());
    }

    public static AutoPotConfig getConfig() {
        return config;
    }

    public static void updateConfig(AutoPotConfig newConfig) {
        config = newConfig;
        AutoPotConfig.save(newConfig);
    }
}
