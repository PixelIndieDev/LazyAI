package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.navigation;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import com.pixelindiedev.lazy_ai_pixelindiedev.config.DistanceType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PathNavigation.class, priority = 1004)
public class EntityNavigationMixin {
    @Unique
    private final static int[] cooldowns = {1, 12, 80};  // Cooldowns from close to far, in ticks
    @Unique
    private final static int[] cooldownsAgressive = {1, 25, 120};
    @Unique
    private final static int[] cooldownsMinimal = {1, 5, 20};
    @Shadow
    @Final
    protected Mob mob;
    @Shadow
    @Final
    protected Level level;
    @Unique
    private int cooldown = 0;
    @Unique
    private DistanceType previousDistanceType = DistanceType.FarRange;

    @Inject(method = "shouldRecomputePath", at = @At("HEAD"), cancellable = true)
    private void ThrottleEntityNav(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (((level.getGameTime() + mob.getId()) & 1) != 0L) cir.setReturnValue(false);

        DistanceType newDistanceType = Lazy_ai_pixelindiedev.getDistance(mob);

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
