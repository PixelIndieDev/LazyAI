package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.DistanceType;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(value = FleeEntityGoal.class, priority = 1003)
public class FleeEntityGoalMixin {
    private final static int[] cooldowns = {10, 20, 40};  // Cooldowns from close to far, in ticks
    private final static int[] cooldownsAgressive = {15, 30, 80};
    private final static int[] cooldownsMinimal = {5, 10, 30};
    @Unique
    private MobEntity mob;
    @Unique
    private int cooldown = 0;
    @Unique
    private DistanceType previousDistanceType = DistanceType.FarRange;
    @Unique
    private int[] temparray;

    @Inject(method = "<init>(Lnet/minecraft/entity/mob/PathAwareEntity;Ljava/lang/Class;FDD)V", at = @At("RETURN"))
    private void captureMob(PathAwareEntity mob, Class fleeFromType, float distance, double slowSpeed, double fastSpeed, CallbackInfo ci) {
        this.mob = mob;
    }

    @Inject(method = "<init>(Lnet/minecraft/entity/mob/PathAwareEntity;Ljava/lang/Class;FDDLjava/util/function/Predicate;)V", at = @At("RETURN"))
    private void captureMob1(PathAwareEntity fleeingEntity, Class classToFleeFrom, float fleeDistance, double fleeSlowSpeed, double fleeFastSpeed, Predicate inclusionSelector, CallbackInfo ci) {
        this.mob = fleeingEntity;
    }

    @Inject(method = "<init>(Lnet/minecraft/entity/mob/PathAwareEntity;Ljava/lang/Class;Ljava/util/function/Predicate;FDDLjava/util/function/Predicate;)V", at = @At("RETURN"))
    private void captureMob2(PathAwareEntity mob, Class fleeFromType, Predicate extraInclusionSelector, float distance, double slowSpeed, double fastSpeed, Predicate inclusionSelector, CallbackInfo ci) {
        this.mob = mob;
    }

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void throttleDetection(CallbackInfoReturnable<Boolean> cir) {
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
    private void reduceTickFrequency(CallbackInfo ci) {
        if (temparray == null || temparray[2] == 0) return;

        if ((mob.age + mob.getId()) % temparray[2] != 0) ci.cancel();
    }

    private int[] getCooldownList() {
        return switch (Lazy_ai_pixelindiedev.getOptimalizationType()) {
            case Minimal -> cooldownsMinimal;
            case Agressive -> cooldownsAgressive;
            default -> cooldowns;
        };
    }
}
