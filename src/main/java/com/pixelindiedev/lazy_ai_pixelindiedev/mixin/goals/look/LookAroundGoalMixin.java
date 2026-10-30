package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.goals.look;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.helpers.CooldownHelper;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RandomLookAroundGoal.class, priority = 1001)
public class LookAroundGoalMixin {
    @Final
    @Unique
    private final CooldownHelper localCooldownHelper = new CooldownHelper(
            new int[]{2, 10, 30}, // Cooldowns from close to far, in ticks
            new int[]{5, 15, 50},
            new int[]{1, 5, 15}
    );
    @Shadow
    @Final
    private Mob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void throttleLookAround(CallbackInfoReturnable<Boolean> cir) {
        if (localCooldownHelper.shouldThrottle(mob)) cir.setReturnValue(false);
    }
}
