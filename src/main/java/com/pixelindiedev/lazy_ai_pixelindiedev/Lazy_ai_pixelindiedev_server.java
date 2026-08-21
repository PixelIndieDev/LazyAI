package com.pixelindiedev.lazy_ai_pixelindiedev;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.config.BlockDistancesHelper;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

public class Lazy_ai_pixelindiedev_server implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> {
            BlockDistancesHelper.SetSimulationDistance(server.getPlayerList().getSimulationDistance());
        });
    }
}
