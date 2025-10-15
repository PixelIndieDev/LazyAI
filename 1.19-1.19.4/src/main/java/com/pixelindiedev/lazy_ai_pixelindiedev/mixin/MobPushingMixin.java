package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

import com.pixelindiedev.lazy_ai_pixelindiedev.config.OptimalizationType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev.getDistance;
import static com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev.getOptimalizationType;

@Mixin(LivingEntity.class)
public class MobPushingMixin {
    @Unique
    private boolean shouldDisable() {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity instanceof PlayerEntity) return false;
        if (entity.getAttacking() != null || entity.getAttacker() != null) return false;

        return switch (getDistance((MobEntity) entity)) {
            case MediumRange -> getOptimalizationType() == OptimalizationType.Agressive;
            case FarRange -> getOptimalizationType() != OptimalizationType.Minimal;
            default -> false;
        };
    }

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    private void ThrottlePush(Entity other, CallbackInfo ci) {
        if (shouldDisable()) {
            ci.cancel();
        }
    }
}
