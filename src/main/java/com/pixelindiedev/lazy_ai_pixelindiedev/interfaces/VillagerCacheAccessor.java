package com.pixelindiedev.lazy_ai_pixelindiedev.interfaces;

import net.minecraft.core.BlockPos;

public interface VillagerCacheAccessor {
    void lazyai$invalidateBlockCache(BlockPos pos);
}
