package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.goals.movement;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.helpers.CooldownHelper;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MoveThroughVillageGoal.class, priority = 1001)
public class MoveThroughVillageGoalMixin {
    @Final
    @Unique
    private final CooldownHelper localCooldownHelper = new CooldownHelper(
            new int[]{20, 50, 120}, // Cooldowns from close to far, in ticks
            new int[]{40, 80, 300},
            new int[]{5, 25, 80}
    );
    @Final
    @Shadow
    protected PathfinderMob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void throttleVillageScan(CallbackInfoReturnable<Boolean> cir) {
        if (localCooldownHelper.shouldThrottle(mob)) cir.setReturnValue(false);
    }
}
