package com.pixelindiedev.lazy_ai_pixelindiedev.config;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static com.pixelindiedev.lazy_ai_pixelindiedev.config.LoggerHolder.MODLOGGER;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "lazy-ai.json";
    public static final File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), FILE_NAME);

    public DistanceScalingType DistanceScaling = ModConfigDefaults.Defaults_DistanceScaling;
    private final String _comment_DistanceScaling = "This setting controls what % range of your simulation distance is considered close and far range";
    public OptimalizationType AIOptimizationType = ModConfigDefaults.Defaults_AIOptimizationType;
    private final String _comment_AIOptimizationType = "This settings controls how aggressive the optimizations should be";
    public TemptDelayEnum TemptDelay = ModConfigDefaults.Defaults_TemptDelay;
    private final String _comment_TemptDelay = "This setting controls how much delay animals have to being tempted by an item";
    public boolean DisableZombieEggStomping = ModConfigDefaults.Defaults_DisableZombieEggStomping;
    private final String _comment_DisableZombieEggStomping = "This setting controls the prevention of zombies wanting to destroy turtle eggs";
    public boolean EnableVanillaMobTicking = ModConfigDefaults.Defaults_EnableVanillaMobTicking;
    private final String _comment_EnableVanillaMobTicking = "This setting controls if distant mobs should tick the same as in a unmodded (vanilla) game. Enabling this reduces the mod's TPS-boosting effect on your game, but can fix mob ticking issues.";

    public transient long lastModified = 0L;

    // LazyAI
    // Copyright (c) 2025 PixelIndieDev
    //
    // Licensed under the GNU GENERAL PUBLIC LICENSE
    // See the LICENSE file in the project root for full license information.

    public static ModConfig load() {
        ModConfig config = new ModConfig();
        JsonObject obj = new JsonObject();
        boolean changed = false;

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                JsonElement element = JsonParser.parseReader(reader);
                if (element.isJsonObject()) obj = element.getAsJsonObject();
            } catch (IOException e) {
                MODLOGGER.error("Failed to read config, restoring defaults.", e);
                config = new ModConfig();
            }
        } else {
            MODLOGGER.warn("Config file not found, creating a new one.");
            config = new ModConfig();
            changed = true;
        }

        // Check for missing options
        if (!obj.has("DistanceScaling")) {
            var value = ModConfigDefaults.Defaults_DistanceScaling.name();
            MODLOGGER.warn("Missing option 'DistanceScaling', adding default (" + value + ").");
            obj.addProperty("DistanceScaling", value);
            obj.addProperty("_comment_DistanceScaling", config._comment_DistanceScaling);
            changed = true;
        }
        if (!obj.has("AIOptimizationType")) {
            var value = ModConfigDefaults.Defaults_AIOptimizationType.name();
            MODLOGGER.warn("Missing option 'AIOptimizationType', adding default (" + value + ").");
            obj.addProperty("AIOptimizationType", value);
            obj.addProperty("_comment_AIOptimizationType", config._comment_AIOptimizationType);
            changed = true;
        }
        if (!obj.has("TemptDelay")) {
            var value = ModConfigDefaults.Defaults_TemptDelay.name();
            MODLOGGER.warn("Missing option 'TemptDelay', adding default (" + value + ").");
            obj.addProperty("TemptDelay", value);
            obj.addProperty("_comment_TemptDelay", config._comment_TemptDelay);
            changed = true;
        }
        if (!obj.has("DisableZombieEggStomping")) {
            var value = ModConfigDefaults.Defaults_DisableZombieEggStomping;
            MODLOGGER.warn("Missing option 'DisableZombieEggStomping', adding default (" + value + ").");
            obj.addProperty("DisableZombieEggStomping", value);
            obj.addProperty("_comment_DisableZombieEggStomping", config._comment_DisableZombieEggStomping);
            changed = true;
        }
        if (!obj.has("EnableVanillaMobTicking")) {
            var value = ModConfigDefaults.Defaults_EnableVanillaMobTicking;
            MODLOGGER.warn("Missing option 'EnableVanillaMobTicking', adding default (" + value + ").");
            obj.addProperty("EnableVanillaMobTicking", value);
            obj.addProperty("_comment_EnableVanillaMobTicking", config._comment_EnableVanillaMobTicking);
            changed = true;
        }

        config = GSON.fromJson(obj, ModConfig.class);

        //Null check
        if (config.DistanceScaling == null) {
            var value = ModConfigDefaults.Defaults_DistanceScaling;
            MODLOGGER.warn("Invalid DistanceScaling value in config, using default (" + value + ").");
            config.DistanceScaling = value;
            changed = true;
        }
        if (config.AIOptimizationType == null) {
            var value = ModConfigDefaults.Defaults_AIOptimizationType;
            MODLOGGER.warn("Invalid AIOptimizationType value in config, using default (" + value + ").");
            config.AIOptimizationType = value;
            changed = true;
        }
        if (config.TemptDelay == null) {
            var value = ModConfigDefaults.Defaults_TemptDelay;
            MODLOGGER.warn("Invalid TemptDelay value, using default (" + value + ").");
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
            MODLOGGER.error("Failed to save config:", e);
        }
    }

    public boolean hasExternalChange() {
        return configFile.exists() && configFile.lastModified() != lastModified;
    }

    private int getMultiplierUsingDistanceScaling(int MediumDistanceValue) {
        return switch (DistanceScaling) {
            case Close -> (MediumDistanceValue * 2);
            case Far -> (MediumDistanceValue / 2);
            case null, default -> MediumDistanceValue;
        };
    }

    public int getBlockDistance_Close_Multiplier() {
        return getMultiplierUsingDistanceScaling(10);
    }

    public int getBlockDistance_Far_Multiplier() {
        return getMultiplierUsingDistanceScaling(5);
    }
}
