package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LookControl.class)
public class LookControlMixin {
    @Unique
    private final static int[] cooldowns = {1, 3, 10};  // Cooldowns from close to far, in ticks
    @Unique
    private final static int[] cooldownsAgressive = {1, 7, 25};
    @Unique
    private final static int[] cooldownsMinimal = {1, 2, 6};

    @Final
    @Shadow
    protected Mob mob;

    @Unique
    private int offset;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void captureMob(Mob mob, CallbackInfo ci) {
        offset = mob.getId();
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        if ((mob.tickCount + offset) % getCooldownList()[Lazy_ai_pixelindiedev.getDistance(mob).ordinal()] != 0)
            ci.cancel();
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