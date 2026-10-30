package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.goals.brain;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import com.pixelindiedev.lazy_ai_pixelindiedev.helpers.CooldownHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Brain.class)
public class BrainMixin<E extends LivingEntity> {
    @Final
    @Unique
    private final CooldownHelper localCooldownHelper = new CooldownHelper(
            new int[]{1, 3, 7}, // Cooldowns from close to far, in ticks
            new int[]{2, 4, 10},
            new int[]{1, 2, 5}
    );

    @Inject(method = "tickSensors", at = @At("HEAD"), cancellable = true)
    private void throttleSensors(ServerLevel world, E entity, CallbackInfo ci) {
        if (!(entity instanceof Mob mob)) return;

        //don't impact breeding
        final Brain<?> brain = (Brain<?>) (Object) this;
        if (brain.hasMemoryValue(MemoryModuleType.BREED_TARGET)) return;

        if ((world.getGameTime() + mob.getId()) % localCooldownHelper.getCurrentCooldownList()[Lazy_ai_pixelindiedev.getDistance(mob).ordinal()] != 0)
            ci.cancel();
    }
}