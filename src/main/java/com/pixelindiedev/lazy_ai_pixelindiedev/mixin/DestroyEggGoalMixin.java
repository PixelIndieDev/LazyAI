package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.DistanceType;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.goal.StepAndDestroyBlockGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = StepAndDestroyBlockGoal.class, priority = 1001)
public class DestroyEggGoalMixin {
    @Unique
    private final static int[] cooldowns = {40, 100, 200};  // Cooldowns from close to far, in ticks
    @Unique
    private final static int[] cooldownsAgressive = {60, 150, 300};
    @Unique
    private final static int[] cooldownsMinimal = {5, 15, 50};
    @Unique
    private int cooldown = 0;
    @Unique
    private MobEntity mob;
    @Unique
    private DistanceType previousDistanceType = DistanceType.FarRange;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void captureMob(Block targetBlock, PathAwareEntity mob, double speed, int maxYDifference, CallbackInfo ci) {
        this.mob = mob;
    }

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void ThrottleEggCheck(CallbackInfoReturnable<Boolean> cir) {
        if (Lazy_ai_pixelindiedev.getDisableZombieEggStomping()) {
            cir.setReturnValue(false);
            return;
        }

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

    @Unique
    private int[] getCooldownList() {
        return switch (Lazy_ai_pixelindiedev.getOptimalizationType()) {
            case Minimal -> cooldownsMinimal;
            case Agressive -> cooldownsAgressive;
            default -> cooldowns;
        };
    }
}
