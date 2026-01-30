package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev.EnableCriticalTPSMode;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "tickCramming", at = @At("HEAD"), cancellable = true)
    private void limitCramming(CallbackInfo ci) {
        if (EnableCriticalTPSMode) ci.cancel();
    }
}