package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.tag.FluidTags;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SwimGoal.class, priority = 1003)
public abstract class SwimGoalMixin {
    @Shadow
    @Final
    private MobEntity mob;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onlyTickWhenUnderwater(CallbackInfo ci) {
        if (!mob.isTouchingWater() && !mob.isInLava()) {
            ci.cancel();
            return;
        }

        double fluidLevel = mob.getFluidHeight(FluidTags.WATER);

        if (fluidLevel < 0.3) ci.cancel();
    }
}
