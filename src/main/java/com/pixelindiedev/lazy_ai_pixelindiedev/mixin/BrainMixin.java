package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
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
        if (entity instanceof MobEntity mob) {
            if (world.getTime() % getCooldownList()[Lazy_ai_pixelindiedev.getDistance(mob).ordinal()] != 0) {
                ci.cancel();
            }
        }
    }

    @Unique
    private int[] getCooldownList() {
        return switch (Lazy_ai_pixelindiedev.getOptimalizationType()) {
            case Minimal -> cooldownsMinimal;
            case Agressive -> cooldownsAgressive;
            default -> cooldowns;
        };
    }
}