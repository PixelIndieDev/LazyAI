package com.pixelindiedev.lazy_ai_pixelindiedev;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.EmptyBlockView;

import java.util.concurrent.ConcurrentHashMap;

public class LazyAI$BlockChecker {
    //this checks for collision on blocks more efficiently than the isSolidBlock() as this checks less accurate, although I don't need the accuracy for what I use it for
    private static final ConcurrentHashMap<Block, Boolean> blockSolidCollisionCheckCache = new ConcurrentHashMap<>(1024); //Stays in memory, does not have a big size, so it's fine

    public static void initializeCacheAsync() {
        new Thread(() -> {
            initializeCache();
            System.out.println("LazyAI$BlockCheckerCacheInit started initializing asynchronously");
        }, "LazyAI$BlockCheckerCacheInit").start();
    }

    private static void initializeCache() {

        synchronized (blockSolidCollisionCheckCache) {
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

            System.out.println("LazyAI$BlockCheckerCacheInit finished initializing asynchronously");
        }
    }

    public static boolean hasSolidCollision(Block block) {
        if (block == null) return false;
        Boolean cached = blockSolidCollisionCheckCache.get(block);
        if (cached != null) return cached;

        final boolean isSolid = hasCollisionFast(block);
        Boolean previous = blockSolidCollisionCheckCache.putIfAbsent(block, isSolid);
        return previous != null ? previous : isSolid;
    }

    public static boolean hasSolidCollision(BlockState state) {
        if (state == null) return false;
        return hasSolidCollision(state.getBlock());
    }

    private static boolean hasCollisionFast(Block block) {
        //these should have a different boolean value
        //is it a door or trapdoor
        if (block instanceof DoorBlock || block instanceof TrapdoorBlock) return false;

        try {
            BlockState defaultState = block.getDefaultState();
            VoxelShape collisionShape = defaultState.getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
            return !collisionShape.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}