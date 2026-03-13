package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.goals.movement;

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.DistanceType;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WanderAroundGoal.class, priority = 1002)
public class WanderAroundGoalMixin {
    @Unique
    private final static int[] cooldowns = {1, 4, 8};  // Cooldowns from close to far, in ticks
    @Unique
    private final static int[] cooldownsAgressive = {2, 7, 12};
    @Unique
    private final static int[] cooldownsMinimal = {0, 2, 5};
    @Final
    @Shadow
    protected PathAwareEntity mob;
    @Unique
    private int cooldown = 0;
    @Unique
    private DistanceType previousDistanceType = DistanceType.FarRange;

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void ThrottleWandering(CallbackInfoReturnable<Boolean> cir) {
        final DistanceType newDistanceType = Lazy_ai_pixelindiedev.getDistance(mob);

        final int[] temparray = getCooldownList();
        if (newDistanceType != previousDistanceType) {
            cooldown = temparray[newDistanceType.ordinal()] - (temparray[previousDistanceType.ordinal()] - cooldown);
            previousDistanceType = newDistanceType;
        }

        if (cooldown > 0) {
            cooldown--;
            cir.setReturnValue(false);
        } else cooldown = temparray[newDistanceType.ordinal()];
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
