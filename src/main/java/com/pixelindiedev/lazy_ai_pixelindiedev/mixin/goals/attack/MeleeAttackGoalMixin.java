package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.goals.attack;

import com.pixelindiedev.lazy_ai_pixelindiedev.interfaces.TickCancellingAware;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MeleeAttackGoal.class, priority = 1003)
public class MeleeAttackGoalMixin {
    @Final
    @Shadow
    protected PathAwareEntity mob;
    @Shadow
    private int cooldown;

    @Inject(method = "tick", at = @At("HEAD"))
    private void compensateCooldown(CallbackInfo ci) {
        final int skipped = ((TickCancellingAware) mob).lazy_ai$getSkippedTicks();
        if (skipped <= 0) return;
        if (cooldown > 0) cooldown = Math.max(0, cooldown - skipped);
    }
}
