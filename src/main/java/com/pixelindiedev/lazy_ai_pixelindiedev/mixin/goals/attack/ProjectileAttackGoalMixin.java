package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.goals.attack;

import com.pixelindiedev.lazy_ai_pixelindiedev.interfaces.TickCancellingAware;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ProjectileAttackGoal.class, priority = 1001)
public class ProjectileAttackGoalMixin {
    @Final
    @Shadow
    private MobEntity mob;
    @Shadow
    private int updateCountdownTicks;
    @Shadow
    private int seenTargetTicks;

    @Inject(method = "tick", at = @At("HEAD"))
    private void compensateCooldown(CallbackInfo ci) {
        final int skipped = ((TickCancellingAware) mob).lazy_ai$getSkippedTicks();
        if (skipped <= 0) return;

        if (updateCountdownTicks > 0) updateCountdownTicks = Math.max(0, updateCountdownTicks - skipped);
        if (seenTargetTicks > 0) seenTargetTicks = seenTargetTicks + skipped;
    }
}
