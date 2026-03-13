package com.pixelindiedev.lazy_ai_pixelindiedev;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LazyAi$ChestCacheManager {
    private static final Reference2ObjectOpenHashMap<Object, Long2ObjectOpenHashMap<List<BlockPos>>> worldCaches = new Reference2ObjectOpenHashMap<>();

    public static List<BlockPos> getCachedStorage(ServerWorld world, BlockPos center, int horizontalRange, int verticalRange) {
        final Object worldKey = world.getRegistryKey();
        Long2ObjectOpenHashMap<List<BlockPos>> worldCache;

        //get worldcache
        synchronized (worldCaches) {
            worldCache = worldCaches.get(worldKey);
            if (worldCache == null) {
                worldCache = new Long2ObjectOpenHashMap<>(256);
                worldCaches.put(worldKey, worldCache);
            }
        }

        final List<BlockPos> result = new ArrayList<>();

        final int minX = center.getX() - horizontalRange;
        final int maxX = center.getX() + horizontalRange;
        final int minY = center.getY() - verticalRange;
        final int maxY = center.getY() + verticalRange;
        final int minZ = center.getZ() - horizontalRange;
        final int maxZ = center.getZ() + horizontalRange;

        final int minChunkX = minX >> 4;
        final int maxChunkX = maxX >> 4;
        final int minChunkZ = minZ >> 4;
        final int maxChunkZ = maxZ >> 4;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                final long chunkKey = packChunkPos(cx, cz);

                //get chests in chunks
                List<BlockPos> chunkChests;
                synchronized (worldCache) {
                    chunkChests = worldCache.get(chunkKey);
                    if (chunkChests == null) {
                        chunkChests = scanChunkForChests(world, cx, cz);
                        addChest(worldCache, chunkKey, chunkChests);
                    }
                }

                //get chests in range of entity
                for (BlockPos pos : chunkChests) {
                    final int px = pos.getX();
                    if (px < minX || px > maxX) continue;
                    final int py = pos.getY();
                    if (py < minY || py > maxY) continue;
                    final int pz = pos.getZ();
                    if (pz < minZ || pz > maxZ) continue;
                    result.add(pos);
                }
            }
        }
        return result;
    }

    private static void addChest(Long2ObjectOpenHashMap<List<BlockPos>> worldCache, long chunk, List<BlockPos> chests) {
        if (worldCache.containsKey(chunk)) worldCache.replace(chunk, chests);
        else worldCache.put(chunk, chests);
    }

    private static void addChest(Long2ObjectOpenHashMap<List<BlockPos>> worldCache, long chunk, List<BlockPos> chests, BlockPos chest) {
        for (BlockPos blockPos : chests) if (blockPos.equals(chest)) return; //chest already exist in list
        chests.add(chest);
        addChest(worldCache, chunk, chests);
    }

    public static void addChestWorld(ServerWorld world, BlockPos pos) {
        final Object worldKey = world.getRegistryKey();
        final Long2ObjectOpenHashMap<List<BlockPos>> worldCache;

        synchronized (worldCaches) {
            worldCache = worldCaches.get(worldKey);
        }

        if (worldCache != null) {
            final int chunkX = pos.getX() >> 4;
            final int chunkZ = pos.getZ() >> 4;
            final long chunkKey = packChunkPos(chunkX, chunkZ);

            synchronized (worldCache) {
                final List<BlockPos> chunkChests = worldCache.get(chunkKey);
                if (chunkChests != null && !chunkChests.isEmpty()) addChest(worldCache, chunkKey, chunkChests, pos);
            }
        }
    }

    public static void removeChest(ServerWorld world, BlockPos pos) {
        final Object worldKey = world.getRegistryKey();
        final Long2ObjectOpenHashMap<List<BlockPos>> worldCache;

        synchronized (worldCaches) {
            worldCache = worldCaches.get(worldKey);
        }

        if (worldCache != null) {
            final int chunkX = pos.getX() >> 4;
            final int chunkZ = pos.getZ() >> 4;
            final long chunkKey = packChunkPos(chunkX, chunkZ);

            synchronized (worldCache) {
                final List<BlockPos> chunkChests = worldCache.get(chunkKey);
                if (chunkChests != null) chunkChests.remove(pos);
            }
        }
    }

    public static void invalidateChunk(ServerWorld world, int chunkX, int chunkZ) {
        final Object worldKey = world.getRegistryKey();
        final Long2ObjectOpenHashMap<List<BlockPos>> worldCache;

        synchronized (worldCaches) {
            worldCache = worldCaches.get(worldKey);
        }

        if (worldCache != null) {
            final long chunkKey = packChunkPos(chunkX, chunkZ);
            synchronized (worldCache) {
                worldCache.remove(chunkKey);
            }
        }
    }

    public static void clearWorld(ServerWorld world) {
        final Object worldKey = world.getRegistryKey();
        synchronized (worldCaches) {
            worldCaches.remove(worldKey);
        }
    }

    private static List<BlockPos> scanChunkForChests(ServerWorld world, int chunkX, int chunkZ) {
        final List<BlockPos> chests = new ArrayList<>(8);

        if (!world.isChunkLoaded(chunkX, chunkZ)) return chests;

        final WorldChunk chunk = world.getChunkManager().getWorldChunk(chunkX, chunkZ);
        if (chunk != null) {
            final Map<BlockPos, BlockEntity> blockEntities = chunk.getBlockEntities();
            for (Map.Entry<BlockPos, BlockEntity> entry : blockEntities.entrySet()) {
                if (entry.getValue() instanceof ChestBlockEntity) chests.add(entry.getKey().toImmutable());
            }
        }
        return chests;
    }

    private static long packChunkPos(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
}
