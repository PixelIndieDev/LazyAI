package com.pixelindiedev.lazy_ai_pixelindiedev;

import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class LazyAI$BlockChecker {
    //this checks for collision on blocks more efficiently than the isSolidBlock() as this checks less accurate, although I don't need the accuracy for what I use it for
    private static final Reference2BooleanMap<Block> blockSolidCollisionCheckCache = new Reference2BooleanOpenHashMap<>(); //Stays in memory, does not have a big size, so it's fine
    private static volatile boolean initialized = false;

    static {
        blockSolidCollisionCheckCache.defaultReturnValue(false);
    }

    public static void initializeCacheAsync() {
        new Thread(() -> {
            initializeCache();
            System.out.println("LazyAI$BlockCheckerCacheInit started initializing asynchronously");
        }, "LazyAI$BlockCheckerCacheInit").start();
    }

    private static void initializeCache() {
        if (initialized) return;

        synchronized (blockSolidCollisionCheckCache) {
            if (initialized) return;
            //default list
            //to reduce checks during runtime

            //liquids
            blockSolidCollisionCheckCache.put(Blocks.AIR, false);
            blockSolidCollisionCheckCache.put(Blocks.CAVE_AIR, false);
            blockSolidCollisionCheckCache.put(Blocks.VOID_AIR, false);
            blockSolidCollisionCheckCache.put(Blocks.WATER, false);
            blockSolidCollisionCheckCache.put(Blocks.LAVA, false);

            //common terrain
            blockSolidCollisionCheckCache.put(Blocks.STONE, true);
            blockSolidCollisionCheckCache.put(Blocks.DEEPSLATE, true);
            blockSolidCollisionCheckCache.put(Blocks.GRANITE, true);
            blockSolidCollisionCheckCache.put(Blocks.DIORITE, true);
            blockSolidCollisionCheckCache.put(Blocks.ANDESITE, true);
            blockSolidCollisionCheckCache.put(Blocks.DIRT, true);
            blockSolidCollisionCheckCache.put(Blocks.GRASS_BLOCK, true);
            blockSolidCollisionCheckCache.put(Blocks.COARSE_DIRT, true);
            blockSolidCollisionCheckCache.put(Blocks.PODZOL, true);
            blockSolidCollisionCheckCache.put(Blocks.MYCELIUM, true);
            blockSolidCollisionCheckCache.put(Blocks.SAND, true);
            blockSolidCollisionCheckCache.put(Blocks.RED_SAND, true);
            blockSolidCollisionCheckCache.put(Blocks.GRAVEL, true);
            blockSolidCollisionCheckCache.put(Blocks.CLAY, true);
            blockSolidCollisionCheckCache.put(Blocks.COBBLESTONE, true);

            //wood
            blockSolidCollisionCheckCache.put(Blocks.OAK_PLANKS, true);
            blockSolidCollisionCheckCache.put(Blocks.SPRUCE_PLANKS, true);
            blockSolidCollisionCheckCache.put(Blocks.BIRCH_PLANKS, true);
            blockSolidCollisionCheckCache.put(Blocks.JUNGLE_PLANKS, true);
            blockSolidCollisionCheckCache.put(Blocks.ACACIA_PLANKS, true);
            blockSolidCollisionCheckCache.put(Blocks.DARK_OAK_PLANKS, true);

            //common non-solid
            blockSolidCollisionCheckCache.put(Blocks.SHORT_GRASS, false);
            blockSolidCollisionCheckCache.put(Blocks.TALL_GRASS, false);
            blockSolidCollisionCheckCache.put(Blocks.FERN, false);
            blockSolidCollisionCheckCache.put(Blocks.LARGE_FERN, false);
            blockSolidCollisionCheckCache.put(Blocks.DEAD_BUSH, false);
            blockSolidCollisionCheckCache.put(Blocks.SEAGRASS, false);
            blockSolidCollisionCheckCache.put(Blocks.TALL_SEAGRASS, false);
            blockSolidCollisionCheckCache.put(Blocks.KELP, false);
            blockSolidCollisionCheckCache.put(Blocks.KELP_PLANT, false);
            blockSolidCollisionCheckCache.put(Blocks.DANDELION, false);
            blockSolidCollisionCheckCache.put(Blocks.POPPY, false);
            blockSolidCollisionCheckCache.put(Blocks.TORCH, false);
            blockSolidCollisionCheckCache.put(Blocks.WALL_TORCH, false);
            blockSolidCollisionCheckCache.put(Blocks.REDSTONE_TORCH, false);
            blockSolidCollisionCheckCache.put(Blocks.REDSTONE_WIRE, false);
            blockSolidCollisionCheckCache.put(Blocks.FIRE, false);
            blockSolidCollisionCheckCache.put(Blocks.SOUL_FIRE, false);
            blockSolidCollisionCheckCache.put(Blocks.SNOW, false);

            initialized = true;
            System.out.println("LazyAI$BlockCheckerCacheInit finished initializing asynchronously");
        }
    }

    public static boolean hasSolidCollision(Block block) {
        if (blockSolidCollisionCheckCache.containsKey(block)) return blockSolidCollisionCheckCache.getBoolean(block);
        boolean isSolid = hasCollisionFast(block);
        blockSolidCollisionCheckCache.put(block, isSolid);
        return isSolid;
    }

    public static boolean hasSolidCollision(BlockState state) {
        return hasSolidCollision(state.getBlock());
    }

    private static boolean hasCollisionFast(Block block) {
        if (blockSolidCollisionCheckCache.containsKey(block)) return blockSolidCollisionCheckCache.getBoolean(block);

        //these should have a different boolean value
        //is it a door or trapdoor
        if (block instanceof DoorBlock || block instanceof TrapdoorBlock) {
            blockSolidCollisionCheckCache.put(block, false);
            return false;
        }

        BlockState defaultState = block.getDefaultState();
        VoxelShape collisionShape = defaultState.getCollisionShape(null, null);
        boolean isSolid = !collisionShape.isEmpty() && collisionShape != VoxelShapes.empty();

        blockSolidCollisionCheckCache.put(block, isSolid);
        return isSolid;
    }
}
