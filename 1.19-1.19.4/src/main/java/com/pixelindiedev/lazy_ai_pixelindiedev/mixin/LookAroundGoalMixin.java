package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.DistanceType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LookAroundGoal.class, priority = 1001)
public class LookAroundGoalMixin {
    private final static int[] cooldowns = {2, 10, 30};  // Cooldowns from close to far, in ticks
    private final static int[] cooldownsAgressive = {5, 15, 50};
    private final static int[] cooldownsMinimal = {1, 5, 15};
    @Shadow
    @Final
    private MobEntity mob;

    @Unique
    private int cooldown = 0;
    @Unique
    private DistanceType previousDistanceType = DistanceType.FarRange;

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void throttleLookAround(CallbackInfoReturnable<Boolean> cir) {
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
            default -> cooldowns;
        };
    }
}
