package com.pixelindiedev.lazy_ai_pixelindiedev;

import com.pixelindiedev.lazy_ai_pixelindiedev.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import static com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev.chunksToSquaredBlocks;
import static com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev.squaredBlocksToChunks;

public class Lazy_ai_pixelindiedev_client implements ClientModInitializer {
    private static int lastSimDistanceChunks = -1;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) return;

            int simDistanceChunks = client.options.getSimulationDistance().getValue();    // value is in chunks

            ModConfig config = ModConfig.load();
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
