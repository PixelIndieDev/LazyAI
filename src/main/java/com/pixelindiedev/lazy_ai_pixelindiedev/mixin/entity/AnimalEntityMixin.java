package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.entity;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.interfaces.TickCancellingAware;
import net.minecraft.entity.passive.AnimalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AnimalEntity.class, priority = 1010)
public class AnimalEntityMixin {
    @Shadow
    private int loveTicks = 0;

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void compensateLoveTicks(CallbackInfo ci) {
        final AnimalEntity self = (AnimalEntity) (Object) this;
        if (!self.isInLove()) return;

        final int skipped = ((TickCancellingAware) self).lazy_ai$getSkippedTicks();
        if (skipped <= 0) return;

        loveTicks = Math.max(0, loveTicks - skipped);
    }
}
