package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.DistanceType;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MoveToTargetPosGoal.class, priority = 1001)
public class MoveToTargetPosGoalMixin {
    @Unique
    private final static int[] cooldowns = {20, 50, 120};  // Cooldowns from close to far, in ticks
    @Unique
    private final static int[] cooldownsAgressive = {40, 80, 300};
    @Unique
    private final static int[] cooldownsMinimal = {5, 25, 80};
    @Unique
    private MobEntity mob;
    @Unique
    private int cooldown = 0;
    @Unique
    private DistanceType previousDistanceType = DistanceType.FarRange;
    @Unique
    private int[] temparray;

    @Inject(method = "<init>(Lnet/minecraft/entity/mob/PathAwareEntity;DI)V", at = @At("RETURN"))
    private void captureMob(PathAwareEntity mob, double speed, int range, CallbackInfo ci) {
        this.mob = mob;
    }

    @Inject(method = "<init>(Lnet/minecraft/entity/mob/PathAwareEntity;DII)V", at = @At("RETURN"))
    private void captureMob2(PathAwareEntity mob, double speed, int range, int maxYDiff, CallbackInfo ci) {
        this.mob = mob;
    }

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void ThrottleSearch(CallbackInfoReturnable<Boolean> cir) {
        DistanceType newDistanceType = Lazy_ai_pixelindiedev.getDistance(mob);

        temparray = getCooldownList();
        if (newDistanceType != previousDistanceType) {
            cooldown = temparray[newDistanceType.ordinal()] - (temparray[previousDistanceType.ordinal()] - cooldown);
            previousDistanceType = newDistanceType;
        }
        if (cooldown > 0) {
            cooldown--;
            cir.setReturnValue(false);
        } else {
            cooldown = temparray[newDistanceType.ordinal()];
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void reduceTickLoad(CallbackInfo ci) {
        if (temparray == null || temparray[2] == 0) return;

        if ((mob.age + mob.getId()) % temparray[2] != 0) ci.cancel();
    }

    @Unique
    private int[] getCooldownList() {
        return switch (Lazy_ai_pixelindiedev.getOptimalizationType()) {
            case Minimal -> cooldownsMinimal;
            case Agressive -> cooldownsAgressive;
            default -> cooldowns;
        };
    }
}
