package com.pixelindiedev.lazy_ai_pixelindiedev.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "lazy-ai.json";
    public static final File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), FILE_NAME);
    private static final Logger LOGGER = LoggerFactory.getLogger("LazyAI");
    public DistanceScalingType DistanceScaling = ModConfigDefaults.Defaults_DistanceScaling;
    public OptimalizationType AIOptimizationType = ModConfigDefaults.Defaults_AIOptimizationType;
    //    Distance in squared blocks
    //    distance is based on simulation distance
    public int BlockDistance_Close = ModConfigDefaults.Defaults_BlockDistance_Close;
    public int BlockDistance_Far = ModConfigDefaults.Defaults_BlockDistance_Far;
    public TemptDelayEnum TemptDelay = ModConfigDefaults.Defaults_TemptDelay;
    public boolean DisableZombieEggStomping = ModConfigDefaults.Defaults_DisableZombieEggStomping;
    public boolean NeverSlowdownDistantMobs = ModConfigDefaults.Defaults_NeverSlowdownDistantMobs;
    public transient long lastModified = 0L;

    public static ModConfig load() {
        ModConfig config = new ModConfig();
        JsonObject obj = new JsonObject();
        boolean changed = false;

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                JsonElement element = JsonParser.parseReader(reader);
                if (element.isJsonObject()) obj = element.getAsJsonObject();
            } catch (IOException e) {
                LOGGER.error("Failed to read config, restoring defaults.", e);
                config = new ModConfig();
            }
        } else {
            LOGGER.warn("Config file not found, creating a new one.");
            config = new ModConfig();
            changed = true;
        }

        // Check for missing options
        if (!obj.has("DistanceScaling")) {
            var value = ModConfigDefaults.Defaults_DistanceScaling.name();
            LOGGER.warn("Missing option 'DistanceScaling', adding default (" + value + ").");
            obj.addProperty("DistanceScaling", value);
            changed = true;
        }
        if (!obj.has("AIOptimizationType")) {
            var value = ModConfigDefaults.Defaults_AIOptimizationType.name();
            LOGGER.warn("Missing option 'AIOptimizationType', adding default (" + value + ").");
            obj.addProperty("AIOptimizationType", value);
            changed = true;
        }
        if (!obj.has("BlockDistance_Close")) {
            var value = ModConfigDefaults.Defaults_BlockDistance_Close;
            LOGGER.warn("Missing option 'BlockDistance_Close', adding default (" + value + ").");
            obj.addProperty("BlockDistance_Close", value);
            changed = true;
        }
        if (!obj.has("BlockDistance_Far")) {
            var value = ModConfigDefaults.Defaults_BlockDistance_Far;
            LOGGER.warn("Missing option 'BlockDistance_Far', adding default (" + value + ").");
            obj.addProperty("BlockDistance_Far", value);
            changed = true;
        }
        if (!obj.has("TemptDelay")) {
            var value = ModConfigDefaults.Defaults_TemptDelay.name();
            LOGGER.warn("Missing option 'TemptDelay', adding default (" + value + ").");
            obj.addProperty("TemptDelay", value);
            changed = true;
        }
        if (!obj.has("DisableZombieEggStomping")) {
            var value = ModConfigDefaults.Defaults_DisableZombieEggStomping;
            LOGGER.warn("Missing option 'DisableZombieEggStomping', adding default (" + value + ").");
            obj.addProperty("DisableZombieEggStomping", value);
            changed = true;
        }
        if (!obj.has("NeverSlowdownDistantMobs")) {
            var value = ModConfigDefaults.Defaults_NeverSlowdownDistantMobs;
            LOGGER.warn("Missing option 'NeverSlowdownDistantMobs', adding default (" + value + ").");
            obj.addProperty("NeverSlowdownDistantMobs", value);
            changed = true;
        }

        config = GSON.fromJson(obj, ModConfig.class);

        //Null check
        if (config.DistanceScaling == null) {
            var value = ModConfigDefaults.Defaults_DistanceScaling;
            LOGGER.warn("Invalid DistanceScaling value in config, using default (" + value + ").");
            config.DistanceScaling = value;
            changed = true;
        }
        if (config.AIOptimizationType == null) {
            var value = ModConfigDefaults.Defaults_AIOptimizationType;
            LOGGER.warn("Invalid AIOptimizationType value in config, using default (" + value + ").");
            config.AIOptimizationType = value;
            changed = true;
        }
        if (config.TemptDelay == null) {
            var value = ModConfigDefaults.Defaults_TemptDelay;
            LOGGER.warn("Invalid TemptDelay value, using default (" + value + ").");
            config.TemptDelay = value;
            changed = true;
        }

        if (changed) {
            config.save();
            config.lastModified = configFile.lastModified();
        }

        return config;
    }

    public void save() {
        File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), FILE_NAME);
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(this, writer);
            lastModified = configFile.lastModified();
        } catch (IOException e) {
            LOGGER.error("Failed to save config:", e);
        }
    }

    public boolean hasExternalChange() {
        return configFile.exists() && configFile.lastModified() != lastModified;
    }

    private int getMultiplierUsingDistanceScaling(int MediumDistanceValue) {
        return switch (DistanceScaling) {
            case Close -> (MediumDistanceValue * 2);
            case Far -> (MediumDistanceValue / 2);
            default -> MediumDistanceValue;
        };
    }

    public int getBlockDistance_Close_Multiplier() {
        return getMultiplierUsingDistanceScaling(10);
    }

    public int getBlockDistance_Far_Multiplier() {
        return getMultiplierUsingDistanceScaling(5);
    }
}
