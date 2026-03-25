package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.goals.attack;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.interfaces.TickCancellingAware;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RangedAttackGoal.class, priority = 1001)
public class ProjectileAttackGoalMixin {
    @Final
    @Shadow
    private Mob mob;
    @Shadow
    private int attackTime;
    @Shadow
    private int seeTime;

    @Inject(method = "tick", at = @At("HEAD"))
    private void compensateCooldown(CallbackInfo ci) {
        final int skipped = ((TickCancellingAware) mob).lazy_ai$getSkippedTicks();
        if (skipped <= 0) return;

        if (attackTime > 0) attackTime = Math.max(0, attackTime - skipped);
        if (seeTime > 0) seeTime = seeTime + skipped;
    }
}
