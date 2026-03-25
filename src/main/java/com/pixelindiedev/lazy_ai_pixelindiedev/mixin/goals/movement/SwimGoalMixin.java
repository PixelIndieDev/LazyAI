package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.goals.movement;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FloatGoal.class, priority = 1003)
public abstract class SwimGoalMixin {
    @Shadow
    @Final
    private Mob mob;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onlyTickWhenUnderwater(CallbackInfo ci) {
        if (!mob.isInWater() && !mob.isInLava()) {
            ci.cancel();
            return;
        }
        final double fluidLevel = mob.getFluidHeight(FluidTags.WATER);
        if (fluidLevel < 0.3) ci.cancel();
    }
}
