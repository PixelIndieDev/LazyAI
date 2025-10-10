package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.DistanceType;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WanderAroundGoal.class, priority = 1002)
public class WanderAroundGoalMixin {
    private final static int[] cooldowns = {1, 4, 8};  // Cooldowns from close to far, in ticks
    private final static int[] cooldownsAgressive = {2, 7, 12};
    private final static int[] cooldownsMinimal = {0, 2, 5};
    @Unique
    private int cooldown = 0;
    @Unique
    private MobEntity mob;
    @Unique
    private DistanceType previousDistanceType = DistanceType.FarRange;

    @Inject(method = "<init>(Lnet/minecraft/entity/mob/PathAwareEntity;D)V", at = @At("RETURN"))
    private void captureMob2(PathAwareEntity entity, double speed, CallbackInfo ci) {
        this.mob = entity;
    }

    @Inject(method = "<init>(Lnet/minecraft/entity/mob/PathAwareEntity;DI)V", at = @At("RETURN"))
    private void captureMob3(PathAwareEntity entity, double speed, int chance, CallbackInfo ci) {
        this.mob = entity;
    }

    @Inject(method = "<init>(Lnet/minecraft/entity/mob/PathAwareEntity;DIZ)V", at = @At("RETURN"))
    private void captureMob(PathAwareEntity entity, double speed, int chance, boolean canDespawn, CallbackInfo ci) {
        this.mob = entity;
    }

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void ThrottleWandering(CallbackInfoReturnable<Boolean> cir) {
        DistanceType newDistanceType = Lazy_ai_pixelindiedev.getDistance(mob);

        int[] temparray = getCooldownList();
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

    private int[] getCooldownList() {
        return switch (Lazy_ai_pixelindiedev.getOptimalizationType()) {
            case Minimal -> cooldownsMinimal;
            case Agressive -> cooldownsAgressive;
            case null, default -> cooldowns;
        };
    }
}
