package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.goals.attack;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.interfaces.TickCancellingAware;
import com.pixelindiedev.lazy_ai_pixelindiedev.mixin.integration.LivingEntityAccessor;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RangedCrossbowAttackGoal.class, priority = 1001)
public class CrossbowAttackGoalMixin<T extends Monster & RangedAttackMob & CrossbowAttackMob> {
    @Final
    @Shadow
    private T mob;

    @Inject(method = "tick", at = @At("HEAD"))
    private void compensateCooldown(CallbackInfo ci) {
        final int skipped = ((TickCancellingAware) mob).lazy_ai$getSkippedTicks();
        if (skipped <= 0) return;

        if (mob.isUsingItem()) {
            final LivingEntityAccessor accessor = (LivingEntityAccessor) mob;
            accessor.setItemUseTimeLeftAccessor(Math.max(0, accessor.getItemUseTimeLeftAccessor() - skipped));
        }
    }
}
