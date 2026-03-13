package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.integration;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Invoker("updateSubmergedInWaterState")
    void invokeUpdateSubmergedInWaterState();

    @Invoker("updateWaterState")
    boolean invokeUpdateWaterState();
}
