package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.navigation;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.helpers.CooldownHelper;
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
    @Final
    @Unique
    private final CooldownHelper localCooldownHelper = new CooldownHelper(
            new int[]{1, 12, 80}, // Cooldowns from close to far, in ticks
            new int[]{1, 25, 120},
            new int[]{1, 5, 20}
    );
    @Shadow
    @Final
    protected Mob mob;
    @Shadow
    @Final
    protected Level level;

    @Inject(method = "shouldRecomputePath", at = @At("HEAD"), cancellable = true)
    private void ThrottleEntityNav(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (((level.getGameTime() + mob.getId()) & 1) != 0L) cir.setReturnValue(false);
        if (localCooldownHelper.shouldThrottle(mob)) cir.setReturnValue(false);
    }
}
