package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.DistanceType;
import net.minecraft.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;

@Mixin(value = MoveThroughVillageGoal.class, priority = 1001)
public class MoveThroughVillageGoalMixin {
    private final static int[] cooldowns = {20, 50, 120};  // Cooldowns from close to far, in ticks
    private final static int[] cooldownsAgressive = {40, 80, 300};
    private final static int[] cooldownsMinimal = {5, 25, 80};
    @Unique
    private MobEntity mob;
    @Unique
    private int cooldown = 0;
    @Unique
    private DistanceType previousDistanceType = DistanceType.FarRange;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void captureMob(PathAwareEntity entity, double speed, boolean requiresNighttime, int distance, BooleanSupplier doorPassingThroughGetter, CallbackInfo ci) {
        mob = entity;
    }

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void throttleVillageScan(CallbackInfoReturnable<Boolean> cir) {
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
