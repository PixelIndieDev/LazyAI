package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.goals.movement;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.helpers.CooldownHelper;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RandomStrollGoal.class, priority = 1002)
public class WanderAroundGoalMixin {
    @Final
    @Unique
    private final CooldownHelper localCooldownHelper = new CooldownHelper(
            new int[]{1, 4, 8}, // Cooldowns from close to far, in ticks
            new int[]{2, 7, 12},
            new int[]{0, 2, 5}
    );
    @Final
    @Shadow
    protected PathfinderMob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void ThrottleWandering(CallbackInfoReturnable<Boolean> cir) {
        if (localCooldownHelper.shouldThrottle(mob)) cir.setReturnValue(false);
    }
}
