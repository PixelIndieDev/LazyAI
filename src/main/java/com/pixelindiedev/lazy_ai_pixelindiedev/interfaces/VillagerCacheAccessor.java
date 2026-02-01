package com.pixelindiedev.lazy_ai_pixelindiedev.interfaces;

import net.minecraft.util.math.BlockPos;

public interface VillagerCacheAccessor {
    void lazyai$invalidateBlockCache(BlockPos pos);
}
