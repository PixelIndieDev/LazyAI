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
    public DistanceScalingType DistanceScaling = DistanceScalingType.Medium;
    public OptimalizationType AIOptimizationType = OptimalizationType.Default;
    //    Distance in squared blocks
    public int BlockDistance_Close = 64;
    public int BlockDistance_Far = 196;
    public TemptDelayEnum TemptDelay = TemptDelayEnum.Low;
    public boolean DisableZombieEggStomping = false;
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
            LOGGER.warn("Missing option 'DistanceScaling', adding default (Medium).");
            obj.addProperty("DistanceScaling", DistanceScalingType.Medium.name());
            changed = true;
        }
        if (!obj.has("AIOptimizationType")) {
            LOGGER.warn("Missing option 'AIOptimizationType', adding default (Default).");
            obj.addProperty("AIOptimizationType", OptimalizationType.Default.name());
            changed = true;
        }
        if (!obj.has("BlockDistance_Close")) {
            LOGGER.warn("Missing option 'BlockDistance_Close', adding default (64).");
            obj.addProperty("BlockDistance_Close", 64);
            changed = true;
        }
        if (!obj.has("BlockDistance_Far")) {
            LOGGER.warn("Missing option 'BlockDistance_Far', adding default (196).");
            obj.addProperty("BlockDistance_Far", 196);
            changed = true;
        }
        if (!obj.has("TemptDelay")) {
            LOGGER.warn("Missing option 'TemptDelay', adding default (Low).");
            obj.addProperty("TemptDelay", TemptDelayEnum.Low.name());
            changed = true;
        }
        if (!obj.has("DisableZombieEggStomping")) {
            LOGGER.warn("Missing option 'DisableZombieEggStomping', adding default (false).");
            obj.addProperty("DisableZombieEggStomping", false);
            changed = true;
        }

        config = GSON.fromJson(obj, ModConfig.class);

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
