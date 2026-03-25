package com.pixelindiedev.lazy_ai_pixelindiedev;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.config.ModConfig;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import static com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev.chunksToSquaredBlocks;
import static com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev.squaredBlocksToChunks;

public class Lazy_ai_pixelindiedev_server implements DedicatedServerModInitializer {
    private static int lastSimDistanceChunks = -1;

    @Override
    public void onInitializeServer() {
        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> {
            final int simDistanceChunks = server.getPlayerList().getSimulationDistance();    // value is in chunks

            final ModConfig config = ModConfig.load();
            lastSimDistanceChunks = squaredBlocksToChunks(config.BlockDistance_Close, config.getBlockDistance_Close_Multiplier());

            if (simDistanceChunks != lastSimDistanceChunks) {
                lastSimDistanceChunks = simDistanceChunks;

                config.BlockDistance_Close = chunksToSquaredBlocks(simDistanceChunks, config.getBlockDistance_Close_Multiplier());
                config.BlockDistance_Far = chunksToSquaredBlocks(simDistanceChunks, config.getBlockDistance_Far_Multiplier());

                config.save();
            }
        });
    }
}
