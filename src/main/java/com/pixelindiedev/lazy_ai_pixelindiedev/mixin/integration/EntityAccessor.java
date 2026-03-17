package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.integration;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Invoker("updateSubmergedInWaterState")
    void invokeUpdateSubmergedInWaterState();

    @Invoker("updateWaterState")
    boolean invokeUpdateWaterState();
}
