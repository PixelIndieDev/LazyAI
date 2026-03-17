package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.goals.brain;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Brain.class)
public class BrainMixin<E extends LivingEntity> {
    @Unique
    private final static int[] cooldowns = {1, 3, 7};  // Cooldowns from close to far, in ticks
    @Unique
    private final static int[] cooldownsAgressive = {2, 4, 10};
    @Unique
    private final static int[] cooldownsMinimal = {1, 2, 5};

    @Inject(method = "tickSensors", at = @At("HEAD"), cancellable = true)
    private void throttleSensors(ServerWorld world, E entity, CallbackInfo ci) {
        if (!(entity instanceof MobEntity mob)) return;

        //don't impact breeding
        final Brain<?> brain = (Brain<?>) (Object) this;
        if (brain.hasMemoryModule(MemoryModuleType.BREED_TARGET)) return;

        if ((world.getTime() + mob.getId()) % getCooldownList()[Lazy_ai_pixelindiedev.getDistance(mob).ordinal()] != 0)
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