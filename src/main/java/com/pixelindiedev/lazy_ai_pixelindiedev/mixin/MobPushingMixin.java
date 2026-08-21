package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.CriticalTPSModeEnum;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.DistanceType;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.EntityCategoryEnum;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.OptimalizationType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.pixelindiedev.lazy_ai_pixelindiedev.EntityClassificationer.GetEntityCategory;
import static com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev.CriticalTPSMode;
import static com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev.GetMobEntity;
import static com.pixelindiedev.lazy_ai_pixelindiedev.config.ThrottleHelper.ShouldThrottlePushing;

@Mixin(LivingEntity.class)
public abstract class MobPushingMixin {
    @Unique
    private final static int[] cooldowns = {5, 10, 15};  // Cooldowns from close to far, in ticks
    @Unique
    private final static int[] cooldownsAgressive = {10, 15, 25};
    @Unique
    private final static int[] cooldownsMinimal = {2, 6, 10};

    @Unique
    private EntityCategoryEnum cachedCategory;

    @Unique
    private OptimalizationType cachedOptiType;
    @Unique
    private int[] cachedCooldownList;

    @Unique
    private int waitingForCramming;

    @Unique
    private int cooldown = 0;
    @Unique
    private Mob mob;
    @Unique
    private DistanceType previousDistanceType = DistanceType.FarRange;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void captureMob(EntityType<?> type, Level world, CallbackInfo ci) {
        this.mob = GetMobEntity((LivingEntity) (Object) this);
        cachedCategory = GetEntityCategory(BuiltInRegistries.ENTITY_TYPE.getResourceKey(type).orElseThrow());
        waitingForCramming = 0;
    }

    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void ThrottlePush(Entity other, CallbackInfo ci) {
        var shouldResult = ShouldThrottlePushing(cachedCategory, mob, waitingForCramming, 0);
        if (shouldResult.shouldThrottle()) {
            ci.cancel();
        }
        waitingForCramming = shouldResult.newWaitingForCramming();

        if (CriticalTPSMode.ordinal() > CriticalTPSModeEnum.Moderate.ordinal()) ci.cancel();

        if (mob != null) {
            DistanceType newDistanceType = Lazy_ai_pixelindiedev.getDistance(mob);

            final int[] temparray = getCooldownList();
            if (newDistanceType != previousDistanceType) {
                cooldown = temparray[newDistanceType.ordinal()] - (temparray[previousDistanceType.ordinal()] - cooldown);
                previousDistanceType = newDistanceType;
            }

            if (cooldown > 0) {
                cooldown--;
                ci.cancel();
            } else cooldown = temparray[newDistanceType.ordinal()];
        }
    }

    @Unique
    private int[] getCooldownList() {
        final OptimalizationType current = Lazy_ai_pixelindiedev.getOptimalizationType();
        if (current != cachedOptiType) {
            cachedOptiType = current;
            cachedCooldownList = switch (current) {
                case Minimal -> cooldownsMinimal;
                case Agressive -> cooldownsAgressive;
                case null, default -> cooldowns;
            };
        }
        return cachedCooldownList;
    }
}
