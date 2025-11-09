package com.pixelindiedev.lazy_ai_pixelindiedev.config.integration;

import com.pixelindiedev.lazy_ai_pixelindiedev.config.DistanceScalingType;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.ModConfig;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.OptimalizationType;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.TemptDelayEnum;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class LazyAiConfigScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;

    protected LazyAiConfigScreen(Screen parent) {
        super(Text.literal("Lazy AI Config"));
        this.parent = parent;
        this.config = ModConfig.load();
    }

    @Override
    protected void init() {
        int y = height / 4;

        addDrawableChild(ButtonWidget.builder(Text.literal("AI Optimization Type: " + config.AIOptimizationType), (btn) ->
        {
            OptimalizationType[] values = OptimalizationType.values();
            int next = (config.AIOptimizationType.ordinal() + 1) % values.length;
            config.AIOptimizationType = values[next];
            btn.setMessage(Text.literal("AI Optimization Type: " + config.AIOptimizationType));
            config.save();
        }).dimensions(width / 2 - 100, y, 200, 20).build());

        y += 25;

        addDrawableChild(ButtonWidget.builder(Text.literal("Distance Scaling: " + config.DistanceScaling), (btn) ->
        {
            DistanceScalingType[] values = DistanceScalingType.values();
            int next = (config.DistanceScaling.ordinal() + 1) % values.length;
            config.DistanceScaling = values[next];
            btn.setMessage(Text.literal("Distance Scaling: " + config.DistanceScaling));
            config.save();
        }).dimensions(width / 2 - 100, y, 200, 20).build());

        y += 25;

        addDrawableChild(ButtonWidget.builder(Text.literal("Mob Tempting Delay: " + config.TemptDelay), (btn) ->
        {
            TemptDelayEnum[] values = TemptDelayEnum.values();
            int next = (config.TemptDelay.ordinal() + 1) % values.length;
            config.TemptDelay = values[next];
            btn.setMessage(Text.literal("Mob Tempting Delay: " + config.TemptDelay));
            config.save();
        }).dimensions(width / 2 - 100, y, 200, 20).build());

        y += 25;

        addDrawableChild(ButtonWidget.builder(Text.literal("Disable Zombie Egg Stomping: " + config.DisableZombieEggStomping), (btn) ->
        {
            config.DisableZombieEggStomping = !config.DisableZombieEggStomping;
            btn.setMessage(Text.literal("Disable Zombie Egg Stomping: " + config.DisableZombieEggStomping));
            config.save();
        }).dimensions(width / 2 - 100, y, 200, 20).build());

        y += 25;

        addDrawableChild(ButtonWidget.builder(Text.literal("Never Slow Down Distant Mobs: " + config.NeverSlowdownDistantMobs), (btn) ->
        {
            config.NeverSlowdownDistantMobs = !config.NeverSlowdownDistantMobs;
            btn.setMessage(Text.literal("Never Slow Down Distant Mobs: " + config.NeverSlowdownDistantMobs));
            config.save();
        }).dimensions(width / 2 - 100, y, 200, 20).build());

        y += 30;

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), (btn) -> MinecraftClient.getInstance().setScreen(parent)).dimensions(width / 2 - 100, y, 200, 20).build());
    }

    @Override
    public void close() {
        config.save();
        assert client != null;
        client.setScreen(parent);
    }
}
