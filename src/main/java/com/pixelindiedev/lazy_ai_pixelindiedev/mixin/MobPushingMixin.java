package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.enums.CriticalTPSModeEnum;
import com.pixelindiedev.lazy_ai_pixelindiedev.enums.EntityCategoryEnum;
import com.pixelindiedev.lazy_ai_pixelindiedev.helpers.CooldownHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.pixelindiedev.lazy_ai_pixelindiedev.EntityClassificationer.GetEntityCategory;
import static com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev.CriticalTPSMode;
import static com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev.GetMobEntity;
import static com.pixelindiedev.lazy_ai_pixelindiedev.helpers.ThrottleHelper.ShouldThrottlePushing;

@Mixin(LivingEntity.class)
public abstract class MobPushingMixin {
    @Final
    @Unique
    private final CooldownHelper localCooldownHelper = new CooldownHelper(
            new int[]{5, 10, 15}, // Cooldowns from close to far, in ticks
            new int[]{10, 15, 25},
            new int[]{2, 6, 10}
    );
    @Unique
    private EntityCategoryEnum cachedCategory;

    @Unique
    private int waitingForCramming;

    @Unique
    private Mob mob;

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

        if (CriticalTPSMode.ordinal() > CriticalTPSModeEnum.Moderate.ordinal()) {
            ci.cancel();
            return;
        }
        if (mob != null && localCooldownHelper.shouldThrottle(mob)) ci.cancel();
    }
}
