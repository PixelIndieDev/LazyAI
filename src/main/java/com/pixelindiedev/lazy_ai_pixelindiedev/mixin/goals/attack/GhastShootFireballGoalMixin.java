package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.goals.attack;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.entity.monster.Ghast$GhastShootFireballGoal", priority = 600)
public class GhastShootFireballGoalMixin {
    @Unique
    private final static float cooldown = 0.2f; //original value divided by the cooldown
    @Unique
    private final static int cooldownServerTick = 5;

    // increase tick to not break the ghast animations of the mob-ai-tweaks mod
    // If the ticks are not trottled, don't do any calculations with the constants
    @Unique
    private static final boolean mobAITweaksIsInstalled = net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("mob-ai-tweaks");

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void throttleFireballTick(CallbackInfo ci) {
        if (!mobAITweaksIsInstalled) {
            if ((Lazy_ai_pixelindiedev.getServerTick() % cooldownServerTick) != 0) ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=10"))
    private int adjustSightDelay2(int original) {
        if (mobAITweaksIsInstalled) return original;
        else return (int) (original * cooldown);
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=20"))
    private int adjustSightDelay1(int original) {
        if (mobAITweaksIsInstalled) return original;
        else return (int) (original * cooldown);
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=-40"))
    private int adjustSightDelay(int original) {
        if (mobAITweaksIsInstalled) return original;
        else return (int) (original * cooldown);
    }
}
