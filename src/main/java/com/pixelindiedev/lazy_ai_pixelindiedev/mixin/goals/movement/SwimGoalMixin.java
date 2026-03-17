package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.goals.movement;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.FluidTags;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SwimGoal.class, priority = 1003)
public abstract class SwimGoalMixin {
    @Shadow
    @Final
    private MobEntity mob;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onlyTickWhenUnderwater(CallbackInfo ci) {
        if (!mob.isTouchingWater() && !mob.isInLava()) {
            ci.cancel();
            return;
        }
        final double fluidLevel = mob.getFluidHeight(FluidTags.WATER);
        if (fluidLevel < 0.3) ci.cancel();
    }
}
