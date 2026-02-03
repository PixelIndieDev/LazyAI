package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

import com.pixelindiedev.lazy_ai_pixelindiedev.LazyAi$ChestCacheManager;
import com.pixelindiedev.lazy_ai_pixelindiedev.interfaces.VillagerCacheAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.pixelindiedev.lazy_ai_pixelindiedev.LazyAI$BlockChecker.hasSolidCollision;

@Mixin(ServerWorld.class)
public class WorldMixin {
    //updates the villager around bloken blocks
    @Inject(method = "onBlockStateChanged", at = @At("HEAD"))
    private void invalidateBlockCacheNearbyVillagers(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        if (pos == null || oldState == null | newState == null) return;

        Block blockOld = oldState.getBlock();
        Block blockNew = newState.getBlock();
        if (blockOld == blockNew) return;

        ServerWorld world = (ServerWorld) (Object) this;
        if (world == null) return;

        //chest updates
        boolean wasChest = blockOld instanceof ChestBlock;
        boolean isChest = blockNew instanceof ChestBlock;

        if (wasChest && !isChest) { //chest removed
            LazyAi$ChestCacheManager.removeChest(world, pos);
        } else if (!wasChest && isChest) { //chest placed
            LazyAi$ChestCacheManager.addChestWorld(world, pos);
        }

        boolean oldCollision = hasSolidCollision(blockOld);
        boolean newCollision = hasSolidCollision(blockNew);
        if (oldCollision == newCollision) return;

        Box searchBox = Box.of(Vec3d.ofCenter(pos), 2.0, 2.0, 2.0);
        List<VillagerEntity> villagers = world.getEntitiesByClass(VillagerEntity.class, searchBox, (v) -> true);

        for (VillagerEntity villager : villagers)
            ((VillagerCacheAccessor) villager).lazyai$invalidateBlockCache(pos);
    }

    @Inject(method = "unloadEntities", at = @At("HEAD"))
    private void onChunkUnload(WorldChunk chunk, CallbackInfo ci) {
        ServerWorld world = (ServerWorld) (Object) this;
        if (world == null) return;
        LazyAi$ChestCacheManager.invalidateChunk(world, chunk.getPos().x, chunk.getPos().z);
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void onWorldClose(CallbackInfo ci) {
        ServerWorld world = (ServerWorld) (Object) this;
        if (world == null) return;
        LazyAi$ChestCacheManager.clearWorld(world);
    }
}