package com.pixelindiedev.lazy_ai_pixelindiedev.config.integration;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import com.pixelindiedev.lazy_ai_pixelindiedev.enums.DistanceScalingType;
import com.pixelindiedev.lazy_ai_pixelindiedev.enums.OptimalizationType;
import com.pixelindiedev.lazy_ai_pixelindiedev.enums.TemptDelayEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class LazyAiConfigScreen extends Screen {
    private final Screen parent;

    protected LazyAiConfigScreen(Screen parent) {
        super(Component.literal("Lazy AI Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int y = height / 4;

        addRenderableWidget(Button.builder(Component.literal("AI Optimization Type: " + Lazy_ai_pixelindiedev.CONFIG.AIOptimizationType), (btn) ->
        {
            OptimalizationType[] values = OptimalizationType.values();
            int next = (Lazy_ai_pixelindiedev.CONFIG.AIOptimizationType.ordinal() + 1) % values.length;
            Lazy_ai_pixelindiedev.CONFIG.AIOptimizationType = values[next];
            btn.setMessage(Component.literal("AI Optimization Type: " + Lazy_ai_pixelindiedev.CONFIG.AIOptimizationType));
            Lazy_ai_pixelindiedev.CONFIG.save();
        }).bounds(width / 2 - 100, y, 200, 20).build());

        y += 25;

        addRenderableWidget(Button.builder(Component.literal("Distance Scaling: " + Lazy_ai_pixelindiedev.CONFIG.DistanceScaling), (btn) ->
        {
            DistanceScalingType[] values = DistanceScalingType.values();
            int next = (Lazy_ai_pixelindiedev.CONFIG.DistanceScaling.ordinal() + 1) % values.length;
            Lazy_ai_pixelindiedev.CONFIG.DistanceScaling = values[next];

            btn.setMessage(Component.literal("Distance Scaling: " + Lazy_ai_pixelindiedev.CONFIG.DistanceScaling));
            Lazy_ai_pixelindiedev.CONFIG.save();
        }).bounds(width / 2 - 100, y, 200, 20).build());

        y += 25;

        addRenderableWidget(Button.builder(Component.literal("Mob Tempting Delay: " + Lazy_ai_pixelindiedev.CONFIG.TemptDelay), (btn) ->
        {
            TemptDelayEnum[] values = TemptDelayEnum.values();
            int next = (Lazy_ai_pixelindiedev.CONFIG.TemptDelay.ordinal() + 1) % values.length;
            Lazy_ai_pixelindiedev.CONFIG.TemptDelay = values[next];
            btn.setMessage(Component.literal("Mob Tempting Delay: " + Lazy_ai_pixelindiedev.CONFIG.TemptDelay));
            Lazy_ai_pixelindiedev.CONFIG.save();
        }).bounds(width / 2 - 100, y, 200, 20).build());

        y += 25;

        addRenderableWidget(Button.builder(Component.literal("Disable Zombie Egg Stomping: " + Lazy_ai_pixelindiedev.CONFIG.DisableZombieEggStomping), (btn) ->
        {
            Lazy_ai_pixelindiedev.CONFIG.DisableZombieEggStomping = !Lazy_ai_pixelindiedev.CONFIG.DisableZombieEggStomping;
            btn.setMessage(Component.literal("Disable Zombie Egg Stomping: " + Lazy_ai_pixelindiedev.CONFIG.DisableZombieEggStomping));
            Lazy_ai_pixelindiedev.CONFIG.save();
        }).bounds(width / 2 - 100, y, 200, 20).build());

        y += 25;

        addRenderableWidget(Button.builder(Component.literal("Enable Vanilla Mob Ticking: " + Lazy_ai_pixelindiedev.CONFIG.EnableVanillaMobTicking), (btn) ->
        {
            Lazy_ai_pixelindiedev.CONFIG.EnableVanillaMobTicking = !Lazy_ai_pixelindiedev.CONFIG.EnableVanillaMobTicking;
            btn.setMessage(Component.literal("Enable Vanilla Mob Ticking: " + Lazy_ai_pixelindiedev.CONFIG.EnableVanillaMobTicking));
            Lazy_ai_pixelindiedev.CONFIG.save();
        }).bounds(width / 2 - 100, y, 200, 20).build());

        y += 30;

        addRenderableWidget(Button.builder(Component.literal("Done"), (btn) -> Minecraft.getInstance().setScreenAndShow(parent)).bounds(width / 2 - 100, y, 200, 20).build());
    }

    @Override
    public void onClose() {
        Lazy_ai_pixelindiedev.CONFIG.save();
        assert minecraft != null;
        minecraft.setScreenAndShow(parent);
    }
}
