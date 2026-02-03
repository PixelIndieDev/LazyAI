package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

import com.pixelindiedev.lazy_ai_pixelindiedev.interfaces.VillagerCacheAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.pixelindiedev.lazy_ai_pixelindiedev.LazyAI$BlockChecker.hasSolidCollision;

@Mixin(ServerWorld.class)
public class WorldMixin {
    //updates the villager around bloken blocks
    @Inject(method = "onBlockChanged", at = @At("HEAD"))
    private void invalidateBlockCacheNearbyVillagers(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        if (pos == null || oldState == null | newState == null) return;

        Block blockOld = oldState.getBlock();
        Block blockNew = newState.getBlock();
        if (blockOld == blockNew) return;

        ServerWorld world = (ServerWorld) (Object) this;
        if (world == null) return;

        boolean oldCollision = hasSolidCollision(blockOld);
        boolean newCollision = hasSolidCollision(blockNew);
        if (oldCollision == newCollision) return;

        Box searchBox = Box.of(Vec3d.ofCenter(pos), 2.0, 2.0, 2.0);
        List<VillagerEntity> villagers = world.getEntitiesByClass(VillagerEntity.class, searchBox, (v) -> true);

        for (VillagerEntity villager : villagers)
            ((VillagerCacheAccessor) villager).lazyai$invalidateBlockCache(pos);
    }
}