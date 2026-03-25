package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.LazyAi$ChestCacheManager;
import com.pixelindiedev.lazy_ai_pixelindiedev.interfaces.VillagerCacheAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.pixelindiedev.lazy_ai_pixelindiedev.LazyAI$BlockChecker.hasSolidCollision;

@Mixin(ServerLevel.class)
public class WorldMixin {
    //updates the villager around bloken blocks
    @Inject(method = "updatePOIOnBlockStateChange", at = @At("HEAD"))
    private void invalidateBlockCacheNearbyVillagers(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        if (pos == null || oldState == null | newState == null) return;

        Block blockOld = oldState.getBlock();
        Block blockNew = newState.getBlock();
        if (blockOld == blockNew) return;

        ServerLevel world = (ServerLevel) (Object) this;
        if (world == null) return;

        //chest updates
        final boolean wasChest = blockOld instanceof ChestBlock;
        final boolean isChest = blockNew instanceof ChestBlock;

        if (wasChest && !isChest) { //chest removed
            LazyAi$ChestCacheManager.removeChest(world, pos);
        } else if (!wasChest && isChest) { //chest placed
            LazyAi$ChestCacheManager.addChestWorld(world, pos);
        }

        final boolean oldCollision = hasSolidCollision(blockOld);
        final boolean newCollision = hasSolidCollision(blockNew);
        if (oldCollision == newCollision) return;

        AABB searchBox = AABB.ofSize(Vec3.atCenterOf(pos), 2.0, 2.0, 2.0);
        List<Villager> villagers = world.getEntitiesOfClass(Villager.class, searchBox, (v) -> true);

        for (Villager villager : villagers)
            ((VillagerCacheAccessor) villager).lazyai$invalidateBlockCache(pos);
    }

    @Inject(method = "unload", at = @At("HEAD"))
    private void onChunkUnload(LevelChunk chunk, CallbackInfo ci) {
        ServerLevel world = (ServerLevel) (Object) this;
        if (world == null) return;
        LazyAi$ChestCacheManager.invalidateChunk(world, chunk.getPos().x(), chunk.getPos().z());
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void onWorldClose(CallbackInfo ci) {
        ServerLevel world = (ServerLevel) (Object) this;
        if (world == null) return;
        LazyAi$ChestCacheManager.clearWorld(world);
    }
}