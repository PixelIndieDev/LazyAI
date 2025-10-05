package com.pixelindiedev.lazy_ai_pixelindiedev;

import com.pixelindiedev.lazy_ai_pixelindiedev.config.DistanceType;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.ModConfig;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.OptimalizationType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Lazy_ai_pixelindiedev implements ModInitializer {

    private static final float MinimumDistanceMultiplier = 1.5f; //Default value of the base game

    private static final float FarDistanceMultiplier = 16.0f;
    private static final float MediumDistanceMultiplier = 6.0f;
    private static final float CloseDistanceMultiplier = 1.65f;
    private static final Map<UUID, DistanceType> cache = new ConcurrentHashMap<>();
    public static ModConfig CONFIG;
    private static int lastTick = -1;

    public static void onServerTick(MinecraftServer server) {
        int currentTick = server.getTicks();

        if (currentTick != lastTick) {
            cache.clear();
            lastTick = currentTick;
        }

        if (CONFIG.hasExternalChange()) {
            CONFIG = ModConfig.load();
        }
    }

    public static float lerp(DistanceType distance) {
        return switch (distance) {
            case FarRange -> FarDistanceMultiplier;
            case MediumRange -> MediumDistanceMultiplier;
            case null, default -> CloseDistanceMultiplier;
        };
    }

    public static DistanceType GetClosestPlayerDistance(MobEntity mob) {
        if (mob == null) return DistanceType.FarRange;

        PlayerEntity closestPlayer = mob.getWorld().getClosestPlayer(mob, CONFIG.BlockDistance_Far);
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
        if (mob == null || mob.getWorld() == null) return DistanceType.FarRange;

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
        return CONFIG.AIOptimizationType;
    }

    public static boolean getDisableZombieEggStomping() {
        return CONFIG.DisableZombieEggStomping;
    }

    @Override
    public void onInitialize() {
        ServerTickEvents.START_SERVER_TICK.register(Lazy_ai_pixelindiedev::onServerTick);
        CONFIG = ModConfig.load();
    }
}
