package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.goals.universal;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.helpers.CooldownHelper;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ResetUniversalAngerTargetGoal.class, priority = 1001)
public class UniversalAngerGoalmixin<T extends Mob & NeutralMob> {
    @Final
    @Unique
    private final CooldownHelper localCooldownHelper = new CooldownHelper(
            new int[]{10, 25, 50}, // Cooldowns from close to far, in ticks
            new int[]{15, 30, 60},
            new int[]{5, 15, 40}
    );
    @Final
    @Shadow
    private T mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void TrotthleAngerChecks(CallbackInfoReturnable<Boolean> cir) {
        if (localCooldownHelper.shouldThrottle(mob)) cir.setReturnValue(false);
    }
}
