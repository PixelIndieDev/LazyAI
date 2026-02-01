package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

import com.pixelindiedev.lazy_ai_pixelindiedev.interfaces.VillagerCacheAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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
        Block blockOld = oldState.getBlock();
        Block blockNew = newState.getBlock();
        if (blockOld != blockNew) {
            if (hasSolidCollision(blockOld) != hasSolidCollision(blockNew)) {
                double radius = 1;
                Box searchBox = new Box(pos).expand(radius);
                ServerWorld world = (ServerWorld) (Object) this;
                List<VillagerEntity> villagers = world.getEntitiesByClass(VillagerEntity.class, searchBox, (v) -> true);

                for (VillagerEntity villager : villagers)
                    if (villager instanceof VillagerCacheAccessor accessor) accessor.lazyai$invalidateBlockCache(pos);
            }
        }
    }
}