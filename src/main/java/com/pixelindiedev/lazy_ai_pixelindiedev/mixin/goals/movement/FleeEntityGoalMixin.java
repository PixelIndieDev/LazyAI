package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.goals.movement;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.helpers.CooldownHelper;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AvoidEntityGoal.class, priority = 1003)
public class FleeEntityGoalMixin {
    @Final
    @Unique
    private final CooldownHelper localCooldownHelper = new CooldownHelper(
            new int[]{10, 20, 40}, // Cooldowns from close to far, in ticks
            new int[]{15, 30, 80},
            new int[]{5, 10, 30}
    );
    @Final
    @Shadow
    protected PathfinderMob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void throttleDetection(CallbackInfoReturnable<Boolean> cir) {
        if (localCooldownHelper.shouldThrottle(mob)) cir.setReturnValue(false);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void reduceTickFrequency(CallbackInfo ci) {
        int[] temparray = localCooldownHelper.getCurrentCooldownList();
        if (temparray == null || temparray[2] == 0) return;
        if ((mob.tickCount + mob.getId()) % temparray[2] != 0) ci.cancel();
    }
}
