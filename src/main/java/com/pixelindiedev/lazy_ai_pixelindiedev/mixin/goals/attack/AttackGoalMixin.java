package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.goals.attack;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.interfaces.TickCancellingAware;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.OcelotAttackGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = OcelotAttackGoal.class, priority = 1001)
public class AttackGoalMixin {
    @Shadow
    private int attackTime;
    @Final
    @Shadow
    private Mob mob;

    @Inject(method = "tick", at = @At("HEAD"))
    private void throttleTick(CallbackInfo ci) {
        final int skipped = ((TickCancellingAware) mob).lazy_ai$getSkippedTicks();
        if (skipped <= 0) return;
        if (attackTime > 0) attackTime = Math.max(0, attackTime - skipped);
    }
}
