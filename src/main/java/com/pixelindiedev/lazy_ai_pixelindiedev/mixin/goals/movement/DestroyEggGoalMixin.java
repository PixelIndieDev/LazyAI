package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.goals.movement;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import com.pixelindiedev.lazy_ai_pixelindiedev.helpers.CooldownHelper;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RemoveBlockGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RemoveBlockGoal.class, priority = 1001)
public class DestroyEggGoalMixin {
    @Final
    @Unique
    private final CooldownHelper localCooldownHelper = new CooldownHelper(
            new int[]{40, 100, 200}, // Cooldowns from close to far, in ticks
            new int[]{60, 150, 300},
            new int[]{5, 15, 50}
    );
    @Final
    @Shadow
    private Mob removerMob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void ThrottleEggCheck(CallbackInfoReturnable<Boolean> cir) {
        if (Lazy_ai_pixelindiedev.getDisableZombieEggStomping()) {
            cir.setReturnValue(false);
            return;
        }

        if (localCooldownHelper.shouldThrottle(removerMob)) cir.setReturnValue(false);
    }
}
