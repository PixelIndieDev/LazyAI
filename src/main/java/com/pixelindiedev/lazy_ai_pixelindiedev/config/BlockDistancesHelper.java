package com.pixelindiedev.lazy_ai_pixelindiedev.config;

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;

import static com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev.chunksToSquaredBlocks;

public class BlockDistancesHelper {
    //    Distance in squared blocks
    //    distance is based on simulation distance
    public static int BlockDistance_Close = ModConfigDefaults.Defaults_BlockDistance_Close;
    public static int BlockDistance_Far = ModConfigDefaults.Defaults_BlockDistance_Far;
    private static int SimulationDistance = 0; // value is in chunks

    public static void SetSimulationDistance(int newDistance) {
        if (SimulationDistance != newDistance) SimulationDistance = newDistance;
        Lazy_ai_pixelindiedev.UpdateDistanceValues();
    }

    public static void SetBlockDistances(int closeScaling, int farScaling) {
        BlockDistance_Close = chunksToSquaredBlocks(SimulationDistance, closeScaling);
        BlockDistance_Far = chunksToSquaredBlocks(SimulationDistance, farScaling);
    }
}
