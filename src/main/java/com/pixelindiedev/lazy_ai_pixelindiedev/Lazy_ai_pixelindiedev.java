package com.pixelindiedev.lazy_ai_pixelindiedev;

import com.pixelindiedev.lazy_ai_pixelindiedev.config.DistanceType;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.ModConfig;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.OptimalizationType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.pixelindiedev.lazy_ai_pixelindiedev.LazyAI$BlockChecker.initializeCacheAsync;

public class Lazy_ai_pixelindiedev implements ModInitializer {
    private static final Map<UUID, DistanceType> cache = new ConcurrentHashMap<>();
    public static ModConfig CONFIG;
    public static boolean EnableCriticalTPSMode = false;
    private static float Server_TPS_MS = 50.0f; //in ms
    private static int lastTick = -1;

    public static void onServerTick(MinecraftServer server) {
        if (CONFIG.lastModified == 0L) CONFIG.lastModified = ModConfig.configFile.lastModified();

        int currentTick = server.getTicks();

        // Calculate TPS
        if (CONFIG.AIOptimizationType == OptimalizationType.Dynamic) {
            if ((currentTick & 8) == 0) {
                long[] tickTimes = server.lastTickLengths; //Always returns 100 values, so no valid check is needed
                long sum = 0;
                float tickTimesLength = 0.0f;
                for (long time : tickTimes) {
                    if (time > 0.0) {
                        sum += time;
                        tickTimesLength++;
                    }
                }

                float MSPerTick;
                if (sum <= 0.0) MSPerTick = 58.8f; //Make it use the default setting temporarily before it has the valid tick times
                else MSPerTick = (sum / tickTimesLength) * 1.0e-6f;

                Server_TPS_MS = MSPerTick;
            }

            EnableCriticalTPSMode = Server_TPS_MS > 76.92f;
        }

        if (currentTick != lastTick) {
            cache.clear();
            lastTick = currentTick;
        }

        if (CONFIG.hasExternalChange()) {
            CONFIG = ModConfig.load();
        }
    }

    public static DistanceType GetClosestPlayerDistance(MobEntity mob) {
        if (mob == null) return DistanceType.FarRange;

        PlayerEntity closestPlayer = mob.getEntityWorld().getClosestPlayer(mob, CONFIG.BlockDistance_Far);
        if (closestPlayer == null) return DistanceType.FarRange;

        double distancebetween = mob.squaredDistanceTo(closestPlayer);

        if (distancebetween >= CONFIG.BlockDistance_Far) {
            return DistanceType.FarRange;
        } else if (distancebetween >= CONFIG.BlockDistance_Close) {
            return DistanceType.MediumRange;
        } else {
            return DistanceType.CloseRange;
        }
    }

    public static DistanceType getDistance(MobEntity mob) {
        if (mob == null || mob.getEntityWorld() == null) return DistanceType.FarRange;

        return cache.computeIfAbsent(mob.getUuid(), id -> GetClosestPlayerDistance(mob));
    }

    public static int squaredBlocksToChunks(int squaredBlockDistance, int multiplier) {
        return (int) (Math.round((Math.sqrt(squaredBlockDistance) * multiplier) / 16.0));
    }

    public static int chunksToSquaredBlocks(int chunkRadius, int multiplier) {
        int blocks = (chunkRadius * 16) / multiplier;
        return blocks * blocks;
    }

    public static int getTemptGoal() {
        return CONFIG.TemptDelay.ordinal();
    }

    public static OptimalizationType getOptimalizationType() {
        if (CONFIG.AIOptimizationType == OptimalizationType.Dynamic) {
            if (Server_TPS_MS <= 50.51f) return OptimalizationType.Minimal;
            else if (Server_TPS_MS <= 62.5f) return OptimalizationType.Moderate;
            else return OptimalizationType.Agressive;
        } else return CONFIG.AIOptimizationType;
    }

    public static boolean getDisableZombieEggStomping() {
        return CONFIG.DisableZombieEggStomping;
    }

    public static boolean getNeverSlowdownDistantMobs() {
        return CONFIG.NeverSlowdownDistantMobs;
    }

    public static int getServerTick() {
        return lastTick;
    }

    public static MobEntity GetMobEntity(LivingEntity entity) {
        if (entity != null) {
            if (entity instanceof MobEntity mob) return (MobEntity) mob;
            else return null;
        } else return null;
    }

    @Override
    public void onInitialize() {
        ServerTickEvents.START_SERVER_TICK.register(Lazy_ai_pixelindiedev::onServerTick);
        CONFIG = ModConfig.load();

        initializeCacheAsync();
    }
}
