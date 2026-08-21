package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.goals.brain;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import com.pixelindiedev.lazy_ai_pixelindiedev.enums.OptimalizationType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
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

    @Unique
    private OptimalizationType cachedOptiType;
    @Unique
    private int[] cachedCooldownList;

    @Inject(method = "tickSensors", at = @At("HEAD"), cancellable = true)
    private void throttleSensors(ServerLevel world, E entity, CallbackInfo ci) {
        if (!(entity instanceof Mob mob)) return;

        //don't impact breeding
        final Brain<?> brain = (Brain<?>) (Object) this;
        if (brain.hasMemoryValue(MemoryModuleType.BREED_TARGET)) return;

        if ((world.getGameTime() + mob.getId()) % getCooldownList()[Lazy_ai_pixelindiedev.getDistance(mob).ordinal()] != 0)
            ci.cancel();
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