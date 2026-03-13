package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.goals.movement;

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.DistanceType;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FleeEntityGoal.class, priority = 1003)
public class FleeEntityGoalMixin {
    @Unique
    private final static int[] cooldowns = {10, 20, 40};  // Cooldowns from close to far, in ticks
    @Unique
    private final static int[] cooldownsAgressive = {15, 30, 80};
    @Unique
    private final static int[] cooldownsMinimal = {5, 10, 30};
    @Final
    @Shadow
    protected PathAwareEntity mob;
    @Unique
    private int cooldown = 0;
    @Unique
    private DistanceType previousDistanceType = DistanceType.FarRange;
    @Unique
    private int[] temparray;

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void throttleDetection(CallbackInfoReturnable<Boolean> cir) {
        final DistanceType newDistanceType = Lazy_ai_pixelindiedev.getDistance(mob);

        temparray = getCooldownList();
        if (newDistanceType != previousDistanceType) {
            cooldown = temparray[newDistanceType.ordinal()] - (temparray[previousDistanceType.ordinal()] - cooldown);
            previousDistanceType = newDistanceType;
        }

        if (cooldown > 0) {
            cooldown--;
            cir.setReturnValue(false);
        } else cooldown = temparray[newDistanceType.ordinal()];
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void reduceTickFrequency(CallbackInfo ci) {
        if (temparray == null || temparray[2] == 0) return;
        if ((mob.age + mob.getId()) % temparray[2] != 0) ci.cancel();
    }

    @Unique
    private int[] getCooldownList() {
        return switch (Lazy_ai_pixelindiedev.getOptimalizationType()) {
            case Minimal -> cooldownsMinimal;
            case Agressive -> cooldownsAgressive;
            case null, default -> cooldowns;
        };
    }
}
