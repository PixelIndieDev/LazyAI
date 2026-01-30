package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.DistanceType;
import net.minecraft.entity.ai.goal.AttackGoal;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AttackGoal.class, priority = 1001)
public class AttackGoalMixin {
    @Unique
    private final static int[] cooldowns = {10, 25, 50};  // Cooldowns from close to far, in ticks
    @Unique
    private final static int[] cooldownsAgressive = {15, 30, 60};
    @Unique
    private final static int[] cooldownsMinimal = {5, 15, 40};
    @Unique
    private MobEntity mob;
    @Unique
    private int cooldown = 0;
    @Unique
    private DistanceType previousDistanceType = DistanceType.FarRange;
    @Unique
    private int[] temparray;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void captureMob(MobEntity mob, CallbackInfo ci) {
        this.mob = mob;
    }

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void throttleCanStart(CallbackInfoReturnable<Boolean> cir) {
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
    private void throttleTick(CallbackInfo ci) {
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
