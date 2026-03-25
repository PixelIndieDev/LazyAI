package com.pixelindiedev.lazy_ai_pixelindiedev.config.integration;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.config.DistanceScalingType;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.ModConfig;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.OptimalizationType;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.TemptDelayEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class LazyAiConfigScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;

    protected LazyAiConfigScreen(Screen parent) {
        super(Component.literal("Lazy AI Config"));
        this.parent = parent;
        this.config = ModConfig.load();
    }

    @Override
    protected void init() {
        int y = height / 4;

        addRenderableWidget(Button.builder(Component.literal("AI Optimization Type: " + config.AIOptimizationType), (btn) ->
        {
            OptimalizationType[] values = OptimalizationType.values();
            int next = (config.AIOptimizationType.ordinal() + 1) % values.length;
            config.AIOptimizationType = values[next];
            btn.setMessage(Component.literal("AI Optimization Type: " + config.AIOptimizationType));
            config.save();
        }).bounds(width / 2 - 100, y, 200, 20).build());

        y += 25;

        addRenderableWidget(Button.builder(Component.literal("Distance Scaling: " + config.DistanceScaling), (btn) ->
        {
            DistanceScalingType[] values = DistanceScalingType.values();
            int next = (config.DistanceScaling.ordinal() + 1) % values.length;
            config.DistanceScaling = values[next];
            btn.setMessage(Component.literal("Distance Scaling: " + config.DistanceScaling));
            config.save();
        }).bounds(width / 2 - 100, y, 200, 20).build());

        y += 25;

        addRenderableWidget(Button.builder(Component.literal("Mob Tempting Delay: " + config.TemptDelay), (btn) ->
        {
            TemptDelayEnum[] values = TemptDelayEnum.values();
            int next = (config.TemptDelay.ordinal() + 1) % values.length;
            config.TemptDelay = values[next];
            btn.setMessage(Component.literal("Mob Tempting Delay: " + config.TemptDelay));
            config.save();
        }).bounds(width / 2 - 100, y, 200, 20).build());

        y += 25;

        addRenderableWidget(Button.builder(Component.literal("Disable Zombie Egg Stomping: " + config.DisableZombieEggStomping), (btn) ->
        {
            config.DisableZombieEggStomping = !config.DisableZombieEggStomping;
            btn.setMessage(Component.literal("Disable Zombie Egg Stomping: " + config.DisableZombieEggStomping));
            config.save();
        }).bounds(width / 2 - 100, y, 200, 20).build());

        y += 25;

        addRenderableWidget(Button.builder(Component.literal("Enable Vanilla Mob Ticking: " + config.EnableVanillaMobTicking), (btn) ->
        {
            config.EnableVanillaMobTicking = !config.EnableVanillaMobTicking;
            btn.setMessage(Component.literal("Enable Vanilla Mob Ticking: " + config.EnableVanillaMobTicking));
            config.save();
        }).bounds(width / 2 - 100, y, 200, 20).build());

        y += 30;

        addRenderableWidget(Button.builder(Component.literal("Done"), (btn) -> Minecraft.getInstance().setScreen(parent)).bounds(width / 2 - 100, y, 200, 20).build());
    }

    @Override
    public void onClose() {
        config.save();
        assert minecraft != null;
        minecraft.setScreen(parent);
    }
}
