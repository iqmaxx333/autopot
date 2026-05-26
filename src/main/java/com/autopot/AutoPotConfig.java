package com.autopot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.util.InputUtil;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class AutoPotConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("autopot.json");

    // Stored as translation string (e.g. "key.keyboard.r")
    private String activationKey = InputUtil.Type.KEYSYM.createFromCode(82).getTranslationKey(); // R by default

    private boolean useFireResistance = false;
    private boolean useStrength = false;
    private boolean useSpeed = false;
    private boolean autoLookDown = true;

    public AutoPotConfig() {
        // defaults above
    }

    public static AutoPotConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                AutoPotConfig loaded = GSON.fromJson(reader, AutoPotConfig.class);
                if (loaded != null) return loaded;
            } catch (IOException | JsonParseException e) {
                AutoPot.LOGGER.warn("Failed to read config, falling back to defaults", e);
            }
        }
        return new AutoPotConfig();
    }

    public static void save(AutoPotConfig config) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            AutoPot.LOGGER.error("Failed to save config", e);
        }
    }

    public AutoPotConfig copy() {
        AutoPotConfig c = new AutoPotConfig();
        c.activationKey = this.activationKey;
        c.useFireResistance = this.useFireResistance;
        c.useStrength = this.useStrength;
        c.useSpeed = this.useSpeed;
        c.autoLookDown = this.autoLookDown;
        return c;
    }

    // Key stored as translation key string for JSON serialisation
    public InputUtil.Key getActivationKey() {
        try {
            return InputUtil.fromTranslationKey(activationKey);
        } catch (Exception e) {
            return InputUtil.Type.KEYSYM.createFromCode(82);
        }
    }

    public void setActivationKey(InputUtil.Key key) {
        this.activationKey = key.getTranslationKey();
    }

    public boolean isUseFireResistance() { return useFireResistance; }
    public void setUseFireResistance(boolean v) { useFireResistance = v; }

    public boolean isUseStrength() { return useStrength; }
    public void setUseStrength(boolean v) { useStrength = v; }

    public boolean isUseSpeed() { return useSpeed; }
    public void setUseSpeed(boolean v) { useSpeed = v; }

    public boolean isAutoLookDown() { return autoLookDown; }
    public void setAutoLookDown(boolean v) { autoLookDown = v; }
}
